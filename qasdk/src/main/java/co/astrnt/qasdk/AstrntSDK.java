package co.astrnt.qasdk;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import com.downloader.PRDownloader;
import com.orhanobut.hawk.Hawk;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import co.astrnt.qasdk.constatnts.PreferenceKey;
import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.CustomFieldApiDao;
import co.astrnt.qasdk.dao.GdprDao;
import co.astrnt.qasdk.dao.InformationApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.MultipleAnswerApiDao;
import co.astrnt.qasdk.dao.PrevQuestionStateApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.dao.QuestionInfoApiDao;
import co.astrnt.qasdk.dao.QuestionInfoMcqApiDao;
import co.astrnt.qasdk.dao.SectionApiDao;
import co.astrnt.qasdk.dao.WelcomeVideoDao;
import co.astrnt.qasdk.type.InterviewType;
import co.astrnt.qasdk.type.SectionType;
import co.astrnt.qasdk.type.TestType;
import co.astrnt.qasdk.type.UploadStatusState;
import co.astrnt.qasdk.type.UploadStatusType;
import co.astrnt.qasdk.utils.HawkUtils;
import co.astrnt.qasdk.utils.LogUtil;
import co.astrnt.qasdk.utils.QuestionInfo;
import co.astrnt.qasdk.utils.SectionInfo;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

import static co.astrnt.qasdk.type.InterviewType.ASTRONAUT_PROFILE;
import static co.astrnt.qasdk.type.InterviewType.PROFILE;

public class AstrntSDK extends HawkUtils {

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

        Hawk.init(context).build();

        PRDownloader.initialize(context);

        realm = Realm.getInstance(getRealmConfig());

        UploadService.NAMESPACE = appId;
        UploadService.HTTP_STACK = new OkHttpStack(getOkHttpClient());
        UploadService.BACKOFF_MULTIPLIER = 2;
        UploadService.IDLE_TIMEOUT = 30 * 1000;
        UploadService.UPLOAD_POOL_SIZE = 1;
        UploadService.EXECUTE_IN_FOREGROUND = false;
        UploadService.BUFFER_SIZE = 1024;
    }

    public AstrntSDK() {
        this.realm = Realm.getInstance(getRealmConfig());
    }

    private static RealmConfiguration getRealmConfig() {
        return new RealmConfiguration.Builder()
                .name("astrntdb")
                .schemaVersion(BuildConfig.VERSION_CODE)
                .deleteRealmIfMigrationNeeded()
                .build();
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

    public void saveInterviewResult(InterviewResultApiDao resultApiDao, InterviewApiDao interviewApiDao, boolean isContinue) {

        InterviewApiDao newInterview = resultApiDao.getInterview();

        if (newInterview.getType().equals(ASTRONAUT_PROFILE)) {
            return;
        }

        if (newInterview.getJob().getRecruitmentType().equals("sourcing")) {
            saveSourcing(true);
        } else {
            saveSourcing(false);
        }

        boolean isProfile = newInterview.getType().contains(PROFILE);
        saveIsProfile(isProfile);

        saveSelfPace(newInterview.isSelfPace());
        savePracticeRetake(false);

        if (newInterview.getToken() != null) {
            Hawk.put(PreferenceKey.KEY_TOKEN, newInterview.getToken());
        }

        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            if (resultApiDao.getInformation() != null) {
                realm.copyToRealmOrUpdate(resultApiDao.getInformation());
            }
            if (resultApiDao.getInvitation_video() != null) {
                realm.copyToRealmOrUpdate(resultApiDao.getInvitation_video());
            }
            realm.commitTransaction();
            GdprDao gdprDao = new GdprDao(resultApiDao.getGdpr_complied(), resultApiDao.getGdpr_text(), resultApiDao.getGdpr_aggrement_text());
            saveGdprDao(gdprDao);
            if (resultApiDao.getWelcomeVideo() != null && !resultApiDao.getWelcomeVideo().getWelcomeVideoUrl().equals("")) {
                saveWelcomeVideoDao(resultApiDao.getWelcomeVideo());
            }
            if (interviewApiDao != null) {
                saveInterviewCode(resultApiDao.getInterview_code());
                saveInterview(interviewApiDao, resultApiDao.getToken(), resultApiDao.getInterview_code());
                updateSectionOrQuestionInfo(interviewApiDao);
            } else {
                saveInterviewCode(resultApiDao.getInterview_code());
                saveInterview(newInterview, resultApiDao.getToken(), resultApiDao.getInterview_code());
                updateSectionOrQuestionInfo(newInterview);
            }
            InterviewApiDao currentInterview = getCurrentInterview();
            if (resultApiDao.getInformation() != null && currentInterview != null && isContinue) {
                updateInterview(currentInterview, resultApiDao.getInformation());
            }

            currentInterview = getCurrentInterview();
            if (currentInterview != null && currentInterview.getInterviewCode() != null) {
                saveInterviewCode(currentInterview.getInterviewCode());

                if (currentInterview.getCompany() != null) {
                    Hawk.put(PreferenceKey.KEY_COMPANY_ID, String.valueOf(currentInterview.getCompany().getId()));
                }
                if (currentInterview.getCandidate() != null) {
                    Hawk.put(PreferenceKey.KEY_CANDIDATE_ID, String.valueOf(currentInterview.getCandidate().getId()));
                }
                if (currentInterview.getJob() != null) {
                    Hawk.put(PreferenceKey.KEY_JOB_ID, String.valueOf(currentInterview.getJob().getId()));
                }
            }

        } else {
            saveInterviewResult(resultApiDao, interviewApiDao, isContinue);
        }

    }

    public void updateInterviewData(InterviewApiDao currentInterview, InterviewApiDao newInterview) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            if (isSectionInterview()) {

                RealmList<SectionApiDao> sectionList = new RealmList<>();
                for (SectionApiDao newSection : newInterview.getSections()) {

                    for (SectionApiDao section : currentInterview.getSections()) {

                        if (newSection.getId() == section.getId()) {

                            if (newSection.getMedia() != null) {
                                if (section.getMedia() != null) {
                                    if (newSection.getMediaId() == section.getMediaId()) {
                                        if (section.getMedia().getOfflinePath() != null) {
                                            newSection.getMedia().setOfflinePath(section.getMedia().getOfflinePath());
                                        }
                                    }
                                }
                            }

                            RealmList<QuestionApiDao> questionList = new RealmList<>();

                            for (QuestionApiDao newQuestion : newSection.getSectionQuestions()) {
                                for (QuestionApiDao question : section.getSectionQuestions()) {
                                    if (newQuestion.getId() == question.getId()) {

                                        if (newSection.getType().equals(SectionType.INTERVIEW)) {
                                            newQuestion.setUploadStatus(question.getUploadStatus());
                                            newQuestion.setVideoPath(question.getVideoPath());
                                            newQuestion.setUploadProgress(question.getUploadProgress());
                                            newQuestion.setRetake(question.isRetake());
                                        } else {

                                            if (newQuestion.getMedia() != null) {
                                                if (question.getMedia() != null) {
                                                    if (newQuestion.getMediaId() == question.getMediaId()) {
                                                        if (question.getMedia().getOfflinePath() != null) {
                                                            newQuestion.getMedia().setOfflinePath(question.getMedia().getOfflinePath());
                                                        }
                                                    }
                                                }
                                            }

                                            newQuestion.setMediaAttemptLeft(question.getMediaAttemptLeft());
                                            newQuestion.setSelectedAnswer(question.getSelectedAnswer());
                                            newQuestion.setAnswered(question.isAnswered());
                                        }
                                    }
                                }
                                questionList.add(newQuestion);
                            }
                            newSection.setSectionQuestions(questionList);
                        }
                    }
                    sectionList.add(newSection);
                }
                if (!sectionList.isEmpty()) {
                    newInterview.setSections(sectionList);
                }
            } else {
                RealmList<QuestionApiDao> questionList = new RealmList<>();
                for (QuestionApiDao newQuestion : newInterview.getQuestions()) {
                    for (QuestionApiDao question : currentInterview.getQuestions()) {
                        if (newQuestion.getId() == question.getId()) {
                            if (newInterview.getType().equals(InterviewType.CLOSE_INTERVIEW)) {
                                newQuestion.setUploadStatus(question.getUploadStatus());
                                newQuestion.setVideoPath(question.getVideoPath());
                                newQuestion.setUploadProgress(question.getUploadProgress());
                                newQuestion.setRetake(question.isRetake());
                            } else {

                                if (newQuestion.getMedia() != null) {
                                    if (question.getMedia() != null) {
                                        if (newQuestion.getMediaId() == question.getMediaId()) {
                                            if (question.getMedia().getOfflinePath() != null) {
                                                newQuestion.getMedia().setOfflinePath(question.getMedia().getOfflinePath());
                                            }
                                        }
                                    }
                                }

                                newQuestion.setMediaAttemptLeft(question.getMediaAttemptLeft());
                                newQuestion.setSelectedAnswer(question.getSelectedAnswer());
                                newQuestion.setAnswered(question.isAnswered());
                            }
                        }
                    }
                    questionList.add(newQuestion);
                }
                newInterview.setQuestions(questionList);
            }

            realm.copyToRealmOrUpdate(newInterview);
            realm.commitTransaction();
        } else {
            updateInterviewData(currentInterview, newInterview);
        }
    }

    private void updateSectionOrQuestionInfo(InterviewApiDao interviewApiDao) {

        if (isSectionInterview()) {
            saveSectionInfo();
            saveQuestionInfo();
        }

        if (interviewApiDao.getQuestions() != null && !interviewApiDao.getQuestions().isEmpty()) {
            saveQuestionInfo();
        }
    }

    public void saveInterview(InterviewApiDao interview, String token, String interviewCode) {

        if (interview.getType().equals(ASTRONAUT_PROFILE)) {
            return;
        }

        if (interview.getJob().getRecruitmentType().equals("sourcing")) {
            saveSourcing(true);
        } else {
            saveSourcing(false);
        }

        boolean isProfile = interview.getType().contains(PROFILE);
        saveIsProfile(isProfile);

        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            interview.setToken(token);
            interview.setInterviewCode(interviewCode);
            realm.copyToRealmOrUpdate(interview);
            realm.commitTransaction();
        } else {
            saveInterview(interview, token, interviewCode);
        }
    }

    private void updateInterview(InterviewApiDao interview, InformationApiDao informationApiDao) {
        if (isSectionInterview()) {
            updateSectionInfo(informationApiDao.getSectionIndex());

            if (informationApiDao.getQuestionsInfo() != null && !informationApiDao.getQuestionsInfo().isEmpty()) {
                for (QuestionInfoApiDao questionInfoApiDao : informationApiDao.getQuestionsInfo()) {
                    updateQuestionInfo(questionInfoApiDao.getInterviewIndex(), questionInfoApiDao.getInterviewAttempt());
                }
            }
        } else {
            updateQuestionInfo(informationApiDao.getInterviewIndex(), informationApiDao.getInterviewAttempt());
        }

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            LogUtil.addNewLog(getInterviewCode(),
                    new LogDao("API Resume Information",
                            informationApiDao.toString()
                    )
            );

            if (isSectionInterview()) {

                Timber.e("Section duration Info %s", informationApiDao.toString());

                RealmList<SectionApiDao> sectionList = new RealmList<>();

                for (int i = 0; i < interview.getSections().size(); i++) {
                    SectionApiDao section = interview.getSections().get(i);
                    RealmList<QuestionApiDao> questionApiDaos = new RealmList<>();

                    if (section != null) {

                        if (i == informationApiDao.getSectionIndex() && !informationApiDao.getSectionInfo().equals("start")) {

                            if (section.getPreparationTime() >= informationApiDao.getPreparationTime()) {
                                section.setPreparationTime(informationApiDao.getPreparationTime());
                            }

                            Timber.e("Section Device duration %s", section.getDuration());
                            LogUtil.addNewLog(getInterviewCode(),
                                    new LogDao("Resume Information",
                                            "Section Device duration " + section.getDuration()
                                    )
                            );
                            LogUtil.addNewLog(getInterviewCode(),
                                    new LogDao("Resume Information",
                                            "Section API info duration " + informationApiDao.getSectionDurationLeft()
                                    )
                            );
                            LogUtil.addNewLog(getInterviewCode(),
                                    new LogDao("Resume Information",
                                            "Section background timer duration " + getLastTimer()
                                    )
                            );

                            if (section.getDuration() >= informationApiDao.getSectionDurationLeft()) {
                                Timber.e("Section duration using from info %s", informationApiDao.getSectionDurationLeft());
                                LogUtil.addNewLog(getInterviewCode(),
                                        new LogDao("Resume Information",
                                                "Section duration using from info " + informationApiDao.getSectionDurationLeft()
                                        )
                                );
                                section.setDuration(informationApiDao.getSectionDurationLeft());
                            }
                            if (section.getDuration() >= getLastTimer()) {
                                Timber.e("Section duration using from background timer %s", getLastTimer());
                                LogUtil.addNewLog(getInterviewCode(),
                                        new LogDao("Resume Information",
                                                "Section duration using from background timer " + getLastTimer()
                                        )
                                );
                                section.setDuration(getLastTimer());
                            }
                            section.setOnGoing(informationApiDao.isOnGoing());
                        }

                        if (section.getType().equals(InterviewType.INTERVIEW)) {

                            for (QuestionApiDao question : section.getSectionQuestions()) {

                                if (informationApiDao.getQuestionsInfo() != null && !informationApiDao.getQuestionsInfo().isEmpty()) {
                                    for (QuestionInfoApiDao questionInfoApiDao : informationApiDao.getQuestionsInfo()) {

                                        if (questionInfoApiDao.getPrevQuestStates() != null) {
                                            for (PrevQuestionStateApiDao questionState : questionInfoApiDao.getPrevQuestStates()) {

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
                                    }
                                }
                                questionApiDaos.add(question);
                            }
                        } else {
                            for (QuestionApiDao question : section.getSectionQuestions()) {

                                if (informationApiDao.getQuestionsMcqInfo() != null && !informationApiDao.getQuestionsMcqInfo().isEmpty()) {
                                    for (QuestionInfoMcqApiDao questionInfoMcqApiDao : informationApiDao.getQuestionsMcqInfo()) {

                                        if (question.getId() == questionInfoMcqApiDao.getId()) {

                                            if (question.getType_child().equals(TestType.FREE_TEXT)) {
                                                addFtqAnswer(question, questionInfoMcqApiDao.getFreetext_answer());
                                            } else {
                                                addSelectedAnswer(question, questionInfoMcqApiDao.getAnswer_ids());
                                            }
                                            question = getQuestionById(question.getId());
                                        }
                                    }
                                }
                                questionApiDaos.add(question);
                            }
                        }

                        if (!questionApiDaos.isEmpty()) {
                            section.setSectionQuestions(questionApiDaos);
                        }
                        sectionList.add(section);
                    }
                }

                if (!sectionList.isEmpty()) {
                    interview.setSections(sectionList);
                }
            } else {

                if (interview.getQuestions() != null && informationApiDao.getPrevQuestStates() != null) {
                    RealmList<QuestionApiDao> questions = interview.getQuestions();
                    for (PrevQuestionStateApiDao questionState : informationApiDao.getPrevQuestStates()) {
                        for (QuestionApiDao question : questions) {
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
                }
            }

            interview.setFinished(informationApiDao.isFinished());

            realm.copyToRealmOrUpdate(interview);
            realm.commitTransaction();

            updateSectionOrQuestionInfo(interview);
        } else {
            updateInterview(interview, informationApiDao);
        }
    }

    public void updateSectionTimeLeft(SectionApiDao currentSection, int timeLeft) {
        currentSection = getSectionById(currentSection.getId());
        if (currentSection != null) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
                currentSection.setDuration(timeLeft);
                realm.copyToRealmOrUpdate(currentSection);
                realm.commitTransaction();
                if (timeLeft == 0) {
                    timeLeft = -1;
                }
                saveLastTimeLeft(timeLeft);
            } else {
                updateSectionTimeLeft(currentSection, timeLeft);
            }
        }
    }

    public void updateSectionPrepTimeLeft(SectionApiDao currentSection, int timeLeft) {
        currentSection = getSectionById(currentSection.getId());
        if (currentSection != null) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
                currentSection.setPreparationTime(timeLeft);
                realm.copyToRealmOrUpdate(currentSection);
                realm.commitTransaction();
            } else {
                updateSectionPrepTimeLeft(currentSection, timeLeft);
            }
        }
    }

    public void updateSectionOnGoing(SectionApiDao currentSection, boolean onGoing) {
        currentSection = getSectionById(currentSection.getId());
        if (currentSection != null) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
                currentSection.setOnGoing(onGoing);
                realm.copyToRealmOrUpdate(currentSection);
                realm.commitTransaction();
            } else {
                updateSectionOnGoing(currentSection, onGoing);
            }
        }
    }

    public void updateInterviewOnGoing(InterviewApiDao interviewApiDao, boolean onGoing) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            interviewApiDao.setOnGoing(onGoing);
            realm.copyToRealmOrUpdate(interviewApiDao);
            realm.commitTransaction();
        } else {
            updateInterviewOnGoing(interviewApiDao, onGoing);
        }
    }

    public void updateQuestionTimeLeft(QuestionApiDao currentQuestion, int timeLeft) {
        currentQuestion = getQuestionById(currentQuestion.getId());
        if (currentQuestion != null) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
                currentQuestion.setTimeLeft(timeLeft);
                realm.copyToRealmOrUpdate(currentQuestion);
                realm.commitTransaction();
            } else {
                updateQuestionTimeLeft(currentQuestion, timeLeft);
            }
        }
    }

    public void updateInterviewTimeLeft(int timeLeft) {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
                interviewApiDao.setDuration_left(timeLeft);
                realm.copyToRealmOrUpdate(interviewApiDao);
                realm.commitTransaction();
            } else {
                updateInterviewTimeLeft(timeLeft);
            }
        }
    }

    private void saveSectionInfo() {
        SectionInfo sectionInfo = new SectionInfo(getSectionIndex());
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(sectionInfo);
            realm.commitTransaction();
        } else {
            saveSectionInfo();
        }
    }

    private void updateSectionInfo(int sectionIndex) {
        SectionInfo questionInfo = new SectionInfo(sectionIndex);
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        } else {
            updateSectionInfo(sectionIndex);
        }
    }

    private SectionInfo getSectionInfo() {
        return realm.where(SectionInfo.class).findFirst();
    }

    private void saveQuestionInfo() {
        QuestionInfo questionInfo = new QuestionInfo(getQuestionIndex(), getQuestionAttempt(), false);
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        } else {
            saveQuestionInfo();
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
        } else {
            updateQuestionInfo(questionIndex, questionAttempt);
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
        if (informationApiDao == null) {
            return false;
        } else {
            if (isSectionInterview()) {
                if (isSelfPace()) {
                    return (getCurrentSection().isOnGoing() && informationApiDao.isOnGoing()) || isContinueInterview();
                } else {
                    return (getCurrentSection().isOnGoing() && informationApiDao.isOnGoing() && informationApiDao.getSectionDurationLeft() > 0) || isContinueInterview();
                }
            } else {
                return (informationApiDao.getPrevQuestStates() != null && !informationApiDao.getPrevQuestStates().isEmpty()) || isContinueInterview();
            }
        }
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
                if (isSectionInterview()) {
                    int questionIndex = 0;

                    SectionApiDao section = getCurrentSection();
                    if (section.getType().equals(InterviewType.INTERVIEW)) {

                        for (QuestionInfoApiDao questionInfoApiDao : information.getQuestionsInfo()) {
                            if (questionInfoApiDao != null) {
                                questionIndex = questionInfoApiDao.getInterviewIndex();
                                updateQuestionInfo(questionIndex, questionInfoApiDao.getInterviewAttempt());

                                for (int i = 0; i < questionInfoApiDao.getPrevQuestStates().size(); i++) {
                                    PrevQuestionStateApiDao prevQuestionState = questionInfoApiDao.getPrevQuestStates().get(i);
                                    assert prevQuestionState != null;
                                    if (prevQuestionState.getDurationLeft() > 0) {
                                        updateQuestion(interviewApiDao, prevQuestionState);
                                        return i;
                                    }
                                }
                            }
                        }
                    } else {
                        RealmList<QuestionInfoMcqApiDao> questionsMcqInfo = information.getQuestionsMcqInfo();
                        for (int i = 0; i < questionsMcqInfo.size(); i++) {
                            QuestionInfoMcqApiDao questionInfoMcqApiDao = questionsMcqInfo.get(i);
                            if (questionInfoMcqApiDao != null) {
                                if (questionInfoMcqApiDao.getAnswer_ids().isEmpty()) {
                                    return i;
                                }
                            }
                        }
                    }

                    return questionIndex;
                } else {
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

                    }
                    return information.getInterviewIndex();
                }
            }
        }
    }

    public int getSectionIndex() {
        if (isPractice()) {
            return 0;
        }
        SectionInfo sectionInfo = getSectionInfo();
        if (sectionInfo != null) {
            return sectionInfo.getIndex();
        } else {
            InformationApiDao information = getInformation();
            if (information == null) {
                return 0;
            } else {
                return information.getSectionIndex();
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
                } else {
                    updateQuestion(interview, questionState);
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

                if (isSectionInterview()) {
                    SectionApiDao currentSection = getCurrentSection();
                    if (currentSection != null) {
                        if (currentSection.getSectionQuestions() != null) {
                            assert currentSection.getSectionQuestions().first() != null;
                            return Objects.requireNonNull(currentSection.getSectionQuestions().first()).getTakesCount();
                        }
                    }
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
    }

    public int getMediaAttempt() {
        QuestionApiDao currentQuestion = getCurrentQuestion();
        if (currentQuestion != null) {
            return currentQuestion.getMediaAttemptLeft();
        } else {
            return 1;
        }
    }

    public List<QuestionApiDao> getAllVideoQuestion() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            if (isSectionInterview()) {
                List<QuestionApiDao> pendingUpload = new ArrayList<>();

                for (SectionApiDao section : interviewApiDao.getSections()) {
                    if (section.getType().equals(SectionType.INTERVIEW)) {
                        pendingUpload.addAll(section.getSectionQuestions());
                    }
                }

                return pendingUpload;
            } else {

                List<QuestionApiDao> pendingUpload = new ArrayList<>();

                if (interviewApiDao.getType().equals(InterviewType.CLOSE_INTERVIEW)) {
                    pendingUpload.addAll(interviewApiDao.getQuestions());
                }

                return pendingUpload;
            }
        } else {
            return null;
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
            if (isProfile()) {
                if (isNeedRegister()) {
                    return interviewApiDao.getTotalVideoQuestion();
                } else {
                    return interviewApiDao.getTotalQuestion();
                }
            } else {
                if (isSectionInterview()) {
                    SectionApiDao currentSection = getCurrentSection();
                    return currentSection.getTotalQuestion();
                } else {
                    return interviewApiDao.getTotalQuestion();
                }
            }
        } else {
            return 0;
        }
    }

    public List<QuestionApiDao> getPending(@UploadStatusState String uploadStatusType) {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            if (isSectionInterview()) {
                List<QuestionApiDao> pendingUpload = new ArrayList<>();

                for (SectionApiDao section : interviewApiDao.getSections()) {
                    if (section.getType().equals(SectionType.INTERVIEW)) {
                        for (QuestionApiDao item : section.getSectionQuestions()) {
                            if (item.getUploadStatus().equals(uploadStatusType)) {
                                pendingUpload.add(item);
                            }
                        }
                    }
                }

                return pendingUpload;
            } else {

                List<QuestionApiDao> pendingUpload = new ArrayList<>();

                if (interviewApiDao.getType().equals(InterviewType.CLOSE_INTERVIEW)) {
                    for (QuestionApiDao item : interviewApiDao.getQuestions()) {
                        if (item.getUploadStatus().equals(uploadStatusType)) {
                            pendingUpload.add(item);
                        }
                    }
                }

                return pendingUpload;
            }
        } else {
            return null;
        }
    }

    public int getTotalSection() {
        if (isPractice()) {
            return 1;
        }
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            return interviewApiDao.getSections().size();
        } else {
            return 0;
        }
    }

    public boolean isSectionHasVideo() {
        for (SectionApiDao item : getCurrentInterview().getSections()) {
            if (item.getType().equals(InterviewType.INTERVIEW)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFinished() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        InformationApiDao informationApiDao = getInformation();
        if (interviewApiDao == null || informationApiDao == null) {
            return true;
        } else {
            return (interviewApiDao.isFinished() || informationApiDao.isFinished()) && isFinishInterview();
        }
    }

    private QuestionApiDao getPracticeQuestion() {
        QuestionApiDao questionApiDao = new QuestionApiDao();
        questionApiDao.setTakesCount(3);
        questionApiDao.setMaxTime(45);
        questionApiDao.setPrepTime(10);
        questionApiDao.setTitle("What are your proudest achievements, and why?");
        questionApiDao.setRetake(isPracticeRetake());
        return questionApiDao;
    }

    public SectionApiDao getCurrentSection() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            int sectionIndex = getSectionIndex();
            if (sectionIndex < interviewApiDao.getSections().size()) {
                return interviewApiDao.getSections().get(sectionIndex);
            } else {
                return interviewApiDao.getSections().last();
            }
        } else {
            return null;
        }
    }

    private SectionApiDao getSectionByIndex(int sectionIndex) {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            if (sectionIndex < interviewApiDao.getSections().size()) {
                return interviewApiDao.getSections().get(sectionIndex);
            } else {
                return interviewApiDao.getSections().last();
            }
        } else {
            return null;
        }
    }

    public SectionApiDao getSectionById(long sectionId) {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            for (SectionApiDao item : interviewApiDao.getSections()) {
                if (item.getId() == sectionId) {
                    return item;
                }
            }
        }
        return null;
    }

    private SectionApiDao getNextSection() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {

            int sectionIndex = getSectionIndex();
            if (sectionIndex < interviewApiDao.getSections().size()) {
                return interviewApiDao.getSections().get(sectionIndex);
            } else {
                return interviewApiDao.getSections().last();
            }
        } else {
            return null;
        }
    }

    public QuestionApiDao getCurrentQuestion() {
        if (isPractice()) {
            return getPracticeQuestion();
        }
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            int questionIndex = getQuestionIndex();
            if (isSectionInterview()) {
                SectionApiDao currentSection = getCurrentSection();
                if (questionIndex < currentSection.getSectionQuestions().size()) {
                    return currentSection.getSectionQuestions().get(questionIndex);
                } else {
                    return currentSection.getSectionQuestions().last();
                }
            } else {
                if (questionIndex < interviewApiDao.getQuestions().size()) {
                    return interviewApiDao.getQuestions().get(questionIndex);
                } else {
                    return interviewApiDao.getQuestions().last();
                }
            }
        } else {
            return null;
        }
    }

    public QuestionApiDao getQuestionByIndex(int questionIndex) {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            if (isSectionInterview()) {
                SectionApiDao currentSection = getCurrentSection();
                if (questionIndex < currentSection.getSectionQuestions().size()) {
                    return currentSection.getSectionQuestions().get(questionIndex);
                } else {
                    return currentSection.getSectionQuestions().last();
                }
            } else {
                if (questionIndex < interviewApiDao.getQuestions().size()) {
                    return interviewApiDao.getQuestions().get(questionIndex);
                } else {
                    return interviewApiDao.getQuestions().last();
                }
            }
        } else {
            return null;
        }
    }

    public QuestionApiDao getQuestionById(long questionId) {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            if (isSectionInterview()) {
                for (SectionApiDao section : interviewApiDao.getSections()) {
                    for (QuestionApiDao item : section.getSectionQuestions()) {
                        if (item.getId() == questionId) {
                            return item;
                        }
                    }
                }
            } else {
                for (QuestionApiDao question : interviewApiDao.getQuestions()) {
                    if (question.getId() == questionId) {
                        return question;
                    }
                }
            }
        }
        return null;
    }

    private QuestionApiDao getNextQuestion() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            int questionIndex = getQuestionIndex();
            if (isSectionInterview()) {
                SectionApiDao currentSection = getCurrentSection();
                if (questionIndex < currentSection.getSectionQuestions().size()) {
                    return currentSection.getSectionQuestions().get(questionIndex);
                } else {
                    return currentSection.getSectionQuestions().last();
                }
            } else {
                if (questionIndex < interviewApiDao.getQuestions().size()) {
                    return interviewApiDao.getQuestions().get(questionIndex);
                } else {
                    return interviewApiDao.getQuestions().last();
                }
            }
        } else {
            return null;
        }
    }

    public void increaseQuestionIndex() {
        if (isPractice()) {
            return;
        }
        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            QuestionInfo questionInfo = getQuestionInfo();

            QuestionApiDao nextQuestion = getNextQuestion();
            if (nextQuestion != null) {
                questionInfo.increaseIndex();
                questionInfo.setAttempt(nextQuestion.getTakesCount());
            } else {
                questionInfo.resetAttempt();
            }

            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        } else {
            increaseQuestionIndex();
        }
    }

    public void decreaseQuestionIndex() {
        if (isPractice()) {
            return;
        }
        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            QuestionInfo questionInfo = getQuestionInfo();
            if (questionInfo.getIndex() > 0) {
                questionInfo.decreaseIndex();
            }

            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        } else {
            decreaseQuestionIndex();
        }
    }

    public void increaseSectionIndex() {
        if (isPractice()) {
            return;
        }
        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            SectionInfo sectionInfo = getSectionInfo();

            SectionApiDao currentSection = getCurrentSection();
            SectionApiDao nextSection = getNextSection();
            if (nextSection != null) {
                if (currentSection.isOnGoing()) {
                    sectionInfo.increaseIndex();
                    LogUtil.addNewLog(getInterviewCode(),
                            new LogDao("Section",
                                    "Section Index Increased"
                            )
                    );
                } else {
                    LogUtil.addNewLog(getInterviewCode(),
                            new LogDao("Section",
                                    "Section Index Failed to Increased, because not started"
                            )
                    );
                }
            } else {
                LogUtil.addNewLog(getInterviewCode(),
                        new LogDao("Section",
                                "Section Index not Increased"
                        )
                );
            }

            realm.copyToRealmOrUpdate(sectionInfo);
            realm.commitTransaction();

            LogUtil.addNewLog(getInterviewCode(),
                    new LogDao("Section",
                            "Question Info reset"
                    )
            );
            updateQuestionInfo(0, 0);
        } else {
            increaseSectionIndex();
        }
    }

    public void decreaseQuestionAttempt() {

        QuestionInfo questionInfo = getQuestionInfo();
        if (questionInfo == null) {
            updateQuestionInfo(0, 0);
            questionInfo = getQuestionInfo();
        }

        if (!realm.isInTransaction() && questionInfo != null) {
            realm.beginTransaction();

            questionInfo.decreaseAttempt();
            int attempt = questionInfo.getAttempt();

            if (attempt > 0) {
                realm.copyToRealmOrUpdate(questionInfo);
            }
            realm.commitTransaction();
        } else {
            decreaseQuestionAttempt();
        }
    }

    public void decreaseMediaAttempt() {

        QuestionApiDao currentQuestion = getCurrentQuestion();

        if (!realm.isInTransaction() && currentQuestion != null) {
            realm.beginTransaction();

            currentQuestion.decreaseMediaAttempt();
            int mediaAttempt = currentQuestion.getMediaAttemptLeft();

            if (mediaAttempt > 0) {
                realm.copyToRealmOrUpdate(currentQuestion);
            }
            realm.commitTransaction();
        } else {
            decreaseMediaAttempt();
        }
    }

    public boolean isLastAttempt() {
        return getQuestionAttempt() <= 0;
    }

    public boolean isNotLastQuestion() {
        if (isSectionInterview()) {
            SectionApiDao sectionApiDao = getSectionByIndex(getSectionIndex());
            assert sectionApiDao != null;
            return getQuestionIndex() < sectionApiDao.getTotalQuestion();
        } else {
            return getQuestionIndex() < getTotalQuestion();
        }
    }

    public boolean isNotLastSection() {
        return getSectionIndex() < getTotalSection();
    }

    public boolean isLastSection() {
        return !isNotLastSection();
    }

    public void updateCompressing(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.COMPRESSING);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();
        } else {
            updateCompressing(questionApiDao);
        }
    }

    public void updateVideoPath(QuestionApiDao questionApiDao, String videoPath) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setVideoPath(videoPath);
            questionApiDao.setUploadStatus(UploadStatusType.COMPRESSED);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();
        } else {
            updateVideoPath(questionApiDao, videoPath);
        }
    }

    public void updateQuestionMediaPath(QuestionApiDao questionApiDao, String mediaPath) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            if (questionApiDao.getMedia() != null) {
                questionApiDao.getMedia().setOfflinePath(mediaPath);
            }

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();
        } else {
            updateQuestionMediaPath(questionApiDao, mediaPath);
        }
    }

    public void updateMediaPath(SectionApiDao sectionApiDao, String mediaPath) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            if (sectionApiDao.getMedia() != null) {
                sectionApiDao.getMedia().setOfflinePath(mediaPath);
            }

            realm.copyToRealmOrUpdate(sectionApiDao);
            realm.commitTransaction();
        } else {
            updateMediaPath(sectionApiDao, mediaPath);
        }
    }

    public void updateProgress(QuestionApiDao questionApiDao, double progress) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadProgress(progress);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s on progress uploading %s / 100", questionApiDao.getId(), progress);
        } else {
            updateProgress(questionApiDao, progress);
        }
    }

    public void markUploading(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.UPLOADING);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s is now uploading", questionApiDao.getId());
        } else {
            markUploading(questionApiDao);
            Timber.e("Video with Question Id %s is failed to marked uploading", questionApiDao.getId());
        }
    }

    public void markNotAnswer(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.NOT_ANSWER);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s is now uploading", questionApiDao.getId());
        } else {
            markNotAnswer(questionApiDao);
        }
    }

    public void markUploaded(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.UPLOADED);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s has been uploaded", questionApiDao.getId());
        } else {
            markUploaded(questionApiDao);
            Timber.e("Video with Question Id %s is failed to marked uploaded", questionApiDao.getId());
        }
    }

    public void markAsCompressed(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.COMPRESSED);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s mark as pending", questionApiDao.getId());
        } else {
            markAsCompressed(questionApiDao);
        }
    }

    public void markAsPending(QuestionApiDao questionApiDao, String rawFilePath) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setUploadStatus(UploadStatusType.PENDING);
            questionApiDao.setVideoPath(rawFilePath);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s mark as pending", questionApiDao.getId());
        } else {
            markAsPending(questionApiDao, rawFilePath);
        }
    }

    public void markAsRetake(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setRetake(true);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s mark as retake", questionApiDao.getId());
        } else {
            markAsRetake(questionApiDao);
        }
    }

    public void clearDb() {
        removeHawkSaved();
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
        } else {
            clearDb();
        }
    }

    public void clearVideoFile(Context context) {
        File filesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);

        try {
            if (filesDir != null) {
                File[] files = filesDir.listFiles();

                if (files != null) {
                    for (File file : files) {
                        deleteRecursive(file);
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null && fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory()) {
                if (fileOrDirectory.listFiles() != null) {
                    for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                        deleteRecursive(child);
                    }
                }
            }

            fileOrDirectory.deleteOnExit();
        }
    }

    public boolean isPractice() {
        return isPractice;
    }

    public boolean isSectionInterview() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        return interviewApiDao != null && interviewApiDao.getSections() != null && !interviewApiDao.getSections().isEmpty();
    }

    public void setInterviewFinished() {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            InterviewApiDao interviewApiDao = getCurrentInterview();
            interviewApiDao.setFinished(true);
            realm.copyToRealmOrUpdate(interviewApiDao);
            realm.commitTransaction();

            Timber.d("Interview marked as finished in local");
        } else {
            setInterviewFinished();
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
        } else {
            setPracticeMode();
        }
    }

    public void finishPracticeMode() {
        isPractice = false;
    }

    public long getAvailableStorage() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        long megAvailable = bytesAvailable / (1024 * 1024);
        Timber.d("Available MB : %s", megAvailable);
        return megAvailable;
    }

    public boolean isStorageEnough() {
        if (isProfile()) {
            return getAvailableStorage() > 300 + (getTotalQuestion() * 30);
        } else {
            if (isSectionInterview()) {
                if (isSectionHasVideo()) {
                    return getAvailableStorage() > 300 + (getTotalQuestion() * 30);
                } else {
                    return true;
                }
            } else {
                return getAvailableStorage() > 300 + (getTotalQuestion() * 30);
            }
        }
    }

    public void addSelectedAnswer(QuestionApiDao questionApiDao, MultipleAnswerApiDao answer) {
        if (!realm.isInTransaction()) {

            RealmList<MultipleAnswerApiDao> selectedAnswer = new RealmList<>();
            realm.beginTransaction();

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

            for (MultipleAnswerApiDao item : multipleAnswer) {
                if (item.isSelected()) {
                    selectedAnswer.add(item);
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
        } else {
            addSelectedAnswer(questionApiDao, answer);
        }
    }

    private void addSelectedAnswer(QuestionApiDao questionApiDao, RealmList<Integer> answerIds) {

        RealmList<MultipleAnswerApiDao> selectedAnswer = new RealmList<>();

        RealmList<MultipleAnswerApiDao> multipleAnswer = questionApiDao.getMultiple_answers();

        for (Integer answerId : answerIds) {
            for (MultipleAnswerApiDao item : multipleAnswer) {
                if (questionApiDao.isMultipleChoice()) {
                    if (item.getId() == answerId) {
                        item.setSelected(!item.isSelected());
                    }
                } else {
                    if (item.getId() == answerId) {
                        item.setSelected(!item.isSelected());
                    } else {
                        item.setSelected(false);
                    }
                }
            }
        }

        for (MultipleAnswerApiDao item : multipleAnswer) {
            if (item.isSelected()) {
                selectedAnswer.add(item);
            }
        }

        questionApiDao.setSelectedAnswer(selectedAnswer);
        questionApiDao.setMultiple_answers(multipleAnswer);
        if (selectedAnswer.isEmpty()) {
            questionApiDao.setAnswered(false);
        } else {
            questionApiDao.setAnswered(true);
        }
    }

    public void addAnswer(QuestionApiDao questionApiDao, String answer) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setAnswer(answer);
            if (answer.isEmpty()) {
                questionApiDao.setAnswered(false);
            } else {
                questionApiDao.setAnswered(true);
            }
            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();
        } else {
            addAnswer(questionApiDao, answer);
        }
    }

    private void addFtqAnswer(QuestionApiDao questionApiDao, String answer) {
        questionApiDao.setAnswer(answer);
        if (answer.isEmpty()) {
            questionApiDao.setAnswered(false);
        } else {
            questionApiDao.setAnswered(true);
        }
    }

    public void markAnswered(QuestionApiDao questionApiDao) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            questionApiDao.setAnswered(true);

            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();

            Timber.d("Video with Question Id %s has been uploaded", questionApiDao.getId());
        } else {
            markAnswered(questionApiDao);
            Timber.e("Video with Question Id %s is failed to marked uploaded", questionApiDao.getId());
        }
    }

    public CustomFieldApiDao getCustomFieldById(long fieldId) {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            for (CustomFieldApiDao fieldApiDao : interviewApiDao.getCustom_fields().getFields()) {
                if (fieldApiDao.getId() == fieldId) {
                    return fieldApiDao;
                }
            }
        }
        return null;
    }

    public void addCustomFieldAnswer(CustomFieldApiDao field, String answer) {
        if (!realm.isInTransaction()) {

            realm.beginTransaction();

            RealmList<String> newAnswers = new RealmList<>();
            RealmList<String> answers = field.getAnswers();

            if (answers.isEmpty()) {
                newAnswers.add(answer);
            } else {
                if (answers.contains(answer)) {
                    answers.remove(answer);
                } else {
                    answers.add(answer);
                }

                if (answers.size() > field.getMaxOptions()) {
                    answers.remove(0);
                }

                newAnswers.addAll(answers);
            }

            field.setAnswers(newAnswers);
            realm.copyToRealmOrUpdate(field);
            realm.commitTransaction();
        } else {
            addCustomFieldAnswer(field, answer);
        }
    }

    @NonNull
    private OkHttpClient getOkHttpClient() {

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.followRedirects(true);
        httpClientBuilder.followSslRedirects(true);
        httpClientBuilder.retryOnConnectionFailure(true);
        httpClientBuilder.writeTimeout(5, TimeUnit.MINUTES);
        httpClientBuilder.readTimeout(60, TimeUnit.SECONDS);
        httpClientBuilder.connectTimeout(3, TimeUnit.MINUTES);

        if (isDebuggable) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClientBuilder.addInterceptor(loggingInterceptor);
        }

        final String manufacturer = Build.MANUFACTURER;
        final String model = Build.MODEL;
        final String device = String.format("%s %s", manufacturer, model);
        final String os = "Android " + Build.VERSION.RELEASE;

        httpClientBuilder.addInterceptor(chain -> {
            Request request = chain.request().newBuilder()
                    .addHeader("device", device)
                    .addHeader("os", os)
                    .addHeader("browser", "")
                    .addHeader("screenresolution", getScreenWidth() + "x" + getScreenHeight())
                    .build();
            return chain.proceed(request);
        });

        return httpClientBuilder.build();
    }

    public AstronautApi getApi() {
        if (mAstronautApi == null) {
            mAstronautApi = new AstronautApi(mApiUrl, isDebuggable);
        }
        return mAstronautApi;
    }

    public boolean haveMediaToDownload() {
        boolean haveMediaToDownload = false;
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (isSectionInterview()) {
            for (SectionApiDao section : interviewApiDao.getSections()) {
                if (section.getMedia() != null) {
                    if (section.getMedia().getOfflinePath() == null) {
                        haveMediaToDownload = true;
                    }
                }

                for (QuestionApiDao question : section.getSectionQuestions()) {
                    if (question.getMedia() != null) {
                        if (question.getMedia().getOfflinePath() == null) {
                            haveMediaToDownload = true;
                        }
                    }
                }
            }
        } else {

            for (QuestionApiDao question : interviewApiDao.getQuestions()) {
                if (question.getMedia() != null) {
                    if (question.getMedia().getOfflinePath() == null) {
                        haveMediaToDownload = true;
                    }
                }
            }
        }

        WelcomeVideoDao welcomeVideoDao = getWelcomeVideoDao();
        String videoUri = getWelcomeVideoUri();
        if (welcomeVideoDao != null && videoUri.isEmpty()) {
            haveMediaToDownload = true;
        }

        return haveMediaToDownload;
    }

}
