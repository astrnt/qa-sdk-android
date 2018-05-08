package co.astrnt.qasdk.dao;

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
    //    private List<?> prevQuestStates;
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

//    public List<?> getPrevQuestStates() {
//        return prevQuestStates;
//    }
//
//    public void setPrevQuestStates(List<?> prevQuestStates) {
//        this.prevQuestStates = prevQuestStates;
//    }

    public void setInterviewAttempt(int interviewAttempt) {
        this.interviewAttempt = interviewAttempt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
