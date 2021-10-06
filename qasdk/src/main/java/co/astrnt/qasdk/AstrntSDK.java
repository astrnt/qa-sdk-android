package co.astrnt.qasdk;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;

import com.downloader.PRDownloader;
import com.orhanobut.hawk.Hawk;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

import co.astrnt.qasdk.constants.PreferenceKey;
import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.CandidateApiDao;
import co.astrnt.qasdk.dao.CompanyApiDao;
import co.astrnt.qasdk.dao.CustomFieldApiDao;
import co.astrnt.qasdk.dao.CustomFieldResultApiDao;
import co.astrnt.qasdk.dao.GdprDao;
import co.astrnt.qasdk.dao.InformationApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.JobApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.MediaDao;
import co.astrnt.qasdk.dao.MultipleAnswerApiDao;
import co.astrnt.qasdk.dao.PrevQuestionStateApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.dao.QuestionInfoApiDao;
import co.astrnt.qasdk.dao.QuestionInfoMcqApiDao;
import co.astrnt.qasdk.dao.SectionApiDao;
import co.astrnt.qasdk.dao.SupportMaterialDao;
import co.astrnt.qasdk.dao.WelcomeVideoDao;
import co.astrnt.qasdk.type.InterviewType;
import co.astrnt.qasdk.type.SectionType;
import co.astrnt.qasdk.type.TestType;
import co.astrnt.qasdk.type.UploadStatusState;
import co.astrnt.qasdk.type.UploadStatusType;
import co.astrnt.qasdk.upload.SingleVideoUploadService;
import co.astrnt.qasdk.utils.FileUtils;
import co.astrnt.qasdk.utils.HawkUtils;
import co.astrnt.qasdk.utils.LogUtil;
import co.astrnt.qasdk.utils.QuestionInfo;
import co.astrnt.qasdk.utils.SectionInfo;
import co.astrnt.qasdk.utils.ServiceUtils;
import co.astrnt.qasdk.videocompressor.services.VideoCompressService;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import timber.log.Timber;

import static co.astrnt.qasdk.type.InterviewType.ASTRONAUT_PROFILE;
import static co.astrnt.qasdk.type.InterviewType.PROFILE;

public class AstrntSDK extends HawkUtils {

    private static final int DB_VERSION = 21;
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

    public static RealmConfiguration getRealmConfig() {
        return new RealmConfiguration.Builder()
                .name("astrntdb")
                .schemaVersion(DB_VERSION)
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

        Hawk.put(PreferenceKey.KEY_TOKEN, resultApiDao.getToken());

        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            if (resultApiDao.getInformation() != null) {
                realm.copyToRealmOrUpdate(resultApiDao.getInformation());
            }
            if (resultApiDao.getInvitation_video() != null) {
                realm.copyToRealmOrUpdate(resultApiDao.getInvitation_video());
            }
            realm.commitTransaction();
            GdprDao gdprDao = new GdprDao(resultApiDao.getGdpr_complied(), resultApiDao.getGdpr_text(), resultApiDao.getGdpr_aggrement_text(), resultApiDao.getCompliance());
            saveGdprDao(gdprDao);
            if (resultApiDao.getWelcomeVideo() != null && !resultApiDao.getWelcomeVideo().getWelcome_video_url().equals("")) {
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
                updateTrySampleQuestion(interviewApiDao, resultApiDao.getInterview().getTrySampleQuestion());

                InterviewApiDao currentInterview1 = getCurrentInterview();
                if (resultApiDao.getInformation().getQuestionsMcqInfo() != null && !resultApiDao.getInformation().getQuestionsMcqInfo().isEmpty())
                    updateInterviewSelectedAnswer(currentInterview1, resultApiDao);
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    updateInterviewSelectedAnswer(currentInterview1, resultApiDao);
                }, 2000);


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
                            newSection.setSupport_materials(newSection.getSupport_materials());
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
                newInterview.setSample_question(newInterview.getSample_question());
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

    private void updateInterviewSelectedAnswer(InterviewApiDao interview, InterviewResultApiDao interviewResultApiDao) {
        if (!isSectionInterview()) {
            updateQuestionInfo(interviewResultApiDao.getInformation().getInterviewIndex(), interviewResultApiDao.getInformation().getInterviewSubIndex(), interviewResultApiDao.getInformation().getInterviewAttempt());
        } else {
            if (isSelfPace()) {
                updateQuestionInfo(interviewResultApiDao.getInformation().getQuestion_index(), interviewResultApiDao.getInformation().getInterviewSubIndex(), interviewResultApiDao.getInformation().getInterviewAttempt());
            }
        }
        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            LogUtil.addNewLog(getInterviewCode(),
                    new LogDao("API Resume Information",
                            interviewResultApiDao.getInformation().toString()
                    )
            );

            if (!isSectionInterview()) {
                RealmList<QuestionApiDao> questionList = new RealmList<>();

                if (interview.getSample_question() != null) {
                    clearSelectedAnswer(interview.getSample_question());
                }
                if (interviewResultApiDao.getInterview().getQuestions() != null && interviewResultApiDao.getInformation().getQuestionsMcqInfo() != null) {
                    RealmList<QuestionApiDao> questions = interview.getQuestions();
                    for (QuestionApiDao question : questions) {

                        for (QuestionInfoMcqApiDao questionState : interviewResultApiDao.getInformation().getQuestionsMcqInfo()) {
                            if (question.getId() == questionState.getId()) {
                                addClearSelectedAnswer(question, questionState.getAnswer_ids());
                                if (question.getType_child() != null) {
                                    if (question.getType_child().equals(TestType.FREE_TEXT)) {
                                        addFtqAnswer(question, questionState.getFreetext_answer());
                                    } else {
                                        addSelectedAnswer(question, questionState.getAnswer_ids());
                                    }
                                }
                            }
                            RealmList<QuestionApiDao> subQuestions = question.getSub_questions();
                            if (subQuestions != null && !subQuestions.isEmpty()) {
                                for (QuestionApiDao subQuestion : subQuestions) {
                                    if (subQuestion.getId() == questionState.getId()) {
                                        addClearSelectedAnswer(subQuestion, questionState.getAnswer_ids());
                                        if (subQuestion.getType_child() != null) {
                                            if (subQuestion.getType_child().equals(TestType.FREE_TEXT)) {
                                                addFtqAnswer(subQuestion, questionState.getFreetext_answer());
                                            } else {
                                                addSelectedAnswer(subQuestion, questionState.getAnswer_ids());
                                            }
                                        }
                                    }
                                }
                            }


                        }
                        questionList.add(question);
                    }
                    interview.setQuestions(questionList);
                }
            } else {
                RealmList<SectionApiDao> sectionList = new RealmList<>();

                for (int i = 0; i < interview.getSections().size(); i++) {
                    SectionApiDao section = interview.getSections().get(i);
                    RealmList<QuestionApiDao> questions = new RealmList<>();

                    if (section != null) {
                        if (section.getSample_question() != null) {
                            clearSelectedAnswer(section.getSample_question());
                        }

                        if (section.getType().equals(SectionType.INTERVIEW)) {

                        } else {
                            for (QuestionApiDao question : section.getSectionQuestions()) {

                                if (interviewResultApiDao.getInformation().getQuestionsMcqInfo() != null && !interviewResultApiDao.getInformation().getQuestionsMcqInfo().isEmpty()) {
                                    for (QuestionInfoMcqApiDao questionInfoMcqApiDao : interviewResultApiDao.getInformation().getQuestionsMcqInfo()) {

                                        if (section.getId() == getCurrentSection().getId()) {
                                            if (question.getId() == questionInfoMcqApiDao.getId()) {
                                                addClearSelectedAnswer(question, questionInfoMcqApiDao.getAnswer_ids());
                                                if (question.getType_child() != null) {
                                                    if (question.getType_child().equals(TestType.FREE_TEXT)) {
                                                        addFtqAnswer(question, questionInfoMcqApiDao.getFreetext_answer());
                                                    } else {
                                                        addSelectedAnswer(question, questionInfoMcqApiDao.getAnswer_ids());
                                                    }
                                                }
                                            }

                                            RealmList<QuestionApiDao> subQuestions = question.getSub_questions();
                                            if (subQuestions != null && !subQuestions.isEmpty()) {
                                                for (QuestionApiDao subQuestion : subQuestions) {

                                                    if (subQuestion.getId() == questionInfoMcqApiDao.getId()) {
                                                        if (subQuestion.getType_child() != null) {
                                                            if (subQuestion.getType_child().equals(TestType.FREE_TEXT)) {
                                                                addFtqAnswer(subQuestion, questionInfoMcqApiDao.getFreetext_answer());
                                                            } else {
                                                                addSelectedAnswer(subQuestion, questionInfoMcqApiDao.getAnswer_ids());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }
                                questions.add(question);
                            }
                        }

                        if (!questions.isEmpty()) {
                            section.setSectionQuestions(questions);
                            section.setSupport_materials(section.getSupport_materials());
                        }
                        sectionList.add(section);
                    }
                }

                if (!sectionList.isEmpty()) {
                    interview.setSections(sectionList);
                }
            }
            realm.copyToRealmOrUpdate(interview);
            realm.commitTransaction();
        } else {
            updateInterviewSelectedAnswer(interview, interviewResultApiDao);
        }
    }

    private void updateInterview(InterviewApiDao interview, InformationApiDao informationApiDao) {
        if (isSectionInterview()) {
            updateSectionInfo(informationApiDao.getSectionIndex());

            if (informationApiDao.getQuestionsInfo() != null && !informationApiDao.getQuestionsInfo().isEmpty()) {
                for (QuestionInfoApiDao questionInfoApiDao : informationApiDao.getQuestionsInfo()) {
                    updateQuestionInfo(questionInfoApiDao.getInterviewIndex(), questionInfoApiDao.getInterviewSubIndex(), questionInfoApiDao.getInterviewAttempt());
                }
            }
        } else {
            updateQuestionInfo(informationApiDao.getInterviewIndex(), informationApiDao.getInterviewSubIndex(), informationApiDao.getInterviewAttempt());
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
                    RealmList<QuestionApiDao> questions = new RealmList<>();

                    if (section != null) {
                        if (section.getSample_question() != null) {
                            clearSelectedAnswer(section.getSample_question());
                        }

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
                            section.setTry_sample_question(section.getTry_sample_question());
                        }

                        if (section.getType().equals(SectionType.INTERVIEW)) {

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
                                questions.add(question);
                            }
                        } else {
                            for (QuestionApiDao question : section.getSectionQuestions()) {

                                if (informationApiDao.getQuestionsMcqInfo() != null && !informationApiDao.getQuestionsMcqInfo().isEmpty()) {
                                    for (QuestionInfoMcqApiDao questionInfoMcqApiDao : informationApiDao.getQuestionsMcqInfo()) {

                                        if (question.getSub_questions().isEmpty() && question.getSub_questions() == null) {
                                            if (question.getId() == questionInfoMcqApiDao.getId()) {
                                                if (question.getType_child() != null) {
                                                    if (question.getType_child().equals(TestType.FREE_TEXT)) {
                                                        addFtqAnswer(question, questionInfoMcqApiDao.getFreetext_answer());
                                                    } else {
                                                        addSelectedAnswer(question, questionInfoMcqApiDao.getAnswer_ids());
                                                    }
                                                }
                                                question = getQuestionById(question.getId());
                                            }
                                        }

                                        RealmList<QuestionApiDao> subQuestions = question.getSub_questions();
                                        RealmList<QuestionApiDao> updatedSubQuestions = new RealmList<>();
                                        if (subQuestions != null && !subQuestions.isEmpty()) {
                                            for (QuestionApiDao subQuestion : subQuestions) {

                                                if (subQuestion.getId() == questionInfoMcqApiDao.getId()) {
                                                    if (subQuestion.getType_child() != null) {
                                                        if (subQuestion.getType_child().equals(TestType.FREE_TEXT)) {
                                                            addFtqAnswer(subQuestion, questionInfoMcqApiDao.getFreetext_answer());
                                                        } else {
                                                            addSelectedAnswer(subQuestion, questionInfoMcqApiDao.getAnswer_ids());
                                                        }
                                                    }
                                                    subQuestion = getQuestionById(subQuestion.getId());
                                                }
                                                updatedSubQuestions.add(subQuestion);
                                            }
                                            question.setSub_questions(updatedSubQuestions);
                                        }

                                    }
                                }
                                questions.add(question);
                            }
                        }

                        if (!questions.isEmpty()) {
                            section.setSectionQuestions(questions);
                            section.setSupport_materials(section.getSupport_materials());
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

                            RealmList<QuestionApiDao> subQuestions = question.getSub_questions();
                            if (subQuestions != null && !subQuestions.isEmpty()) {
                                for (QuestionApiDao subQuestion : subQuestions) {
                                    if (subQuestion.getId() == questionState.getQuestionId()) {
                                        if (questionState.isAnswered()) {
                                            subQuestion.setAnswered(true);
                                        } else {
                                            subQuestion.setAnswered(false);
                                        }
                                        subQuestion.setTimeLeft(questionState.getDurationLeft());
                                    }
                                }
                            }

                        }
                    }

                    interview.setQuestions(questions);
                    interview.setSample_question(interview.getSample_question());
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

    public void updateSectionSampleQuestion(SectionApiDao currentSection, int trySample) {
        currentSection = getSectionById(currentSection.getId());
        if (currentSection != null) {
            if (!realm.isInTransaction()) {
                realm.beginTransaction();
                currentSection.setTry_sample_question(trySample);
                realm.copyToRealmOrUpdate(currentSection);
                realm.commitTransaction();
            } else {
                updateSectionSampleQuestion(currentSection, trySample);
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

    public void updateTrySampleQuestion(InterviewApiDao interviewApiDao, int trySampleQuestion) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            interviewApiDao.setTrySampleQuestion(trySampleQuestion);
            realm.copyToRealmOrUpdate(interviewApiDao);
            realm.commitTransaction();
        } else {
            updateTrySampleQuestion(interviewApiDao, trySampleQuestion);
        }
    }

    public void updateDurationLeft(InterviewApiDao interviewApiDao, int timeLeft) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            interviewApiDao.setDuration_left(timeLeft);
            realm.copyToRealmOrUpdate(interviewApiDao);
            realm.commitTransaction();
        } else {
            updateDurationLeft(interviewApiDao, timeLeft);
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
        QuestionInfo questionInfo = new QuestionInfo(getQuestionIndex(), getQuestionSubIndex(), getQuestionAttempt(), isPractice());
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        } else {
            saveQuestionInfo();
        }
    }

    public void updateSubQuestionInfo(long subQuestionId) {
        if (!realm.isInTransaction()) {
            QuestionApiDao currentQuestion = getCurrentQuestion();
            int subQuestionIndex = 0;
            for (int i = 0; i < currentQuestion.getSub_questions().size(); i++) {
                QuestionApiDao subQuestion = currentQuestion.getSub_questions().get(i);
                assert subQuestion != null;
                if (subQuestionId == subQuestion.getId()) {
                    subQuestionIndex = i;
                }
            }
            QuestionInfo questionInfo = new QuestionInfo(getQuestionIndex(), subQuestionIndex, getQuestionAttempt(), isPractice());
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        } else {
            updateSubQuestionInfo(subQuestionId);
        }
    }

    private void updateQuestionInfo(int questionIndex, int questionSubIndex, int questionAttempt) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            QuestionApiDao currentQuestion = getQuestionByIndex(questionIndex);
            questionAttempt = currentQuestion.getTakesCount() - questionAttempt;
            QuestionInfo questionInfo = new QuestionInfo(questionIndex, questionSubIndex, questionAttempt, isPractice());
            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        } else {
            updateQuestionInfo(questionIndex, questionSubIndex, questionAttempt);
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
                    if (section.getType().equals(SectionType.INTERVIEW)) {

                        for (QuestionInfoApiDao questionInfoApiDao : information.getQuestionsInfo()) {
                            if (questionInfoApiDao != null) {
                                questionIndex = questionInfoApiDao.getInterviewIndex();
                                int questionSubIndex = questionInfoApiDao.getInterviewSubIndex();
                                updateQuestionInfo(questionIndex, questionSubIndex, questionInfoApiDao.getInterviewAttempt());

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

    public int getQuestionSubIndex() {
        if (isPractice()) {
            return 0;
        }
        QuestionInfo questionInfo = getQuestionInfo();
        if (questionInfo != null) {
            return questionInfo.getSubIndex();
        } else {
            InformationApiDao information = getInformation();
            if (information == null) {
                return 0;
            } else {
                InterviewApiDao interviewApiDao = getCurrentInterview();
                if (isSectionInterview()) {
                    int questionSubIndex = 0;

                    SectionApiDao section = getCurrentSection();
                    if (section.getType().equals(SectionType.INTERVIEW)) {

                        for (QuestionInfoApiDao questionInfoApiDao : information.getQuestionsInfo()) {
                            if (questionInfoApiDao != null) {
                                int questionIndex = questionInfoApiDao.getInterviewIndex();
                                questionSubIndex = questionInfoApiDao.getInterviewSubIndex();
                                updateQuestionInfo(questionIndex, questionSubIndex, questionInfoApiDao.getInterviewAttempt());

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

                    return questionSubIndex;
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
                    return information.getInterviewSubIndex();
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

        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            for (QuestionApiDao question : interview.getQuestions()) {
                if (question.getId() == questionState.getQuestionId()) {
                    if (questionState.isAnswered()) {
                        question.setAnswered(true);
                    }
                    question.setTimeLeft(questionState.getDurationLeft());
                    realm.copyToRealmOrUpdate(question);
                }
                RealmList<QuestionApiDao> subQuestions = question.getSub_questions();

                if (subQuestions != null && !subQuestions.isEmpty()) {
                    RealmList<QuestionApiDao> updatedSubQuestions = new RealmList<>();

                    for (QuestionApiDao subQuestion : subQuestions) {
                        if (subQuestion.getId() == questionState.getQuestionId()) {
                            if (questionState.isAnswered()) {
                                subQuestion.setAnswered(true);
                            }
                            subQuestion.setTimeLeft(questionState.getDurationLeft());
                            subQuestion = getQuestionById(subQuestion.getId());
                            realm.copyToRealmOrUpdate(subQuestion);
                        }
                        updatedSubQuestions.add(subQuestion);
                    }
                    question.setSub_questions(updatedSubQuestions);
                    realm.copyToRealmOrUpdate(question);
                }
            }

            realm.commitTransaction();
        } else {
            updateQuestion(interview, questionState);
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

    public RealmList<QuestionApiDao> getAllVideoQuestion() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            if (isSectionInterview()) {
                RealmList<QuestionApiDao> pendingUpload = new RealmList<>();

                for (SectionApiDao section : interviewApiDao.getSections()) {
                    if (section.getType().equals(SectionType.INTERVIEW)) {
                        pendingUpload.addAll(section.getSectionQuestions());
                    }
                }

                return pendingUpload;
            } else {

                RealmList<QuestionApiDao> pendingUpload = new RealmList<>();

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
                    return getTotalQuestionsAndSubs();
                }
            } else {
                return getTotalQuestionsAndSubs();
            }
        } else {
            return 0;
        }
    }

    public int getTotalQuestionsAndSubs() {

        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {

            RealmList<QuestionApiDao> questions = getQuestionsAndSubs();
            if (questions != null) {
                return questions.size();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public RealmList<QuestionApiDao> getQuestionsAndSubs() {

        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {

            RealmList<QuestionApiDao> questions = new RealmList<>();
            RealmList<QuestionApiDao> questionsWithSub = new RealmList<>();
            if (isSectionInterview()) {
                RealmList<SectionApiDao> sections = interviewApiDao.getSections();
                SectionApiDao currentSection = getCurrentSection();
                for (SectionApiDao section : sections) {
                    if (currentSection.getId() == section.getId() && !isShowUpload()) {
                        questions.addAll(section.getSectionQuestions());
                    }
                }
            } else {
                questions.addAll(interviewApiDao.getQuestions());
            }

            for (QuestionApiDao question : questions) {
                if (question.getSub_questions() != null && !question.getSub_questions().isEmpty()) {
                    questionsWithSub.addAll(question.getSub_questions());
                } else {
                    questionsWithSub.add(question);
                }
            }
            return questionsWithSub;
        } else {
            return null;
        }
    }

    public int getTotalSubQuestion() {
        if (isPractice()) {
            return 1;
        }
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            QuestionApiDao currentQuestion = getCurrentQuestion();
            if (currentQuestion.getSub_questions() != null && !currentQuestion.getSub_questions().isEmpty()) {
                return currentQuestion.getSub_questions().size();
            } else {
                return 0;
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
            return (interviewApiDao.isFinished() && informationApiDao.isFinished()) && isFinishInterview();
        }
    }

    public boolean isTriedSampleQuestion() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (isSectionInterview()) {
            SectionApiDao currentSection = getCurrentSection();
            return currentSection != null && currentSection.getTry_sample_question() != 0;
        } else {
            if (interviewApiDao.getTrySampleQuestion() != 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    private QuestionApiDao getPracticeQuestion() {
        QuestionApiDao questionApiDao = new QuestionApiDao();
        questionApiDao.setId(-1);
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

    public QuestionApiDao getSampleQuestion() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            return interviewApiDao.getSample_question();
        } else {
            return null;
        }
    }

    public void setSectionShowReview(SectionApiDao section) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            section.setShowReview(true);

            realm.copyToRealmOrUpdate(section);
            realm.commitTransaction();
        } else {
            setSectionShowReview(section);
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

    public QuestionApiDao getCurrentSubQuestion() {
        if (isPractice()) {
            return getPracticeQuestion();
        }
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            int questionSubIndex = getQuestionSubIndex();
            QuestionApiDao currentQuestion = getCurrentQuestion();
            RealmList<QuestionApiDao> subQuestions = currentQuestion.getSub_questions();
            if (subQuestions != null && !subQuestions.isEmpty()) {
                if (questionSubIndex < subQuestions.size()) {
                    return subQuestions.get(questionSubIndex);
                } else {
                    return subQuestions.last();
                }
            } else {
                return null;
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
            RealmList<QuestionApiDao> questions = new RealmList<>();
            if (isSectionInterview()) {
                for (SectionApiDao section : interviewApiDao.getSections()) {
                    questions.addAll(section.getSectionQuestions());
                }
            } else {
                questions.addAll(interviewApiDao.getQuestions());
            }

            for (QuestionApiDao question : questions) {
                if (question.getId() == questionId) {
                    return question;
                } else {
                    if (question.getSub_questions() != null && !question.getSub_questions().isEmpty()) {
                        for (QuestionApiDao subQuestion : question.getSub_questions()) {
                            if (subQuestion.getId() == questionId) {
                                return subQuestion;
                            }
                        }
                    }
                }
            }

        }
        return null;
    }

    public QuestionApiDao getParentQuestionById(long questionId) {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            RealmList<QuestionApiDao> questions = new RealmList<>();
            if (isSectionInterview()) {
                for (SectionApiDao section : interviewApiDao.getSections()) {
                    questions.addAll(section.getSectionQuestions());
                }
            } else {
                questions.addAll(interviewApiDao.getQuestions());
            }

            for (QuestionApiDao question : questions) {
                if (question.getId() == questionId) {
                    return null;
                } else {
                    if (question.getSub_questions() != null && !question.getSub_questions().isEmpty()) {
                        for (QuestionApiDao subQuestion : question.getSub_questions()) {
                            if (subQuestion.getId() == questionId) {
                                return question;
                            }
                        }
                    }
                }
            }

        }
        return null;
    }

    public int getIndexById(long questionId) {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            RealmList<QuestionApiDao> questions = new RealmList<>();
            if (isSectionInterview()) {
                SectionApiDao currentSection = getCurrentSection();
                for (SectionApiDao section : interviewApiDao.getSections()) {
                    if (section.getId() == currentSection.getId()) {
                        for (QuestionApiDao question : section.getSectionQuestions()) {
                            if (question.getSub_questions() != null && !question.getSub_questions().isEmpty()) {
                                questions.addAll(question.getSub_questions());
                            } else {
                                questions.add(question);
                            }
                        }
                    }
                }
            } else {
                for (QuestionApiDao question : interviewApiDao.getQuestions()) {
                    if (question.getSub_questions() != null && !question.getSub_questions().isEmpty()) {
                        questions.addAll(question.getSub_questions());
                    } else {
                        questions.add(question);
                    }
                }
            }

            if (!questions.isEmpty()) {
                for (int i = 0; i < questions.size(); i++) {
                    QuestionApiDao question = questions.get(i);
                    if (question.getId() == questionId) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    public QuestionApiDao getNextQuestion() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            int questionIndex = getQuestionIndex() + 1;
            if (isSectionInterview()) {
                SectionApiDao currentSection = getCurrentSection();
                if (questionIndex < currentSection.getSectionQuestions().size()) {
                    return currentSection.getSectionQuestions().get(questionIndex);
                } else {
                    return null;
                }
            } else {
                if (questionIndex < interviewApiDao.getQuestions().size()) {
                    return interviewApiDao.getQuestions().get(questionIndex);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public QuestionApiDao getPrevQuestion() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            int questionIndex = getQuestionIndex() - 1;
            if (isSectionInterview()) {
                SectionApiDao currentSection = getCurrentSection();
                if (questionIndex >= 0) {
                    return currentSection.getSectionQuestions().get(questionIndex);
                } else {
                    return null;
                }
            } else {
                if (questionIndex >= 0) {
                    return interviewApiDao.getQuestions().get(questionIndex);
                } else {
                    return null;
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
                questionInfo.setAttempt(nextQuestion.getTakesCount());
                if (nextQuestion.getSub_questions() != null && !nextQuestion.getSub_questions().isEmpty()) {
                    questionInfo.resetSubIndex();
                }
            } else {
                questionInfo.resetAttempt();
            }
            questionInfo.increaseIndex();

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
                QuestionApiDao prevQuestion = getPrevQuestion();
                if (prevQuestion != null) {
                    RealmList<QuestionApiDao> subQuestions = prevQuestion.getSub_questions();
                    if (subQuestions != null && !subQuestions.isEmpty()) {
                        questionInfo.setSubIndex(subQuestions.size() - 1);
                    }
                }

                questionInfo.decreaseIndex();
            }

            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        } else {
            decreaseQuestionIndex();
        }
    }

    public void increaseQuestionSubIndex() {
        if (isPractice()) {
            return;
        }
        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            QuestionInfo questionInfo = getQuestionInfo();
            QuestionApiDao currentQuestion = getCurrentQuestion();

            if (questionInfo.getSubIndex() < currentQuestion.getSub_questions().size()) {
                questionInfo.increaseSubIndex();
            } else {
                QuestionApiDao nextQuestion = getNextQuestion();
                if (nextQuestion != null) {
                    questionInfo.setAttempt(nextQuestion.getTakesCount());
                    questionInfo.resetSubIndex();
                } else {
                    questionInfo.increaseIndex();
                    questionInfo.resetAttempt();
                }
            }

            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        } else {
            increaseQuestionSubIndex();
        }
    }

    public void decreaseQuestionSubIndex() {
        if (isPractice()) {
            return;
        }
        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            QuestionInfo questionInfo = getQuestionInfo();
            if (questionInfo.getSubIndex() > 0) {
                questionInfo.decreaseSubIndex();
            } else {
                QuestionApiDao prevQuestion = getPrevQuestion();
                if (prevQuestion != null) {
                    RealmList<QuestionApiDao> subQuestions = prevQuestion.getSub_questions();
                    if (subQuestions != null && !subQuestions.isEmpty()) {
                        questionInfo.setSubIndex(subQuestions.size() - 1);
                    }
                }
                questionInfo.decreaseIndex();
            }

            realm.copyToRealmOrUpdate(questionInfo);
            realm.commitTransaction();
        } else {
            decreaseQuestionSubIndex();
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
                sectionInfo.increaseIndex();
                LogUtil.addNewLog(getInterviewCode(),
                        new LogDao("Section",
                                "Section Index Increased"
                        )
                );
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
            updateQuestionInfo(0, 0, 0);
        } else {
            increaseSectionIndex();
        }
    }

    public void decreaseQuestionAttempt() {

        QuestionInfo questionInfo = getQuestionInfo();
        if (questionInfo == null) {
            updateQuestionInfo(0, 0, 0);
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

    public void decreaseMediaAttempt(QuestionApiDao question) {

        if (!realm.isInTransaction() && question != null) {
            realm.beginTransaction();

            question.decreaseMediaAttempt();
            int mediaAttempt = question.getMediaAttemptLeft();

            if (mediaAttempt > 0) {
                realm.copyToRealmOrUpdate(question);
            }
            realm.commitTransaction();
        } else {
            decreaseMediaAttempt(question);
        }
    }

    public boolean isLastAttempt() {
        return getQuestionAttempt() <= 0;
    }

    public boolean isNotLastQuestion() {
        QuestionApiDao currentQuestion = getCurrentQuestion();
        long currentQuestionId = currentQuestion.getId();
        List<QuestionApiDao> subQuestions = currentQuestion.getSub_questions();
        if (subQuestions != null && !subQuestions.isEmpty()) {
            QuestionApiDao currentSubQuestion = getCurrentSubQuestion();
            currentQuestionId = currentSubQuestion.getId();
        }
        QuestionApiDao lastQuestion = getQuestionsAndSubs().last();
        long lastQuestionId = lastQuestion.getId();
        return currentQuestionId != lastQuestionId;
    }

    public boolean isNotLastVideoQuestion() {
        if (isSectionInterview()) {
            SectionApiDao section = getCurrentSection();
            assert section != null;
            return getQuestionIndex() < section.getTotalQuestion();
        } else {
            return getQuestionIndex() < getTotalQuestion();
        }
    }

    public boolean isNotLastSubQuestion() {
        QuestionApiDao currentQuestion = getCurrentQuestion();
        int subIndex = getQuestionSubIndex();
        int totalSubQuestion = currentQuestion.getSub_questions().size();
        return subIndex < totalSubQuestion;
    }

    public boolean isNotLastSection() {
        return getSectionIndex() < getTotalSection();
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

    public void updateCheatSheetPath(SupportMaterialDao supportMaterialDao, String mediaPath) {

        if (!realm.isInTransaction()) {
            realm.beginTransaction();

            if (supportMaterialDao != null) {
                supportMaterialDao.setOfflinePath(mediaPath);
            }

            realm.copyToRealmOrUpdate(supportMaterialDao);
            realm.commitTransaction();
        } else {
            updateCheatSheetPath(supportMaterialDao, mediaPath);
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

            try {
                questionApiDao.setUploadProgress(progress);
            } catch (Exception e) {
                Timber.e("Exception %s", e.getMessage());
            }

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

            LogUtil.addNewLog(getInterviewCode(),
                    new LogDao("Video Upload Info",
                            String.format(Locale.getDefault(), "Upload file not found. Mark not answer for Question Id : %d", questionApiDao.getId())
                    )
            );

            Timber.d("Video with Question Id %s is mark not Answer", questionApiDao.getId());
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

    public void getVideoFile(Context context, String interviewCode, long questionId) {
        File directory = FileUtils.makeAndGetSubDirectory(context, interviewCode, "video");
        if (!directory.exists()) {
            directory.mkdir();
        }

        QuestionApiDao questionApiDao = getQuestionById(questionId);

        File rawFile = new File(directory, questionId + "_raw.mp4");
        if (rawFile.exists()) {
            markAsPending(questionApiDao, rawFile.getAbsolutePath());
            if (!ServiceUtils.isMyServiceRunning(context, VideoCompressService.class)) {
                if (!isRunningCompressing()) {
                    LogUtil.addNewLog(getInterviewCode(), new LogDao("Start compress", "From SDK " + questionId));
                    VideoCompressService.start(context, rawFile.getAbsolutePath(), questionId, getCurrentInterview().getInterviewCode());
                }
            }
        } else {
            File compressedFile = new File(directory, questionId + ".mp4");
            if (compressedFile.exists()) {
                markAsCompressed(questionApiDao);
                if (!isShowUpload() && UploadService.getTaskList().isEmpty()) {
                    if (questionApiDao.getUploadStatus().equals(UploadStatusType.COMPRESSED)) {
                        if (!ServiceUtils.isMyServiceRunning(context, SingleVideoUploadService.class)) {
                            if (!isRunningUploading()) {
                                LogUtil.addNewLog(getInterviewCode(), new LogDao("Start upload", "upload From sdk status compressed " + questionId));
                                SingleVideoUploadService.start(context, questionId, interviewCode);
                            }

                        }
                    }
                }
            } else {
                markNotAnswer(questionApiDao);
            }
        }
    }

    public void clearDb() {
        removeHawkSaved();
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            realm.delete(CandidateApiDao.class);
            realm.delete(CompanyApiDao.class);
            realm.delete(CustomFieldApiDao.class);
            realm.delete(CustomFieldResultApiDao.class);
            realm.delete(InformationApiDao.class);
            realm.delete(InterviewApiDao.class);
            realm.delete(JobApiDao.class);
            realm.delete(MediaDao.class);
            realm.delete(MultipleAnswerApiDao.class);
            realm.delete(PrevQuestionStateApiDao.class);
            realm.delete(QuestionApiDao.class);
            realm.delete(QuestionInfoApiDao.class);
            realm.delete(QuestionInfo.class);
            realm.delete(SectionApiDao.class);
            realm.delete(SectionInfo.class);
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

            fileOrDirectory.delete();
        }
    }

    public boolean isPractice() {
        return isPractice;
    }

    public boolean isSectionInterview() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        return interviewApiDao != null && interviewApiDao.getSections() != null && !interviewApiDao.getSections().isEmpty();
    }

    public boolean isRatingScale() {
        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (isSectionInterview()) {
            SectionApiDao currentSection = getCurrentSection();
            return currentSection != null && currentSection.getSub_type().equals(TestType.RATING_SCALE);
        } else {
            return interviewApiDao != null && interviewApiDao.getSub_type().equals(TestType.RATING_SCALE);
        }
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
            QuestionInfo questionInfo = new QuestionInfo(0, 0, 3, isPractice);
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

    public long getNeededStorage() {
        long questionsStorage = getTotalQuestionRawStorage();
        long uploadStorage = getTotalQuestionUpload();
        long requiredStorage = questionsStorage + uploadStorage;
        long availableStorage = getAvailableStorage();
        return availableStorage - requiredStorage;
    }

    public boolean isStorageEnough() {
        long questionsStorage = getTotalQuestionRawStorage();
        long uploadStorage = getTotalQuestionUpload();
        long requiredStorage = questionsStorage + uploadStorage;
        long availableStorage = getAvailableStorage();
        boolean isStorageEnough = availableStorage > requiredStorage;

        LogUtil.addNewLog(getInterviewCode(),
                new LogDao("isStorageEnough()",
                        "Available Storage : " + availableStorage + "Mb, " +
                                "Required Storage : " + requiredStorage + "Mb, " +
                                "Is Practice : " + isPractice()
                )
        );

        if (isProfile()) {
            return isStorageEnough;
        } else {
            if (isSectionInterview()) {
                if (isSectionHasVideo()) {
                    return isStorageEnough;
                } else {
                    return true;
                }
            } else {
                InterviewApiDao currentInterview = getCurrentInterview();

                if (currentInterview.getType().contains(InterviewType.INTERVIEW)) {
                    return isStorageEnough;
                } else {
                    return true;
                }
            }
        }
    }

    public int getTotalQuestionUpload() {

        if (isPractice()) {
            QuestionApiDao practiceQuestion = getPracticeQuestion();
            return practiceQuestion.getEstimationUpload();
        }

        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            int totalQuestionStorage = 0;

            RealmList<QuestionApiDao> allVideoQuestions = new RealmList<>();

            if (isSectionInterview()) {

                for (SectionApiDao section : interviewApiDao.getSections()) {
                    if (section.getType().equals(SectionType.INTERVIEW)) {
                        allVideoQuestions.addAll(section.getSectionQuestions());
                    }
                }

            } else {

                if (interviewApiDao.getType().contains(InterviewType.INTERVIEW)) {
                    allVideoQuestions.addAll(interviewApiDao.getQuestions());
                }

            }

            for (QuestionApiDao item : allVideoQuestions) {
                totalQuestionStorage += item.getEstimationUpload();
            }

            return totalQuestionStorage;
        } else {
            return 0;
        }
    }

    public int getTotalQuestionRawStorage() {

        if (isPractice()) {
            QuestionApiDao practiceQuestion = getPracticeQuestion();
            return practiceQuestion.getEstimationRawStorage();
        }

        InterviewApiDao interviewApiDao = getCurrentInterview();
        if (interviewApiDao != null) {
            int totalQuestionStorage = 0;

            RealmList<QuestionApiDao> allVideoQuestions = new RealmList<>();

            if (isSectionInterview()) {

                for (SectionApiDao section : interviewApiDao.getSections()) {
                    if (section.getType().equals(SectionType.INTERVIEW)) {
                        allVideoQuestions.addAll(section.getSectionQuestions());
                    }
                }

            } else {

                if (interviewApiDao.getType().contains(InterviewType.INTERVIEW)) {
                    allVideoQuestions.addAll(interviewApiDao.getQuestions());
                }

            }

            for (QuestionApiDao item : allVideoQuestions) {
                totalQuestionStorage += item.getEstimationRawStorage();
            }

            return totalQuestionStorage;
        } else {
            return 0;
        }
    }

    public void addSelectedAnswer(QuestionApiDao questionApiDao, MultipleAnswerApiDao answer) {
        if (!realm.isInTransaction()) {

            if (questionApiDao != null && questionApiDao.getMultiple_answers() != null) {

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
                            questionApiDao.setAnswerId(item.getId());
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

                realm.copyToRealmOrUpdate(questionApiDao);
                realm.commitTransaction();
            }

        } else {
            addSelectedAnswer(questionApiDao, answer);
        }
    }

    public void clearSelectedAnswer(QuestionApiDao questionApiDao) {

        if (questionApiDao.getMultiple_answers() != null && !questionApiDao.getMultiple_answers().isEmpty()) {

            RealmList<MultipleAnswerApiDao> selectedAnswer = new RealmList<>();

            RealmList<MultipleAnswerApiDao> multipleAnswer = questionApiDao.getMultiple_answers();
            RealmList<MultipleAnswerApiDao> newMultipleAnswer = new RealmList<>();

            for (MultipleAnswerApiDao item : multipleAnswer) {
                item.setSelected(false);

                MultipleAnswerApiDao multipleAnswerApiDao = item;
                if (!newMultipleAnswer.contains(multipleAnswerApiDao)) {
                    newMultipleAnswer.add(item);
                }
            }

            for (MultipleAnswerApiDao item : newMultipleAnswer) {
                selectedAnswer.add(item);
            }

            questionApiDao.setSelectedAnswer(selectedAnswer);
            questionApiDao.setMultiple_answers(newMultipleAnswer);
            questionApiDao.setAnswerId(0);
        }

        questionApiDao.setAnswered(false);
    }

    public void clearSelectedAnswerTransactions(QuestionApiDao questionApiDao) {
        if (!realm.isInTransaction()) {
            realm.beginTransaction();
            RealmList<MultipleAnswerApiDao> selectedAnswer = new RealmList<>();

            if (questionApiDao.getMultiple_answers() != null) {

                RealmList<MultipleAnswerApiDao> multipleAnswer = questionApiDao.getMultiple_answers();
                RealmList<MultipleAnswerApiDao> newMultipleAnswer = new RealmList<>();

                for (MultipleAnswerApiDao item : multipleAnswer) {
                    if (questionApiDao.isMultipleChoice()) {
                        item.setSelected(false);
                    } else {
                        item.setSelected(false);
                    }

                    MultipleAnswerApiDao multipleAnswerApiDao = item;
                    if (!newMultipleAnswer.contains(multipleAnswerApiDao)) {
                        newMultipleAnswer.add(item);
                    }
                }

                for (MultipleAnswerApiDao item : newMultipleAnswer) {
                    selectedAnswer.add(item);
                }

                questionApiDao.setSelectedAnswer(selectedAnswer);
                questionApiDao.setMultiple_answers(newMultipleAnswer);
            }

            questionApiDao.setAnswered(false);
            realm.copyToRealmOrUpdate(questionApiDao);
            realm.commitTransaction();
        } else {
            clearSelectedAnswer(questionApiDao);
        }
    }

    public void addClearSelectedAnswer(QuestionApiDao questionApiDao, RealmList<Integer> answerIds) {

        RealmList<MultipleAnswerApiDao> selectedAnswer = new RealmList<>();

        if (questionApiDao.getMultiple_answers() != null) {

            RealmList<MultipleAnswerApiDao> multipleAnswer = questionApiDao.getMultiple_answers();
            RealmList<MultipleAnswerApiDao> newMultipleAnswer = new RealmList<>();

            for (Integer answerId : answerIds) {
                for (MultipleAnswerApiDao item : multipleAnswer) {
                    if (questionApiDao.isMultipleChoice()) {
                        item.setSelected(false);
                    } else {
                        item.setSelected(false);
                    }

                    MultipleAnswerApiDao multipleAnswerApiDao = item;
                    if (!newMultipleAnswer.contains(multipleAnswerApiDao)) {
                        newMultipleAnswer.add(item);
                    }
                }
            }

            for (MultipleAnswerApiDao item : newMultipleAnswer) {
                selectedAnswer.add(item);
            }

            questionApiDao.setSelectedAnswer(selectedAnswer);
            questionApiDao.setMultiple_answers(newMultipleAnswer);
        }

        questionApiDao.setAnswered(false);
    }

    private void addSelectedAnswer(QuestionApiDao questionApiDao, RealmList<Integer> answerIds) {

        RealmList<MultipleAnswerApiDao> selectedAnswer = new RealmList<>();

        if (questionApiDao.getMultiple_answers() != null) {

            RealmList<MultipleAnswerApiDao> multipleAnswer = questionApiDao.getMultiple_answers();
            RealmList<MultipleAnswerApiDao> newMultipleAnswer = new RealmList<>();

            for (Integer answerId : answerIds) {
                for (MultipleAnswerApiDao item : multipleAnswer) {
                    if (questionApiDao.isMultipleChoice()) {
                        if (item.getId() == answerId) {
                            questionApiDao.setAnswerId(answerId);
                            item.setSelected(true);
                        }
                    } else {
                        if (item.getId() == answerId) {
                            item.setSelected(true);
                            questionApiDao.setAnswerId(answerId);
                        }
                    }


                    MultipleAnswerApiDao multipleAnswerApiDao = item;
                    if (!newMultipleAnswer.contains(multipleAnswerApiDao)) {
                        newMultipleAnswer.add(item);
                    }
                }
            }

            for (MultipleAnswerApiDao item : newMultipleAnswer) {
                if (item.isSelected()) {
                    selectedAnswer.add(item);
                }
            }

            questionApiDao.setSelectedAnswer(selectedAnswer);
            questionApiDao.setMultiple_answers(newMultipleAnswer);
        }

        if (!selectedAnswer.isEmpty()) {
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
        List<QuestionApiDao> questions = new ArrayList<>();
        if (isSectionInterview()) {
            for (SectionApiDao section : interviewApiDao.getSections()) {
                if (section.getMedia() != null) {
                    if (section.getMedia().getOfflinePath() == null) {
                        haveMediaToDownload = true;
                    }
                }
                for (SupportMaterialDao cheatsheet : section.getSupport_materials()) {
                    if (cheatsheet.getUrl() != null) {
                        if (cheatsheet.getOfflinePath() == null) {
                            haveMediaToDownload = true;
                        }
                    }
                }

                for (QuestionApiDao question : section.getSectionQuestions()) {
                    if (question.getMedia() != null) {
                        if (question.getMedia().getOfflinePath() == null) {
                            haveMediaToDownload = true;
                        }
                    }
                }
                questions.addAll(section.getSectionQuestions());
            }
        } else {
            questions.addAll(interviewApiDao.getQuestions());
        }

        for (QuestionApiDao question : questions) {
            if (question.getMedia() != null) {
                if (question.getMedia().getOfflinePath() == null) {
                    haveMediaToDownload = true;
                }
            }

            for (QuestionApiDao subQuestion : question.getSub_questions()) {
                if (subQuestion.getMedia() != null) {
                    if (subQuestion.getMedia().getOfflinePath() == null) {
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
