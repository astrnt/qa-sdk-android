package co.astrnt.qasdk;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.InformationApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.MultipleAnswerApiDao;
import co.astrnt.qasdk.dao.PrevQuestionStateApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.type.InterviewType;
import co.astrnt.qasdk.type.UploadStatusType;
import co.astrnt.qasdk.utils.QuestionInfo;
import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

public class AstrntSDK {

    private static AstronautApi mAstronautApi;
    private static String mApiUrl;
    private static boolean isPractice = false;
    private Realm realm;
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

    public AstrntSDK() {
        this.realm = Realm.getDefaultInstance();
    }

    private static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public Realm getRealm() {
        return realm;
    }

    public String getApiUrl() {
        return mApiUrl;
    }

    public void saveInterviewResult(InterviewResultApiDao interviewResult, InterviewApiDao interviewApiDao) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            if (interviewResult.getInformation() != null) {
                realm.copyToRealmOrUpdate(interviewResult.getInformation());
            }
            if (interviewResult.getInvitation_video() != null) {
                realm.copyToRealmOrUpdate(interviewResult.getInvitation_video());
            }
            realm.commitTransaction();
            saveInterview(interviewApiDao, interviewResult.getToken(), interviewResult.getInterview_code());
            if (interviewResult.getInformation() != null) {
                updateInterview(getCurrentInterview(), interviewResult.getInformation());
            }
        }

        if (interviewResult.getInterview().getQuestions() != null) {
            saveQuestionInfo();
        }
    }

    public void saveInterview(InterviewApiDao interview, String token, String interviewCode) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            interview.setToken(token);
            interview.setInterviewCode(interviewCode);
            realm.copyToRealmOrUpdate(interview);
            realm.commitTransaction();
        }
    }

    private void updateInterview(InterviewApiDao interview, InformationApiDao informationApiDao) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            RealmList<QuestionApiDao> questions = interview.getQuestions();
            for (QuestionApiDao question : questions) {
                for (PrevQuestionStateApiDao questionState : informationApiDao.getPrevQuestStates()) {
                    if (question.getId() == questionState.getQuestionId()) {
                        if (questionState.isAnswered()) {
                            question.setAnswered(true);
                        } else {
                            question.setAnswered(false);
                        }
                        question.setTimeLeft(questionState.getDurationLeft());
                    }
                }
            }

            interview.setQuestions(questions);

            realm.copyToRealmOrUpdate(interview);
            realm.commitTransaction();

            updateQuestionInfo(informationApiDao.getInterviewIndex(), informationApiDao.getInterviewAttempt());
        }
    }

    public void updateQuestionTimeLeft(QuestionApiDao currentQuestion, int timeLeft) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            currentQuestion.setTimeLeft(timeLeft);
            realm.copyToRealmOrUpdate(currentQuestion);
            realm.commitTransaction();
        }
    }

    public void updateInterviewTimeLeft(int timeLeft) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            InterviewApiDao interviewApiDao = getCurrentInterview();
            interviewApiDao.setDuration_left(timeLeft);
            realm.copyToRealmOrUpdate(interviewApiDao);
            realm.commitTransaction();
        }
    }

    private void saveQuestionInfo() {
        QuestionInfo questionInfo = new QuestionInfo(getQuestionIndex(), getQuestionAttempt(), false);
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        }
    }

    private void updateQuestionInfo(int questionIndex, int questionAttempt) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            QuestionApiDao currentQuestion = getQuestionByIndex(questionIndex);
            questionAttempt = currentQuestion.getTakesCount() - questionAttempt;
            QuestionInfo questionInfo = new QuestionInfo(questionIndex, questionAttempt, false);
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        }
    }

    private QuestionInfo getQuestionInfo() {
        return realm.where(QuestionInfo.class).equalTo("isPractice", isPractice()).findFirst();
    }

    private InformationApiDao getInformation() {
        return realm.where(InformationApiDao.class).findFirst();
    }

    public boolean isResume() {
        InformationApiDao informationApiDao = getInformation();
        return informationApiDao.getPrevQuestStates() != null && !informationApiDao.getPrevQuestStates().isEmpty();
    }

    public int getQuestionIndex() {
        if (isPractice()) {
            return 0;
        }
        QuestionInfo questionInfo = getQuestionInfo();
        if (questionInfo != null) {
            return questionInfo.getIndex();
        } else {
            InformationApiDao information = getInformation();
            if (information == null) {
                return 0;
            } else {
                InterviewApiDao interviewApiDao = getCurrentInterview();
                if (interviewApiDao.getType().equals(InterviewType.OPEN_TEST) ||
                        interviewApiDao.getType().equals(InterviewType.CLOSE_TEST)) {

                    for (int i = 0; i < information.getPrevQuestStates().size(); i++) {
                        PrevQuestionStateApiDao prevQuestionState = information.getPrevQuestStates().get(i);
                        assert prevQuestionState != null;
                        if (prevQuestionState.getDurationLeft() > 0) {
                            updateQuestion(interviewApiDao, prevQuestionState);
                            return i;
                        }
                    }

                    return information.getInterviewIndex();
                } else {
                    return information.getInterviewIndex();
                }
            }
        }
    }

    private void updateQuestion(InterviewApiDao interview, PrevQuestionStateApiDao questionState) {
        for (QuestionApiDao question : interview.getQuestions()) {
            if (question.getId() == questionState.getQuestionId()) {
                if (!realm.isInTransaction()) {
                    realm.beginTransaction();
                    if (questionState.isAnswered()) {
                        question.setAnswered(true);
                    } else {
                        question.setAnswered(false);
                    }
                    question.setTimeLeft(questionState.getDurationLeft());

                    realm.copyToRealmOrUpdate(question);
                    realm.commitTransaction();
                }
            }
        }
    }

    public int getQuestionAttempt() {
        QuestionInfo questionInfo = getQuestionInfo();
        if (questionInfo != null) {
            return questionInfo.getAttempt();
        } else {
            InformationApiDao information = getInformation();
            if (information == null) {
                return 1;
            } else {
                QuestionApiDao currentQuestion = getCurrentQuestion();
                if (currentQuestion != null) {
                    return currentQuestion.getTakesCount() - information.getInterviewAttempt();
                } else {
                    return 1;
                }
            }
        }
    }

    public QuestionApiDao searchQuestionById(long id) {
        return realm.where(QuestionApiDao.class).equalTo("id", id).findFirst();
    }

    public InterviewApiDao getCurrentInterview() {
        return realm.where(InterviewApiDao.class).findFirst();
    }

    public int getTotalQuestion() {
        if (isPractice()) {
            return 1;
        }
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            return interviewApiDao.getQuestions().size();
        } else {
            return 0;
        }
    }

    public boolean isAllUploaded() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao == null) {
            return true;
        } else {
            if (interviewApiDao.getQuestions().isEmpty()) {
                return true;
            } else {
                int totalQuestion = getTotalQuestion();
                return getQuestionIndex() >= totalQuestion && getQuestionAttempt() == 0;
            }
        }
    }

    public boolean isCanContinue() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        return interviewApiDao != null && !interviewApiDao.isFinished() && interviewApiDao.getCandidate() != null && isNotLastQuestion();
    }

    private QuestionApiDao getPracticeQuestion() {
        QuestionApiDao questionApiDao = new QuestionApiDao();
        questionApiDao.setTakesCount(3);
        questionApiDao.setTitle("What are your proudest achievements, and why?");
        return questionApiDao;
    }

    public QuestionApiDao getCurrentQuestion() {
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

    private QuestionApiDao getQuestionByIndex(int questionIndex) {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        assert interviewApiDao != null;
        if (questionIndex < interviewApiDao.getQuestions().size()) {
            return interviewApiDao.getQuestions().get(questionIndex);
        } else {
            return interviewApiDao.getQuestions().last();
        }
    }

    private QuestionApiDao getNextQuestion() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        assert interviewApiDao != null;
        int questionIndex = getQuestionIndex();
        if (questionIndex < interviewApiDao.getQuestions().size()) {
            return interviewApiDao.getQuestions().get(questionIndex);
        } else {
            return interviewApiDao.getQuestions().last();
        }
    }

    public void increaseQuestionIndex() {
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

    public void decreaseQuestionAttempt() {

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

    public boolean isLastAttempt() {
        return getQuestionAttempt() == 0;
    }

    public boolean isNotLastQuestion() {
        return getQuestionIndex() < getTotalQuestion();
    }

    public void updateVideoPath(QuestionApiDao questionApiDao, String videoPath) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setVideoPath(videoPath);
            questionApiDao.setUploadStatus(UploadStatusType.COMPRESSED);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();
        }
    }

    public void updateProgress(QuestionApiDao questionApiDao, double progress) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadProgress(progress);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s has been uploaded", questionApiDao.getId());
        }
    }

    public void markUploading(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.UPLOADING);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s is now uploading", questionApiDao.getId());
        }
    }

    public void markNotAnswer(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.NOT_ANSWER);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s is now uploading", questionApiDao.getId());
        }
    }

    public void markUploaded(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.UPLOADED);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s has been uploaded", questionApiDao.getId());
        }
    }

    public void markAsCompressed(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.COMPRESSED);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s mark as pending", questionApiDao.getId());
        }
    }

    public void markAsPending(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.PENDING);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s mark as pending", questionApiDao.getId());
        }
    }

    public void clearDb() {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
        }
    }

    public boolean isPractice() {
        return isPractice;
    }

    public void setInterviewFinished() {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            InterviewApiDao interviewApiDao = getCurrentInterview();
            interviewApiDao.setFinished(true);
            realm.copyToRealmOrUpdate(interviewApiDao);
            realm.commitTransaction();

            Timber.d("Interview marked as finished in local");
        }
    }

    public void setPracticeMode() {
        isPractice = true;
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            QuestionInfo questionInfo = new QuestionInfo(0, 3, isPractice);
            questionInfo.setId(20180427);
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        }
    }

    public void finishPracticeMode() {
        isPractice = false;
    }

    public long getAvailableMemory() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        long megAvailable = bytesAvailable / (1024 * 1024);
        Timber.d("Available MB : %s", megAvailable);
        return megAvailable;
    }

    public void addSelectedAnswer(QuestionApiDao questionApiDao, MultipleAnswerApiDao answer) {
        if (!realm.isInTransaction()) {

            RealmList<MultipleAnswerApiDao> selectedAnswer = questionApiDao.getSelectedAnswer();
            realm.beginTransaction();

            if (answer.isSelected()) {
                selectedAnswer.remove(answer);
            } else {
                selectedAnswer.add(answer);
            }

            RealmList<MultipleAnswerApiDao> multipleAnswer = questionApiDao.getMultiple_answers();
            for (MultipleAnswerApiDao item : multipleAnswer) {
                if (questionApiDao.isMultipleChoice()) {
                    if (item.getId() == answer.getId()) {
                        item.setSelected(!answer.isSelected());
                    }
                } else {
                    if (item.getId() == answer.getId()) {
                        item.setSelected(!answer.isSelected());
                    } else {
                        item.setSelected(false);
                    }
                }
            }

            questionApiDao.setSelectedAnswer(selectedAnswer);
            questionApiDao.setMultiple_answers(multipleAnswer);
            if (selectedAnswer.isEmpty()) {
                questionApiDao.setAnswered(false);
            } else {
                questionApiDao.setAnswered(true);
            }
            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();
        }
    }

    @NonNull
    private OkHttpClient getOkHttpClient() {

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.followRedirects(true);
        httpClientBuilder.followSslRedirects(true);
        httpClientBuilder.retryOnConnectionFailure(true);
        httpClientBuilder.writeTimeout(60, TimeUnit.SECONDS);
        httpClientBuilder.readTimeout(60, TimeUnit.SECONDS);
        httpClientBuilder.connectTimeout(60, TimeUnit.SECONDS);

        if (isDebuggable) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClientBuilder.addInterceptor(loggingInterceptor);
        }

        httpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .addHeader("device", "android")
                        .addHeader("os", "value")
                        .addHeader("browser", "")
                        .addHeader("screenresolution", getScreenWidth() + "x" + getScreenHeight())
                        .build();
                return chain.proceed(request);
            }
        });

        return httpClientBuilder.build();
    }

    public AstronautApi getApi() {
        if (mAstronautApi == null) {
            mAstronautApi = new AstronautApi(mApiUrl, isDebuggable);
        }
        return mAstronautApi;
    }

}
