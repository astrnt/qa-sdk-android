package co.astrnt.qasdk.dao;

public class SummaryQuestionApiDao {

    private int question_id;
    private int answered_counter;

    public int getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(int question_id) {
        this.question_id = question_id;
    }

    public int getAnswered_counter() {
        return answered_counter;
    }

    public void setAnswered_counter(int answered_counter) {
        this.answered_counter = answered_counter;
    }
}
