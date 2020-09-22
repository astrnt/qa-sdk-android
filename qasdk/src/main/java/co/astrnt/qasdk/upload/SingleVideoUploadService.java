package co.astrnt.qasdk.upload;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.google.gson.Gson;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.R;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.type.UploadStatusType;
import co.astrnt.qasdk.utils.FileUploadHelper;
import co.astrnt.qasdk.utils.LogUtil;
import co.astrnt.qasdk.utils.ServiceUtils;
import co.astrnt.qasdk.utils.UploadNotifConfig;
import co.astrnt.qasdk.utils.services.SendLogService;
import co.astrnt.qasdk.videocompressor.services.VideoCompressService;
import timber.log.Timber;

public class SingleVideoUploadService extends Service {

    public static final String EXT_QUESTION_ID = "SingleVideoUploadService.QuestionId";

    public static final long NOTIFY_INTERVAL = 2 * 60 * 1000;

    private long questionId;

    private Context context;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Timer mTimer = null;
    private QuestionApiDao currentQuestion;
    private AstrntSDK astrntSDK;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int mNotificationId;

    public static void start(Context context, long questionId) {
        Intent intent = new Intent(context, SingleVideoUploadService.class)
                .putExtra(EXT_QUESTION_ID, questionId);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            questionId = intent.getLongExtra(EXT_QUESTION_ID, 0);

            currentQuestion = astrntSDK.searchQuestionById(questionId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        astrntSDK = new AstrntSDK();

        startServiceOreoCondition();

        if (mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
        }
        mTimer.scheduleAtFixedRate(new SingleVideoUploadService.TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    private void startServiceOreoCondition() {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotification("Upload Video");
        }
    }

    private void createNotification(String message) {
        if (currentQuestion == null) {
            return;
        }
        mNotificationId = (int) currentQuestion.getId();

        // Make a channel if necessary
        final String channelId = "Astronaut Q&A";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "Upload Video";
            String description = "Astronaut Q&A Upload Video";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);

            // Add the channel
            mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (mNotifyManager != null) {
                mNotifyManager.createNotificationChannel(channel);
            }
        } else {
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        // Create the notification
        mBuilder = new NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_autorenew_white_24dp)
                .setContentTitle("Astronaut Q&A")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        mNotifyManager.notify(mNotificationId, mBuilder.build());
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void doUploadVideo() {

        final InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        try {

            if (currentQuestion.getVideoPath() == null) {
                astrntSDK.getVideoFile(context, interviewApiDao.getInterviewCode(), currentQuestion.getId());
                stopService();
            }

            final File file = new File(currentQuestion.getVideoPath());

            if (!file.exists()) {
                astrntSDK.getVideoFile(context, interviewApiDao.getInterviewCode(), currentQuestion.getId());
                stopService();
            } else {

                if (currentQuestion.getVideoPath().contains("_raw.mp4")) {
                    astrntSDK.markAsPending(currentQuestion, currentQuestion.getVideoPath());

                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                            new LogDao("Background Upload (Pending)",
                                    "Video Still RAW File, will start compress first")
                    );

                    VideoCompressService.start(context, currentQuestion.getVideoPath(), currentQuestion.getId());

                    stopService();

                } else {
                    if (currentQuestion.getUploadStatus().equals(UploadStatusType.NOT_ANSWER) ||
                            currentQuestion.getUploadStatus().equals(UploadStatusType.UPLOADED)) {
                        stopService();
                        return;
                    }

                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                            new LogDao("Background Upload (Upload)",
                                    "Video Still Starting Upload")
                    );

                    List<QuestionApiDao> allVideoQuestion = astrntSDK.getAllVideoQuestion();

                    int counter = 0;
                    int totalQuestion = allVideoQuestion.size();

                    for (int i = 0; i < allVideoQuestion.size(); i++) {
                        QuestionApiDao item = allVideoQuestion.get(i);
                        if (item.getId() == currentQuestion.getId()) {
                            counter = i;
                        }
                    }

                    String uploadMessage = "Uploading video " + (counter + 1) + " from " + totalQuestion;

                    UploadNotificationConfig notificationConfig = UploadNotifConfig.getSingleNotificationConfig(uploadMessage);
                    notificationConfig.setNotificationChannelId(UploadService.NAMESPACE);
                    notificationConfig.setClearOnActionForAllStatuses(true);
                    notificationConfig.setRingToneEnabled(false);
                    astrntSDK.markUploading(currentQuestion);

                    String apiUrl = astrntSDK.getApiUrl() + "v2/video/upload";
                    String uploadId = FileUploadHelper.uploadVideo(context, interviewApiDao, currentQuestion, apiUrl)
                            .setNotificationConfig(notificationConfig)
                            .setDelegate(new UploadStatusDelegate() {
                                @Override
                                public void onProgress(Context context, UploadInfo uploadInfo) {
                                    if (uploadInfo != null) {
                                        Timber.d("Video Upload Progress : %s", uploadInfo.getProgressPercent());
                                        astrntSDK.updateProgress(currentQuestion, uploadInfo.getProgressPercent());
                                    }
                                }

                                @Override
                                public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                                    astrntSDK.removeUploadId();
                                    Timber.e("Video Upload Error : ");
                                    String message = "";
                                    if (serverResponse != null && serverResponse.getBody() != null) {
                                        try {
                                            BaseApiDao baseApiDao = new Gson().fromJson(serverResponse.getBodyAsString(), BaseApiDao.class);
                                            message = baseApiDao.getMessage();
                                            Timber.e(baseApiDao.getMessage());
                                        } catch (Exception e) {
                                            message = e.getMessage();
                                        }
                                    } else {
                                        if (exception != null) {
                                            message = exception.getMessage();
                                        }
                                    }

                                    Timber.e("Video Upload Error : %s", message);
                                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                                            new LogDao("Background Upload (Error)",
                                                    "Error " + message
                                            )
                                    );

                                    astrntSDK.markAsCompressed(currentQuestion);
                                    stopService();
                                }

                                @Override
                                public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                                    astrntSDK.removeUploadId();
                                    astrntSDK.markUploaded(currentQuestion);

                                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                                            new LogDao("Background Upload (Complete)",
                                                    "Success uploaded for question id " + currentQuestion.getId()
                                            )
                                    );
                                    stopService();
                                }

                                @Override
                                public void onCancelled(Context context, UploadInfo uploadInfo) {
                                    astrntSDK.removeUploadId();
                                    Timber.e("Video Upload Canceled");
                                    astrntSDK.markAsCompressed(currentQuestion);

                                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                                            new LogDao("Background Upload (Cancelled)",
                                                    "Cancelled"
                                            )
                                    );
                                    stopService();
                                }
                            }).startUpload();

                    astrntSDK.saveUploadId(uploadId);

                    Timber.d("SingleVideoUploadService %s", uploadId);
                }
            }
        } catch (Exception exc) {
            Timber.d("SingleVideoUploadService %s", exc.getMessage());

            LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                    new LogDao("Background Upload (Exc)",
                            exc.getMessage())
            );
            stopService();
        }

    }

    private void sendLog() {
        if (!ServiceUtils.isMyServiceRunning(context, SendLogService.class)) {
            SendLogService.start(context);
        }
    }

    public void stopService() {
        sendLog();
        if (mTimer != null) mTimer.cancel();
        if (mNotifyManager != null) mNotifyManager.cancelAll();
        stopSelf();
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(() -> {
                currentQuestion = astrntSDK.searchQuestionById(questionId);
                if (currentQuestion != null) {
                    if (currentQuestion.getUploadStatus().equals(UploadStatusType.COMPRESSED)) {
                        if (mNotifyManager != null) mNotifyManager.cancelAll();
                        doUploadVideo();
                    } else {
                        stopService();
                    }
                } else {
                    stopService();
                }
            });
        }
    }
}
