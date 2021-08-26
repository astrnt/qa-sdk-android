package co.astrnt.qasdk.dao;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 28/05/18.
 */
public class QuestionInfoApiDao extends RealmObject {

    @PrimaryKey
    private long id;
    private boolean finished;
    private int interviewIndex;
    private int interviewSubIndex;
    private int interviewAttempt;
    private String status;
    private String message;
    private RealmList<PrevQuestionStateApiDao> prevQuestStates;

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

    public RealmList<PrevQuestionStateApiDao> getPrevQuestStates() {
        return prevQuestStates;
    }

    public void setPrevQuestStates(RealmList<PrevQuestionStateApiDao> prevQuestStates) {
        this.prevQuestStates = prevQuestStates;
    }

    @Override
    public String toString() {
        return "QuestionInfoApiDao{" +
                "id=" + id +
                ", finished=" + finished +
                ", interviewIndex=" + interviewIndex +
                ", interviewSubIndex=" + interviewSubIndex +
                ", interviewAttempt=" + interviewAttempt +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", prevQuestStates=" + prevQuestStates +
                '}';
    }
}
