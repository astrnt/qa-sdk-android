package co.astrnt.qasdk;

import android.content.Context;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.util.concurrent.TimeUnit;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.InformationApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.type.UploadStatusType;
import co.astrnt.qasdk.utils.QuestionInfo;
import io.realm.Realm;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class AstrntSDK {

    private static Realm realm;
    private static AstronautApi mAstronautApi;
    private static String mApiUrl;
    private static boolean isPractice = false;
    private boolean isDebuggable;

    public AstrntSDK(Context context, String apiUrl, boolean debug, String appId) {
        mApiUrl = apiUrl;
        isDebuggable = debug;

        if (debug) {
            Timber.plant(new Timber.DebugTree());
        }
        Realm.init(context);
        realm = Realm.getDefaultInstance();

        UploadService.NAMESPACE = appId;
        UploadService.HTTP_STACK = new OkHttpStack(getOkHttpClient());
        UploadService.BACKOFF_MULTIPLIER = 2;
        UploadService.IDLE_TIMEOUT = 10 * 1000;
        UploadService.KEEP_ALIVE_TIME_IN_SECONDS = 3 * 60 * 1000;
        UploadService.INITIAL_RETRY_WAIT_TIME = 10 * 1000;
        UploadService.MAX_RETRY_WAIT_TIME = 10 * 1000;
    }

    public static Realm getRealm() {
        return realm;
    }

    public static String getApiUrl() {
        return mApiUrl;
    }

    public static void saveInterviewResult(InterviewResultApiDao interviewResult) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            if (interviewResult.getInformation() != null) {
                realm.copyToRealmOrUpdate(interviewResult.getInformation());
            }
            if (interviewResult.getInvitation_video() != null) {
                realm.copyToRealmOrUpdate(interviewResult.getInvitation_video());
            }
            realm.commitTransaction();
            saveInterview(interviewResult.getInterview(), interviewResult.getToken(), interviewResult.getInterview_code());
        }

        if (interviewResult.getInterview().getQuestions() != null) {
            saveQuestionInfo();
        }
    }

    public static void saveInterview(InterviewApiDao interview, String token, String interviewCode) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            interview.setToken(token);
            interview.setInterviewCode(interviewCode);
            realm.copyToRealmOrUpdate(interview);
            realm.commitTransaction();
        }
    }

    public static void saveQuestionInfo() {
        QuestionInfo questionInfo = new QuestionInfo(getQuestionIndex(), getQuestionAttempt(), false);
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        }
    }

    public static QuestionInfo getQuestionInfo() {
        return realm.where(QuestionInfo.class).equalTo("isPractice", isPractice()).findFirst();
    }

    public static int getQuestionIndex() {
        if (isPractice()) {
            return 0;
        }
        QuestionInfo questionInfo = getQuestionInfo();
        if (questionInfo != null) {
            return questionInfo.getIndex();
        } else {
            InformationApiDao information = realm.where(InformationApiDao.class).findFirst();
            assert information != null;
            return information.getInterviewIndex();
        }
    }

    public static int getQuestionAttempt() {
        QuestionInfo questionInfo = getQuestionInfo();
        if (questionInfo != null) {
            return questionInfo.getAttempt();
        } else {
            InformationApiDao information = realm.where(InformationApiDao.class).findFirst();
            assert information != null;
            QuestionApiDao currentQuestion = getCurrentQuestion();
            if (currentQuestion != null) {
                return currentQuestion.getTakesCount() - information.getInterviewAttempt();
            } else {
                return 1;
            }
        }
    }

    public static QuestionApiDao searchQuestionById(long id) {
        return realm.where(QuestionApiDao.class).equalTo("id", id).findFirst();
    }

    public static InterviewApiDao getCurrentInterview() {
        return realm.where(InterviewApiDao.class).findFirst();
    }

    public static int getTotalQuestion() {
        if (isPractice()) {
            return 1;
        }
        InterviewApiDao interviewApiDao = getCurrentInterview();
        assert interviewApiDao != null;
        return interviewApiDao.getQuestions().size();
    }

    public static QuestionApiDao getPracticeQuestion() {
        QuestionApiDao questionApiDao = new QuestionApiDao();
        questionApiDao.setTakesCount(3);
        questionApiDao.setTitle("What are your proudest achievements, and why?");
        return questionApiDao;
    }

    public static QuestionApiDao getCurrentQuestion() {
        if (isPractice()) {
            return getPracticeQuestion();
        }
        InterviewApiDao interviewApiDao = getCurrentInterview();
        assert interviewApiDao != null;
        int questionIndex = getQuestionIndex();
        if (questionIndex < interviewApiDao.getQuestions().size()) {
            return interviewApiDao.getQuestions().get(questionIndex);
        } else {
            return interviewApiDao.getQuestions().last();
        }
    }

    public static QuestionApiDao getNextQuestion() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        assert interviewApiDao != null;
        int questionIndex = getQuestionIndex();
        if (questionIndex < interviewApiDao.getQuestions().size()) {
            return interviewApiDao.getQuestions().get(questionIndex);
        } else {
            return interviewApiDao.getQuestions().last();
        }
    }

    public static void increaseQuestionIndex() {
        if (isPractice()) {
            return;
        }
        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            QuestionInfo questionInfo = getQuestionInfo();
            questionInfo.increaseIndex();

            QuestionApiDao nextQuestion = getNextQuestion();
            if (nextQuestion != null) {
                questionInfo.setAttempt(nextQuestion.getTakesCount());
            } else {
                questionInfo.resetAttempt();
            }

            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        }
    }

    public static void decreaseQuestionAttempt() {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            QuestionInfo questionInfo = getQuestionInfo();
            questionInfo.decreaseAttempt();
            int attempt = questionInfo.getAttempt();

            if (attempt <= 0) {
                realm.commitTransaction();
            } else {
                realm.copyToRealmOrUpdate(questionInfo);
                realm.commitTransaction();
            }
        }
    }

    public static boolean isLastAttempt() {
        return getQuestionAttempt() == 0;
    }

    public static boolean isLastQuestion() {
        return getQuestionIndex() == getTotalQuestion() - 1;
    }

    public static void updateVideoPath(QuestionApiDao questionApiDao, String videoPath) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setVideoPath(videoPath);
            questionApiDao.setUploadStatus(UploadStatusType.PENDING);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();
        }
    }

    public static void markUploaded(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.UPLOADED);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();
        }
    }

    public static void clearDb() {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
        }
    }

    public static boolean isPractice() {
        return isPractice;
    }

    public static void setPracticeMode() {
        isPractice = true;
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            QuestionInfo questionInfo = new QuestionInfo(0, 3, isPractice);
            questionInfo.setId(20180427);
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        }
    }

    public static void finishPracticeMode() {
        isPractice = false;
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public AstronautApi getApi() {
        if (mAstronautApi == null) {
            mAstronautApi = new AstronautApi(mApiUrl, isDebuggable);
        }
        return mAstronautApi;
    }

}
