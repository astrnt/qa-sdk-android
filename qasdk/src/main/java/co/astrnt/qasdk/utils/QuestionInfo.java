package co.astrnt.qasdk.utils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class QuestionInfo extends RealmObject {

    @PrimaryKey
    private long id;
    private int index;
    private int attempt;

    public QuestionInfo() {
    }

    public QuestionInfo(int index, int attempt) {
        this.index = index;
        this.attempt = attempt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public void increaseIndex() {
        this.index++;
    }

    public void resetAttempt() {
        this.attempt = 0;
    }

    public void decreaseAttempt() {
        this.attempt--;
    }
}
