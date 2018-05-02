package co.astrnt.qasdk.upload;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import com.google.gson.Gson;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.type.UploadStatusType;
import io.reactivex.annotations.Nullable;
import timber.log.Timber;

public class SingleVideoUploadService extends Service {

    public static final String EXT_QUESTION_ID = "VideoCompressService.QuestionId";

    public static final long NOTIFY_INTERVAL = 2 * 60 * 1000;

    private long questionId;

    private Context context;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    private QuestionApiDao currentQuestion;

    public static void start(Context context, long questionId) {
        context.startService(
                new Intent(context, SingleVideoUploadService.class)
                        .putExtra(EXT_QUESTION_ID, questionId)
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            questionId = intent.getLongExtra(EXT_QUESTION_ID, 0);

            currentQuestion = AstrntSDK.searchQuestionById(questionId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        if (mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
        }
        mTimer.scheduleAtFixedRate(new SingleVideoUploadService.TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void doUploadVideo() {
        try {
            UploadNotificationConfig notificationConfig = new UploadNotificationConfig();
            notificationConfig.setRingToneEnabled(false);

            InterviewApiDao interviewApiDao = AstrntSDK.getCurrentInterview();
            String uploadId = new MultipartUploadRequest(context, AstrntSDK.getApiUrl() + "video/upload")
                    .addParameter("token", interviewApiDao.getToken())
                    .addParameter("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()))
                    .addParameter("company_id", String.valueOf(interviewApiDao.getCompany().getId()))
                    .addParameter("question_id", String.valueOf(questionId))
                    .addParameter("job_id", String.valueOf(interviewApiDao.getJob().getId()))
                    .addParameter("device", "android")
                    .addParameter("device_type", Build.MODEL)
                    .addFileToUpload(new File(currentQuestion.getVideoPath()).getAbsolutePath(), "interview_video")
                    .setUtf8Charset()
                    .setNotificationConfig(notificationConfig)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                            if (uploadInfo != null) {
                                Timber.d("Video Upload Progress : %s", uploadInfo.getProgressPercent());
                            }
                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            if (serverResponse != null && serverResponse.getBody() != null) {
                                BaseApiDao baseApiDao = new Gson().fromJson(serverResponse.getBodyAsString(), BaseApiDao.class);
                                Timber.e(baseApiDao.getMessage());
                            }
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            AstrntSDK.markUploaded(currentQuestion);
                            stopSelf();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                        }
                    }).startUpload();

            Timber.d("SingleVideoUploadService %s", uploadId);
        } catch (Exception exc) {
            Timber.d("SingleVideoUploadService %s", exc.getMessage());
        }
    }

    public void stopService() {
        mTimer.cancel();
        stopSelf();
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (currentQuestion != null) {
                        if (!currentQuestion.getUploadStatus().equals(UploadStatusType.UPLOADED)) {
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