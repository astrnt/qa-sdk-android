package co.astrnt.qasdk.dao;

import java.util.List;

public class SummaryQuestionApiDao {

    private int question_id;
    private int answered_counter;
    //Arrays
    private List<Integer> question_ids;
    private List<Integer> answered_counters;

    public SummaryQuestionApiDao(int question_id, int answered_counter) {
        this.question_id = question_id;
        this.answered_counter = answered_counter;
    }

    public SummaryQuestionApiDao(List<Integer> question_ids, List<Integer> answered_counters) {
        this.question_ids = question_ids;
        this.answered_counters = answered_counters;
    }

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

    public List<Integer> getQuestion_ids() {
        return question_ids;
    }

    public void setQuestion_ids(List<Integer> question_ids) {
        this.question_ids = question_ids;
    }

    public List<Integer> getAnswered_counters() {
        return answered_counters;
    }

    public void setAnswered_counters(List<Integer> answered_counters) {
        this.answered_counters = answered_counters;
    }
}
