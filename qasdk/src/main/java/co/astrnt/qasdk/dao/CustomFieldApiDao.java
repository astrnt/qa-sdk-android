package co.astrnt.qasdk.dao;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 09/04/18.
 */
public class CustomFieldApiDao extends RealmObject {
    @PrimaryKey
    private long id;
    private String label;
    private int is_mandatory;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isMandatory() {
        return is_mandatory != 0;
    }

    public void setIs_mandatory(int is_mandatory) {
        this.is_mandatory = is_mandatory;
    }
}
