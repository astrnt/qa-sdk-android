package co.astrnt.qasdk;

import android.content.Context;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.InformationApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.type.UploadStatusType;
import co.astrnt.qasdk.utils.QuestionInfo;
import io.realm.Realm;
import timber.log.Timber;

public class AstrntSDK {

    private static Realm realm;
    private static AstronautApi mAstronautApi;
    private static String mApiUrl;
    private boolean isDebuggable;

    public AstrntSDK(Context context, String apiUrl, boolean debug) {
        mApiUrl = apiUrl;
        isDebuggable = debug;

        if (debug) {
            Timber.plant(new Timber.DebugTree());
        }
        Realm.init(context);
        realm = Realm.getDefaultInstance();
    }

    public static Realm getRealm() {
        return realm;
    }

    public static void saveInterviewResult(InterviewResultApiDao interviewResult) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(interviewResult.getInterview());
            if (interviewResult.getInformation() != null) {
                realm.copyToRealmOrUpdate(interviewResult.getInformation());
            }
            if (interviewResult.getInvitation_video() != null) {
                realm.copyToRealmOrUpdate(interviewResult.getInvitation_video());
            }
            realm.commitTransaction();
        }

        if (interviewResult.getInterview().getQuestions() != null) {
            saveQuestionInfo();
        }
    }

    public static void saveInterview(InterviewApiDao interview) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(interview);
            realm.commitTransaction();
        }
    }

    public static void saveQuestionInfo() {
        QuestionInfo questionInfo = new QuestionInfo(getQuestionIndex(), getQuestionAttempt());
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        }
    }

    public static QuestionInfo getQuestionInfo() {
        return realm.where(QuestionInfo.class).findFirst();
    }

    public static int getQuestionIndex() {
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
        QuestionApiDao currentQuestion = getCurrentQuestion();
        if (questionInfo != null) {
            if (questionInfo.getAttempt() > 0) {
                return questionInfo.getAttempt();
            } else {
                QuestionApiDao nextQuestion = getNextQuestion();
                if (nextQuestion != null) {
                    return getNextQuestion().getTakesCount();
                } else {
                    return 3;
                }
            }
        } else {
            InformationApiDao information = realm.where(InformationApiDao.class).findFirst();
            assert information != null;
            return currentQuestion.getTakesCount() - information.getInterviewAttempt();
        }
    }

    public static QuestionApiDao searchQuestionById(long id) {
        return realm.where(QuestionApiDao.class).equalTo("id", id).findFirst();
    }

    public static QuestionApiDao getCurrentQuestion() {
        InterviewApiDao interviewApiDao = realm.where(InterviewApiDao.class).findFirst();
        assert interviewApiDao != null;
        int questionIndex = getQuestionIndex();
        if (questionIndex < interviewApiDao.getQuestions().size()) {
            return interviewApiDao.getQuestions().get(questionIndex);
        } else {
            return interviewApiDao.getQuestions().last();
        }
    }

    public static QuestionApiDao getNextQuestion() {
        InterviewApiDao interviewApiDao = realm.where(InterviewApiDao.class).findFirst();
        assert interviewApiDao != null;
        int questionIndex = getQuestionIndex();
        if (questionIndex < interviewApiDao.getQuestions().size()) {
            return interviewApiDao.getQuestions().get(questionIndex);
        } else {
            return interviewApiDao.getQuestions().last();
        }
    }

    public static void increaseQuestionIndex() {
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
                increaseQuestionIndex();
            } else {
                realm.copyToRealmOrUpdate(questionInfo);
                realm.commitTransaction();
            }
        }
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

    public AstronautApi getApi() {
        if (mAstronautApi == null) {
            mAstronautApi = new AstronautApi(mApiUrl, isDebuggable);
        }
        return mAstronautApi;
    }

}
