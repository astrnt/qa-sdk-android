package co.astrnt.qasdk.dao;

import java.util.List;

/**
 * Created by deni rohimat on 25/10/18.
 */
public class SummaryApiDao extends BaseApiDao {
    private List<SummaryQuestionApiDao> result;

    public List<SummaryQuestionApiDao> getResult() {
        return result;
    }

    public void setResult(List<SummaryQuestionApiDao> result) {
        this.result = result;
    }
}
