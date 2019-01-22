package co.astrnt.qasdk.upload.aws;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.R;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.event.UploadEvent;
import co.astrnt.qasdk.type.UploadStatusType;
import co.astrnt.qasdk.utils.LogUtil;
import timber.log.Timber;

public class AwsUploadService extends Service {

    public static final long NOTIFY_INTERVAL = 60 * 1000;

    public static final String EXT_QUESTION_ID = "AwsUploadService.QuestionId";
    public static final String EXT_PATH = "AwsUploadService.Path";

    private String videoPath;

    private TransferUtility transferUtility;
    private AstrntSDK astrntSDK;
    private QuestionApiDao currentQuestion;
    private InterviewApiDao interviewApiDao;

    private Context context;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int mNotificationId;

    private int counter = 0;
    private int totalQuestion;

    public static void start(Context context, String inputPath, long questionId) {
        context.startService(
                new Intent(context, AwsUploadService.class)
                        .putExtra(EXT_PATH, inputPath)
                        .putExtra(EXT_QUESTION_ID, questionId)
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        astrntSDK = new AstrntSDK();

        AwsUtil util = new AwsUtil();
        transferUtility = util.getTransferUtility(this);

        if (mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
        }
        mTimer.scheduleAtFixedRate(new AwsUploadService.TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            videoPath = intent.getStringExtra(EXT_PATH);
            long questionId = intent.getLongExtra(EXT_QUESTION_ID, 0);

            interviewApiDao = astrntSDK.getCurrentInterview();
            currentQuestion = astrntSDK.searchQuestionById(questionId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void doUploadVideo() {

        final InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        List<QuestionApiDao> allVideoQuestion = astrntSDK.getAllVideoQuestion();

        final File file = new File(videoPath);

        if (!file.exists()) {

            LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                    new LogDao("Background Upload",
                            String.format("Upload file not found. Mark not answer for Question Id : %d", currentQuestion.getId())
                    )
            );

            astrntSDK.markNotAnswer(currentQuestion);
            EventBus.getDefault().post(new UploadEvent());

            stopService();

        } else {

            if (videoPath.contains("_raw.mp4")) {
                astrntSDK.markAsPending(currentQuestion, currentQuestion.getVideoPath());

                EventBus.getDefault().post(new UploadEvent());

                stopService();

            } else {

                final String key = file.getName();

                LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                        new LogDao("Background Upload",
                                String.format("Start Upload %d, file : %s", currentQuestion.getId(), videoPath)
                        )
                );

                TransferObserver transferObserver;

                String urlBucket = String.format(astrntSDK.getAwsBucket(),
                        interviewApiDao.getCompany().getId(),
                        interviewApiDao.getJob().getId(),
                        interviewApiDao.getCandidate().getId());

                transferObserver = transferUtility.upload(urlBucket, key, file);
                transferObserver.setTransferListener(new UploadListener());

                counter = 0;
                totalQuestion = allVideoQuestion.size();

                for (int i = 0; i < allVideoQuestion.size(); i++) {
                    QuestionApiDao item = allVideoQuestion.get(i);
                    if (item.getId() == currentQuestion.getId()) {
                        counter = i;
                    }
                }

                String uploadMessage = "Uploading video " + (counter + 1) + " from " + totalQuestion;
                createNotification(uploadMessage);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification(String message) {
        mNotificationId = (int) currentQuestion.getId();

        // Make a channel if necessary
        final String channelId = "Astronaut";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "Video Upload";
            String description = "Astronaut Video Upload";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

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
                .setSmallIcon(R.drawable.ic_cloud_upload_white_24dp)
                .setContentTitle("Astronaut")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        mNotifyManager.notify(mNotificationId, mBuilder.build());
    }

    public void stopService() {
        mTimer.cancel();

        stopSelf();
    }

    private class UploadListener implements TransferListener {

        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {

            if (e != null) {
                Timber.e("AwsUploadService onError: %d% s", id, e.getMessage());

                mBuilder.setSmallIcon(R.drawable.ic_cloud_off_white_24dp)
                        .setContentText(e.getMessage())
                        .setOngoing(false)
                        .setAutoCancel(true);

                mNotifyManager.notify(mNotificationId, mBuilder.build());

                LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                        new LogDao("Background Upload (Error)",
                                "Error upload for question id " + currentQuestion.getId()
                        )
                );

            }

            EventBus.getDefault().post(new UploadEvent());
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Timber.d("AwsUploadService onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent);

            double percentage = (bytesCurrent * 100) / bytesTotal;

            mBuilder.setProgress(100, (int) percentage, false);
            mNotifyManager.notify(mNotificationId, mBuilder.build());
            astrntSDK.updateProgress(currentQuestion, percentage);

            EventBus.getDefault().post(new UploadEvent());
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Timber.d("AwsUploadService onStateChanged: %d, %s", id, state);

            switch (state) {
                case COMPLETED:
                    astrntSDK.markUploaded(currentQuestion);

                    if (counter == totalQuestion) {
                        mBuilder.setSmallIcon(R.drawable.ic_cloud_done_white_24dp)
                                .setContentText(counter + " of files uploaded, interview complete!")
                                .setProgress(100, 100, false);
                    } else {
                        mBuilder.setSmallIcon(R.drawable.ic_cloud_done_white_24dp)
                                .setContentText("Question " + (counter + 1) + " upload Completed")
                                .setProgress(100, 100, false);
                    }

                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                            new LogDao("Background Upload (Complete)",
                                    "Success uploaded for question id " + currentQuestion.getId()
                            )
                    );

                    EventBus.getDefault().post(new UploadEvent());

                    break;
                case RESUMED_WAITING:

                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                            new LogDao("Background Upload (Resumed)",
                                    "Upload Resumed for question id " + currentQuestion.getId()
                            )
                    );

                    mBuilder.setSmallIcon(R.drawable.ic_cloud_off_white_24dp)
                            .setProgress(0, 0, false)
                            .setContentText("Upload Resumed Waiting");

                    break;
                case PAUSED:

                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                            new LogDao("Background Upload (Paused)",
                                    "Upload paused for question id " + currentQuestion.getId()
                            )
                    );

                    mBuilder.setSmallIcon(R.drawable.ic_cloud_off_white_24dp)
                            .setProgress(0, 0, false)
                            .setContentText("Upload Paused");

                    break;
                case CANCELED:
                    astrntSDK.markAsCompressed(currentQuestion);

                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                            new LogDao("Background Upload (Canceled)",
                                    "Upload canceled for question id " + currentQuestion.getId()
                            )
                    );

                    mBuilder.setSmallIcon(R.drawable.ic_cloud_off_white_24dp)
                            .setProgress(0, 0, false)
                            .setContentText("Upload Canceled");

                    break;
                case PENDING_NETWORK_DISCONNECT:
                case WAITING_FOR_NETWORK:

                    astrntSDK.markAsCompressed(currentQuestion);

                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                            new LogDao("Background Upload (Pending)",
                                    "Upload pending for question id " + currentQuestion.getId()
                            )
                    );

                    mBuilder.setSmallIcon(R.drawable.ic_cloud_off_white_24dp)
                            .setProgress(0, 0, false)
                            .setContentText("Upload pending, not connected to internet");

                    break;
            }

            mBuilder.setOngoing(false)
                    .setAutoCancel(true);

            mNotifyManager.notify(mNotificationId, mBuilder.build());

            stopService();
        }
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (currentQuestion != null) {
                        if (currentQuestion.getUploadStatus().equals(UploadStatusType.COMPRESSED)) {
                            doUploadVideo();
                        } else {
                            stopService();
                        }
                    } else {
                        stopService();
                    }
                }
            });
        }
    }
}
