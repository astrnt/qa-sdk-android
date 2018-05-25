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
    private String section_info;
    private String message;

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

}
