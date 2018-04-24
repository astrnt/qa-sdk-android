package co.astrnt.qasdk.dao;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by deni rohimat on 09/04/18.
 */
public class CustomFieldResultApiDao extends RealmObject {
    private RealmList<CustomFieldApiDao> fields;

    public RealmList<CustomFieldApiDao> getFields() {
        return fields;
    }

    public void setFields(RealmList<CustomFieldApiDao> fields) {
        this.fields = fields;
    }
}
