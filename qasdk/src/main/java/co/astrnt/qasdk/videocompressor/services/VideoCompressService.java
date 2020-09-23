package co.astrnt.qasdk.videocompressor.services;

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

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.NotificationCompat;
import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.R;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.event.CompressEvent;
import co.astrnt.qasdk.type.UploadStatusType;
import co.astrnt.qasdk.upload.SingleVideoUploadService;
import co.astrnt.qasdk.utils.FileUtils;
import co.astrnt.qasdk.utils.LogUtil;
import co.astrnt.qasdk.utils.ServiceUtils;
import co.astrnt.qasdk.utils.services.SendLogService;
import co.astrnt.qasdk.videocompressor.VideoCompress;
import io.reactivex.annotations.Nullable;
import timber.log.Timber;

public class VideoCompressService extends Service {

    public static final String EXT_QUESTION_ID = "VideoCompressService.QuestionId";
    public static final String EXT_PATH = "VideoCompressService.Path";

    public static final long NOTIFY_INTERVAL = 60 * 1000;

    private File inputFile, outputFile;
    private String inputPath, outputPath;
    private long questionId;

    private Context context;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Timer mTimer = null;
    private InterviewApiDao currentInterview;
    private QuestionApiDao currentQuestion;
    private AstrntSDK astrntSDK;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int mNotificationId;

    private int counter = 0;
    private int totalQuestion;

    public static void start(Context context, String inputPath, long questionId) {
        Intent intent = new Intent(context, VideoCompressService.class)
                .putExtra(EXT_PATH, inputPath)
                .putExtra(EXT_QUESTION_ID, questionId);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            inputPath = intent.getStringExtra(EXT_PATH);
            questionId = intent.getLongExtra(EXT_QUESTION_ID, 0);

            inputFile = new File(inputPath);
            currentInterview = astrntSDK.getCurrentInterview();
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
        mTimer.scheduleAtFixedRate(new VideoCompressService.TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    private void startServiceOreoCondition() {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotification("Compress Video");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void doCompress() {

        List<QuestionApiDao> allVideoQuestion = astrntSDK.getAllVideoQuestion();

        if (currentQuestion != null) {

            if (!inputFile.exists()) {
                astrntSDK.getVideoFile(context, currentInterview.getInterviewCode(), currentQuestion.getId());
                stopService();
            } else {

                File directory = FileUtils.makeAndGetSubDirectory(context, currentInterview.getInterviewCode(), "video");
                if (!directory.exists()) {
                    directory.mkdir();
                }
                outputFile = new File(directory, currentQuestion.getId() + ".mp4");
                outputPath = outputFile.getAbsolutePath();

                LogUtil.addNewLog(currentInterview.getInterviewCode(),
                        new LogDao("Compress Video " + currentQuestion.getId(),
                                String.format("Start Compress, src file : %s \n output file %s", inputPath, outputPath)
                        )
                );

                counter = 0;
                totalQuestion = allVideoQuestion.size();

                for (int i = 0; i < allVideoQuestion.size(); i++) {
                    QuestionApiDao item = allVideoQuestion.get(i);
                    if (item.getId() == currentQuestion.getId()) {
                        counter = i;
                    }
                }

                String compressMessage = "Compressing video " + (counter + 1) + " from " + totalQuestion;

                createNotification(compressMessage);

                VideoCompress.compressVideo(inputPath, outputPath, new VideoCompress.CompressListener() {
                    @Override
                    public void onStart() {

                        astrntSDK.updateCompressing(currentQuestion);

                        LogUtil.addNewLog(currentInterview.getInterviewCode(),
                                new LogDao("Video Compress (Start) " + currentQuestion.getId(),
                                        "Available storage " + astrntSDK.getAvailableStorage() + "Mb"
                                )
                        );

                        Timber.d("Video Compress compress START %s %s", inputPath, outputPath);
                    }

                    @Override
                    public void onSuccess() {
                        long availableStorage = astrntSDK.getAvailableStorage();

                        Timber.d("Video Compress compress %s %s %s", inputPath, outputPath, "SUCCESS");
                        Timber.d("Video Compress compress Available Storage %d", availableStorage);

                        double fileSizeInKb = (double) (outputFile.length() / 1024);
                        long fileSizeInMb = (long) (fileSizeInKb / 1024);

                        Timber.d("Video Compress compress output File size %d", outputFile.length());

                        String message = "Compress completed";
                        if (fileSizeInKb < 150) {

                            outputFile.delete();


                            LogUtil.addNewLog(currentInterview.getInterviewCode(),
                                    new LogDao("Video Compress (Success) " + currentQuestion.getId(),
                                            "But, File is corrupt or too small " + fileSizeInKb + "Kb. Compressed file will be deleted."
                                    )
                            );
                            astrntSDK.markAsPending(currentQuestion, inputPath);

                            message = "Video Compress (Success), but file is corrupt or too small";

                        } else {

                            inputFile.delete();
                            astrntSDK.updateVideoPath(currentQuestion, outputPath);
                            LogUtil.addNewLog(currentInterview.getInterviewCode(),
                                    new LogDao("Video Compress (Success) " + currentQuestion.getId(),
                                            "Success, file compressed size: " + fileSizeInMb + "Mb, available storage "
                                                    + astrntSDK.getAvailableStorage() + "Mb."
                                                    + "Raw File has been deleted"
                                    )
                            );

                            if (astrntSDK.isShowUpload()) {
                                EventBus.getDefault().post(new CompressEvent());
                            } else {
                                if (!ServiceUtils.isMyServiceRunning(context, SingleVideoUploadService.class)) {
                                    SingleVideoUploadService.start(context, questionId);
                                }
                            }
                        }

                        mBuilder.setContentText(message)
                                .setProgress(0, 0, false)
                                .setOngoing(false)
                                .setAutoCancel(true);

                        mNotifyManager.notify(mNotificationId, mBuilder.build());
                        mNotifyManager.cancel(mNotificationId);
                        stopService();
                    }

                    @Override
                    public void onFail() {
                        astrntSDK.markAsPending(currentQuestion, inputPath);

                        String errorMsg = String.format("Video Compress FAILED Available Storage %d", astrntSDK.getAvailableStorage());

                        mBuilder.setContentText(errorMsg)
                                .setProgress(0, 0, false)
                                .setOngoing(false)
                                .setAutoCancel(true);

                        mNotifyManager.notify(mNotificationId, mBuilder.build());

                        Timber.e("Video Compress %s %s %s", inputPath, outputPath, "FAILED");
                        Timber.e(errorMsg);

                        LogUtil.addNewLog(currentInterview.getInterviewCode(),
                                new LogDao("Video Compress (Fail) " + currentQuestion.getId(),
                                        errorMsg
                                )
                        );

                        stopService();
                    }

                    @Override
                    public void onProgress(float percent) {
                        mBuilder.setProgress(100, (int) percent, false);
                        // Displays the progress bar for the first time.
                        mNotifyManager.notify(mNotificationId, mBuilder.build());
                    }
                });
            }
        } else {
            stopService();
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
            CharSequence name = "Video Compress";
            String description = "Astronaut Q&A Video Compress";
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

    private void sendLog() {
        if (!ServiceUtils.isMyServiceRunning(context, SendLogService.class)) {
            SendLogService.start(context);
        }
    }

    public void stopService() {
        sendLog();
        mTimer.cancel();
        if (mNotifyManager != null) mNotifyManager.cancelAll();
        stopSelf();
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(() -> {
                if (currentQuestion != null) {
                    if (currentQuestion.getUploadStatus().equals(UploadStatusType.PENDING)) {
                        doCompress();
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