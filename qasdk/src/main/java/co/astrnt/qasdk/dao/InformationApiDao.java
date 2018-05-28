package co.astrnt.qasdk.dao;

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
    private int interviewAttempt;
    private String status;
    private RealmList<PrevQuestionStateApiDao> prevQuestStates;
    private int section_index;
    private int preparation_time;
    private int section_duration_left;
    private String section_info;
    private String message;
    private QuestionInfoApiDao questions_info;

    public InformationApiDao() {
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

    public QuestionInfoApiDao getQuestionsInfo() {
        return questions_info;
    }

    public void setQuestionsInfo(QuestionInfoApiDao questions_info) {
        this.questions_info = questions_info;
    }

    public boolean isOnGoing() {
        return getSectionInfo().equals("ongoing");
    }

}
