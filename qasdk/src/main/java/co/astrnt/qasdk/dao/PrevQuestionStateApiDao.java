package co.astrnt.qasdk.dao;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 22/05/18.
 */
public class PrevQuestionStateApiDao extends RealmObject {

    @PrimaryKey
    private long id;
    private int duration_left_in_second;
    private int question_id;
    private String start_time;
    private int status_answered;
    private int status_finish;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDurationLeft() {
        return duration_left_in_second;
    }

    public void setDurationLeft(int duration_left_in_second) {
        this.duration_left_in_second = duration_left_in_second;
    }

    public int getQuestionId() {
        return question_id;
    }

    public void setQuestionId(int question_id) {
        this.question_id = question_id;
    }

    public String getStartTime() {
        return start_time;
    }

    public void setStartTime(String start_time) {
        this.start_time = start_time;
    }

    public boolean isAnswered() {
        return status_answered != 0;
    }

    public void setAnswered(boolean finished) {
        if (finished) {
            this.status_answered = 1;
        } else {
            this.status_answered = 0;
        }
    }

    public boolean isFinished() {
        return status_finish != 0;
    }

    public void setFinished(boolean finished) {
        if (finished) {
            this.status_finish = 1;
        } else {
            this.status_finish = 0;
        }
    }

    @Override
    public String toString() {
        return "PrevQuestionStateApiDao{" +
                "id=" + id +
                ", duration_left_in_second=" + duration_left_in_second +
                ", question_id=" + question_id +
                ", start_time='" + start_time + '\'' +
                ", status_answered=" + status_answered +
                ", status_finish=" + status_finish +
                '}';
    }
}
