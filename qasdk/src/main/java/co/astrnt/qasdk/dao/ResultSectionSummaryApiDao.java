package co.astrnt.qasdk.dao;

import java.util.List;

/**
 * Created by deni rohimat on 25/10/18.
 */
public class ResultSectionSummaryApiDao {

    private String section_title;
    private int section_id;
    private String type;
    private List<SummaryQuestionApiDao> summary;

    public String getSection_title() {
        return section_title;
    }

    public void setSection_title(String section_title) {
        this.section_title = section_title;
    }

    public int getSection_id() {
        return section_id;
    }

    public void setSection_id(int section_id) {
        this.section_id = section_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<SummaryQuestionApiDao> getSummary() {
        return summary;
    }

    public void setSummary(List<SummaryQuestionApiDao> summary) {
        this.summary = summary;
    }
}
