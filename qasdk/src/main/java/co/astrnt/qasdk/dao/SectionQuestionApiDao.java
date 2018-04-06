package co.astrnt.qasdk.dao;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class SectionQuestionApiDao extends BaseApiDao {
    private String id;
    private String section_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSection_id() {
        return section_id;
    }

    public void setSection_id(String section_id) {
        this.section_id = section_id;
    }
}
