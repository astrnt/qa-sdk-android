package co.astrnt.qasdk.dao;

import io.realm.RealmObject;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class SectionQuestionApiDao extends RealmObject {
    private long id;
    private long section_id;

    public SectionQuestionApiDao() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSection_id() {
        return section_id;
    }

    public void setSection_id(long section_id) {
        this.section_id = section_id;
    }

}
