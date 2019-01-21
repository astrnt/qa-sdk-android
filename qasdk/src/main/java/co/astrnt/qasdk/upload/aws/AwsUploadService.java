package co.astrnt.qasdk.upload.aws;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.R;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.event.UploadEvent;
import co.astrnt.qasdk.utils.LogUtil;
import timber.log.Timber;

public class AwsUploadService extends Service {

    public static final String INTENT_KEY_QUESTION = "questionId";
    private TransferUtility transferUtility;
    private AstrntSDK astrntSDK;
    private QuestionApiDao currentQuestion;
    private InterviewApiDao interviewApiDao;

    private Context context;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int mNotificationId;

    private int counter = 0;
    private int totalQuestion;

    public static void start(Context context, long questionId) {
        Intent intent = new Intent(context, AwsUploadService.class);
        intent.putExtra(INTENT_KEY_QUESTION, questionId);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        astrntSDK = new AstrntSDK();

        AwsUtil util = new AwsUtil();
        transferUtility = util.getTransferUtility(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final long questionId = intent.getLongExtra(INTENT_KEY_QUESTION, 0);

        interviewApiDao = astrntSDK.getCurrentInterview();
        currentQuestion = astrntSDK.searchQuestionById(questionId);
        final InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        List<QuestionApiDao> allVideoQuestion = astrntSDK.getAllVideoQuestion();

        final File file = new File(currentQuestion.getVideoPath());
        final String key = file.getName();

        if (currentQuestion != null) {

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

            return START_STICKY;
        } else {
            return START_NOT_STICKY;
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

    private class UploadListener implements TransferListener {

        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
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

            if (state == TransferState.COMPLETED) {
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

                stopSelf();
            } else if (state == TransferState.RESUMED_WAITING) {

                mBuilder.setSmallIcon(R.drawable.ic_cloud_off_white_24dp)
                        .setProgress(0, 0, false);

                LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                        new LogDao("Background Upload (Resumed)",
                                "Upload Resumed for question id " + currentQuestion.getId()
                        )
                );
            } else if (state == TransferState.PAUSED) {

                mBuilder.setSmallIcon(R.drawable.ic_cloud_off_white_24dp)
                        .setProgress(0, 0, false);

                LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                        new LogDao("Background Upload (Paused)",
                                "Upload paused for question id " + currentQuestion.getId()
                        )
                );
            } else if (state == TransferState.CANCELED) {
                astrntSDK.markAsCompressed(currentQuestion);

                LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                        new LogDao("Background Upload (Canceled)",
                                "Upload canceled for question id " + currentQuestion.getId()
                        )
                );

                mBuilder.setSmallIcon(R.drawable.ic_cloud_off_white_24dp)
                        .setProgress(0, 0, false)
                        .setContentText("Upload Canceled");
                stopSelf();
            } else if (state == TransferState.PENDING_NETWORK_DISCONNECT
                    || state == TransferState.WAITING_FOR_NETWORK) {

                astrntSDK.markAsCompressed(currentQuestion);

                LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                        new LogDao("Background Upload (Pending)",
                                "Upload pending for question id " + currentQuestion.getId()
                        )
                );

                mBuilder.setSmallIcon(R.drawable.ic_cloud_off_white_24dp)
                        .setProgress(0, 0, false)
                        .setContentText("Upload pending, not connected to internet");
                stopSelf();
            }

            mBuilder.setOngoing(false)
                    .setAutoCancel(true);

            mNotifyManager.notify(mNotificationId, mBuilder.build());

            EventBus.getDefault().post(new UploadEvent());
        }
    }
}
