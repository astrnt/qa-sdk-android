package co.astrnt.qasdk.utils;

import co.astrnt.qasdk.dao.QuestionApiDao;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class QuestionInfo extends RealmObject {

    @PrimaryKey
    private long id;
    private int index;
    private int attempt;
    private QuestionApiDao currentQuestion;

    public QuestionInfo() {
    }

    public QuestionInfo(int index, QuestionApiDao currentQuestion) {
        this.index = index;
        this.attempt = currentQuestion.getTakesCount();
        this.currentQuestion = currentQuestion;
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

    public QuestionApiDao getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(QuestionApiDao currentQuestion) {
        this.currentQuestion = currentQuestion;
    }
}
