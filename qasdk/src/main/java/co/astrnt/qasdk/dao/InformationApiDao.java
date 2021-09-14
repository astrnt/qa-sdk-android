package co.astrnt.qasdk.dao;

import org.jetbrains.annotations.NotNull;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 19/04/18.
 */
public class InformationApiDao extends RealmObject {

    @PrimaryKey
    private long id;
    private boolean finished;
    private int interviewIndex;
    private int interviewSubIndex;
    private int interviewAttempt;
    private String status;
    private RealmList<PrevQuestionStateApiDao> prevQuestStates;
    private int section_index;
    private int question_index;
    private int preparation_time;
    private int section_duration_left;
    private String section_info;
    private String message;
    private RealmList<QuestionInfoApiDao> questions_info;
    private RealmList<QuestionInfoMcqApiDao> questions_mcq_info;

    public InformationApiDao() {
    }

    public InformationApiDao(int questionIndex, boolean finished, int interviewIndex, int interviewSubIndex, int interviewAttempt, String status, String message, PrevQuestionStateApiDao... prevQuestStates) {
        this.question_index = questionIndex;
        this.finished = finished;
        this.status = status;
        this.interviewIndex = interviewIndex;
        this.interviewSubIndex = interviewSubIndex;
        this.interviewAttempt = interviewAttempt;
        this.message = message;
        if (prevQuestStates != null) {
            this.prevQuestStates = new RealmList<>(prevQuestStates);
        }
    }

    public InformationApiDao(int questionIndex,boolean finished, int interviewIndex, int interviewSubIndex, int interviewAttempt, String status, String message, PrevQuestionStateApiDao[] prevQuestStates, QuestionInfoMcqApiDao... questionInfoMcqApiDao) {
        this.question_index = questionIndex;
        this.finished = finished;
        this.status = status;
        this.interviewIndex = interviewIndex;
        this.interviewSubIndex = interviewSubIndex;
        this.interviewAttempt = interviewAttempt;
        this.message = message;
        if (prevQuestStates != null) {
            this.prevQuestStates = new RealmList<>(prevQuestStates);
        }
        if (questionInfoMcqApiDao != null) {
            this.questions_mcq_info = new RealmList<>(questionInfoMcqApiDao);
        }
    }


    public InformationApiDao(int questionIndex, boolean finished, String status, int section_index, int preparation_time, int section_duration_left, String section_info, String message) {
        this.question_index = questionIndex;
        this.finished = finished;
        this.status = status;
        this.section_index = section_index;
        this.preparation_time = preparation_time;
        this.section_duration_left = section_duration_left;
        this.section_info = section_info;
        this.message = message;
    }

    public InformationApiDao(int questionIndex, boolean finished, String status, int section_index, int preparation_time, int section_duration_left, String section_info, String message, QuestionInfoApiDao... questionInfos) {
        this.question_index = questionIndex;
        this.finished = finished;
        this.status = status;
        this.section_index = section_index;
        this.preparation_time = preparation_time;
        this.section_duration_left = section_duration_left;
        this.section_info = section_info;
        this.message = message;
        if (questionInfos != null) {
            this.questions_info = new RealmList<>(questionInfos);
        }
    }

    public InformationApiDao(int questionIndex, boolean finished, String status, int section_index, int preparation_time, int section_duration_left, String section_info, String message, QuestionInfoMcqApiDao... questionInfoMcqApiDaos) {
        this.question_index = questionIndex;
        this.finished = finished;
        this.status = status;
        this.section_index = section_index;
        this.preparation_time = preparation_time;
        this.section_duration_left = section_duration_left;
        this.section_info = section_info;
        this.message = message;
        if (questionInfoMcqApiDaos != null) {
            this.questions_mcq_info = new RealmList<>(questionInfoMcqApiDaos);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getInterviewIndex() {
        return interviewIndex;
    }

    public void setInterviewIndex(int interviewIndex) {
        this.interviewIndex = interviewIndex;
    }

    public int getInterviewSubIndex() {
        return interviewSubIndex;
    }

    public void setInterviewSubIndex(int interviewSubIndex) {
        this.interviewSubIndex = interviewSubIndex;
    }

    public int getInterviewAttempt() {
        return interviewAttempt;
    }

    public void setInterviewAttempt(int interviewAttempt) {
        this.interviewAttempt = interviewAttempt;
    }

    public RealmList<PrevQuestionStateApiDao> getPrevQuestStates() {
        return prevQuestStates;
    }

    public void setPrevQuestStates(RealmList<PrevQuestionStateApiDao> prevQuestStates) {
        this.prevQuestStates = prevQuestStates;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSectionIndex() {
        return section_index;
    }

    public void setSectionIndex(int section_index) {
        this.section_index = section_index;
    }

    public int getPreparationTime() {
        return preparation_time;
    }

    public void setPreparationTime(int preparation_time) {
        this.preparation_time = preparation_time;
    }

    public int getSectionDurationLeft() {
        return section_duration_left;
    }

    public void setSectionDurationLeft(int section_duration_left) {
        this.section_duration_left = section_duration_left;
    }

    public String getSectionInfo() {
        return section_info;
    }

    public void setSectionInfo(String section_info) {
        this.section_info = section_info;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RealmList<QuestionInfoApiDao> getQuestionsInfo() {
        return questions_info;
    }

    public void setQuestions_info(RealmList<QuestionInfoApiDao> questions_info) {
        this.questions_info = questions_info;
    }

    public RealmList<QuestionInfoMcqApiDao> getQuestionsMcqInfo() {
        return questions_mcq_info;
    }

    public void setQuestions_mcq_info(RealmList<QuestionInfoMcqApiDao> questions_mcq_info) {
        this.questions_mcq_info = questions_mcq_info;
    }

    public int getQuestion_index() {
        return question_index;
    }

    public void setQuestion_index(int question_index) {
        this.question_index = question_index;
    }

    public boolean isOnGoing() {
        return getSectionInfo().equals("ongoing");
    }

    @NotNull
    @Override
    public String toString() {
        return "InformationApiDao{" +
                "id=" + id +
                ", finished=" + finished +
                ", interviewIndex=" + interviewIndex +
                ", interviewSubIndex=" + interviewSubIndex +
                ", interviewAttempt=" + interviewAttempt +
                ", status='" + status + '\'' +
                ", prevQuestStates=" + prevQuestStates +
                ", section_index=" + section_index +
                ", preparation_time=" + preparation_time +
                ", section_duration_left=" + section_duration_left +
                ", section_info='" + section_info + '\'' +
                ", message='" + message + '\'' +
                ", questions_info=" + questions_info +
                ", questions_mcq_info=" + questions_mcq_info +
                '}';
    }

}
