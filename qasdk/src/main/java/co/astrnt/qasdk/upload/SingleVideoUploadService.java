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
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
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

public class SingleVideoUploadService extends Service implements UploadStatusDelegate {

    public static final String EXT_QUESTION_ID = "SingleVideoUploadService.QuestionId";

    public static final long NOTIFY_INTERVAL = 2 * 60 * 1000;

    private long questionId;

    private Context context;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Timer mTimer = null;
    private QuestionApiDao currentQuestion;
    private AstrntSDK astrntSDK;
    private boolean isDoingCompress = true;
    InterviewApiDao interviewApiDao;

    private NotificationManager mNotifyManager;

    public static void start(Context context, long questionId) {
        Timber.d("start uploading id = %d", questionId);
        Intent intent = new Intent(context, SingleVideoUploadService.class)
                .putExtra(EXT_QUESTION_ID, questionId);
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            questionId = intent.getLongExtra(EXT_QUESTION_ID, 0);

            currentQuestion = astrntSDK.searchQuestionById(questionId);

            createNotification();

            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
                mTimer = new Timer();
            } else {
                mTimer = new Timer();
            }
            mTimer.scheduleAtFixedRate(new SingleVideoUploadService.TimeDisplayTimerTask(), 5000, NOTIFY_INTERVAL);
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        astrntSDK = new AstrntSDK();

    }

    private void createNotification() {
        if (currentQuestion == null) {
            return;
        }
        int mNotificationId = (int) currentQuestion.getId();

        final String channelId = "Astronaut Q&A";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_autorenew_white_24dp)
                .setContentTitle("Astronaut Q&A")
                .setContentText("Upload Video")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Upload Video";
            String description = "Astronaut Q&A Upload Video";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);

            mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (mNotifyManager != null) {
                mNotifyManager.createNotificationChannel(channel);
            }

            startForeground(mNotificationId, mBuilder.build());
        } else {
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        mNotifyManager.notify(mNotificationId, mBuilder.build());
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void doUploadVideo() {
        Timber.d("do uploading");
        isDoingCompress = true;

        interviewApiDao = astrntSDK.getCurrentInterview();

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

                    new Handler(Looper.getMainLooper()).post(() -> {
                        VideoCompressService.start(context, currentQuestion.getVideoPath(), currentQuestion.getId());
                    });

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
                    try {
                        Timber.d("try execute upload");
                        String uploadId = FileUploadHelper.uploadVideo(context, interviewApiDao, currentQuestion, apiUrl)
                                .setNotificationConfig(notificationConfig)
                                .setDelegate(this).startUpload();

                        astrntSDK.saveUploadId(uploadId);
                    } catch (FileNotFoundException exc) {
                        Timber.e("File not exception");
                    } catch (IllegalArgumentException exc) {
                        Timber.e("IllegalArgumentException exception");
                    } catch (MalformedURLException exc) {
                        Timber.e("MalformedURLException exception");
                    }

                }
            }
        } catch (Exception exc) {
            Timber.e("SingleVideoUploadService %s", exc.getMessage());

            LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                    new LogDao("Background Upload (Exc)",
                            exc.getMessage())
            );
            stopService();
        }

    }

    private void sendLog() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!ServiceUtils.isMyServiceRunning(context, SendLogService.class)) {
                SendLogService.start(context);
            }
        });
    }

    public void stopService() {
        sendLog();
        if (mTimer != null) mTimer.cancel();
        if (mNotifyManager != null) mNotifyManager.cancelAll();
        stopSelf();
    }

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        if (uploadInfo != null) {
            Timber.d("Video Upload Progress : %s", uploadInfo.getProgressPercent());
            astrntSDK.updateProgress(currentQuestion, uploadInfo.getProgressPercent());
        } else {
            Timber.e("upload progress null");
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
        Timber.d("on completed");
        astrntSDK.removeUploadId();
        astrntSDK.markUploaded(currentQuestion);

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Background Upload (Complete)",
                        "Success uploaded for question id " + currentQuestion.getId()
                )
        );

        List<QuestionApiDao> uploadingVideo = astrntSDK.getPending(UploadStatusType.PENDING);
        List<QuestionApiDao> compressedVideo = astrntSDK.getPending(UploadStatusType.COMPRESSED);

        isDoingCompress = true;
        for (QuestionApiDao item : uploadingVideo) {
            if (isDoingCompress) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> VideoCompressService.start(context, item.getVideoPath(), item.getId()), 1000);
                isDoingCompress = false;
            }
        }

        for (QuestionApiDao item : compressedVideo) {
            if (isDoingCompress) {
                Timber.e("current status upload compressed");
                SingleVideoUploadService.start(context, item.getId());
                isDoingCompress = false;
            }
        }

        List<QuestionApiDao> compressingVideo = astrntSDK.getPending(UploadStatusType.COMPRESSING);
        for (QuestionApiDao item : compressingVideo) {
            if (isDoingCompress) {
                if (!ServiceUtils.isMyServiceRunning(context, VideoCompressService.class)) {
                    astrntSDK.markAsPending(item, item.getVideoPath());

                    Timber.e("current status compress is compressing");
                    new Handler(Looper.getMainLooper()).postDelayed(() ->VideoCompressService.start(context, item.getVideoPath(), item.getId()), 1000);
                    isDoingCompress = false;
                } else {
                    Timber.e("still running compress successing");
                }
            }
        }
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
