package co.astrnt.qasdk;

import android.content.Context;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.InformationApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
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
        QuestionInfo questionInfo = new QuestionInfo(getQuestionIndex(), getCurrentQuestion());
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
        if (getQuestionInfo() != null) {
            return getQuestionInfo().getIndex();
        } else {
            InformationApiDao information = realm.where(InformationApiDao.class).findFirst();
            assert information != null;
            return information.getInterviewIndex();
        }
    }

    public static QuestionApiDao getCurrentQuestion() {
        InterviewApiDao interviewApiDao = realm.where(InterviewApiDao.class).findAll().last();
        assert interviewApiDao != null;
        if (getQuestionIndex() < interviewApiDao.getQuestions().size()) {
            return interviewApiDao.getQuestions().get(getQuestionIndex());
        } else {
            return interviewApiDao.getQuestions().last();
        }
    }

    public static QuestionApiDao getNextQuestion() {
        InterviewApiDao interviewApiDao = realm.where(InterviewApiDao.class).findAll().last();
        assert interviewApiDao != null;
        if (getQuestionIndex() < interviewApiDao.getQuestions().size()) {
            return interviewApiDao.getQuestions().get(getQuestionIndex());
        } else {
            return interviewApiDao.getQuestions().last();
        }
    }

    public static void increaseQuestionIndex() {
        QuestionInfo questionInfo = getQuestionInfo();
        questionInfo.setIndex(questionInfo.getIndex() + 1);
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        }
    }

    public static void increaseQuestionAttempt() {
        QuestionInfo questionInfo = getQuestionInfo();
        int attempt = questionInfo.getAttempt();
        QuestionApiDao currentQuestion = questionInfo.getCurrentQuestion();

        if (attempt < currentQuestion.getTakesCount()) {
            questionInfo.setAttempt(questionInfo.getAttempt() - 1);
        } else {
            QuestionApiDao nextQuestion = getNextQuestion();
            questionInfo.setIndex(questionInfo.getIndex() + 1);
            questionInfo.setAttempt(nextQuestion.getTakesCount());
            questionInfo.setCurrentQuestion(nextQuestion);
        }

        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(questionInfo);
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
