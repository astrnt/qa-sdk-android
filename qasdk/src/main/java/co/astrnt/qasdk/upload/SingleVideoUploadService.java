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

    public static final String EXT_QUESTION_ID = "SingleVideoUploadService.QuestionId";

    public static final long NOTIFY_INTERVAL = 2 * 60 * 1000;

    private long questionId;

    private Context context;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    private QuestionApiDao currentQuestion;
    private AstrntSDK astrntSDK;

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

            currentQuestion = astrntSDK.searchQuestionById(questionId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        astrntSDK = new AstrntSDK();

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

            if (currentQuestion.getUploadStatus().equals(UploadStatusType.NOT_ANSWER) ||
                    currentQuestion.getUploadStatus().equals(UploadStatusType.UPLOADED)) {
                stopService();
                return;
            }

            astrntSDK.markUploading(currentQuestion);

            InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
            String uploadId = new MultipartUploadRequest(context, astrntSDK.getApiUrl() + "video/upload")
                    .addHeader("token", interviewApiDao.getToken())
                    .addParameter("interview_code", interviewApiDao.getInterviewCode())
                    .addParameter("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()))
                    .addParameter("company_id", String.valueOf(interviewApiDao.getCompany().getId()))
                    .addParameter("question_id", String.valueOf(questionId))
                    .addParameter("job_id", String.valueOf(interviewApiDao.getJob().getId()))
                    .addParameter("device", "android")
                    .addParameter("device_type", Build.MODEL)
                    .addFileToUpload(new File(currentQuestion.getVideoPath()).getAbsolutePath(), "interview_video")
                    .setUtf8Charset()
                    .setNotificationConfig(notificationConfig)
                    .setAutoDeleteFilesAfterSuccessfulUpload(false)
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
                            Timber.e("Video Upload Error : %s", exception.getMessage());
                            if (serverResponse != null && serverResponse.getBody() != null) {
                                BaseApiDao baseApiDao = new Gson().fromJson(serverResponse.getBodyAsString(), BaseApiDao.class);
                                Timber.e(baseApiDao.getMessage());
                                astrntSDK.markAsCompressed(currentQuestion);
                            }
                            stopService();
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            astrntSDK.markUploaded(currentQuestion);
                            stopService();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            Timber.e("Video Upload Canceled");
                            astrntSDK.markAsCompressed(currentQuestion);
                            stopService();
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