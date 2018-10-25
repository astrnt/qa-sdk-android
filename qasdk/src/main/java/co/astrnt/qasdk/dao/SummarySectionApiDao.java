package co.astrnt.qasdk.dao;

import java.util.List;

/**
 * Created by deni rohimat on 25/10/18.
 */
public class SummarySectionApiDao extends BaseApiDao {

    private List<ResultSectionSummaryApiDao> result;

    public List<ResultSectionSummaryApiDao> getResult() {
        return result;
    }

    public void setResult(List<ResultSectionSummaryApiDao> result) {
        this.result = result;
    }
}
