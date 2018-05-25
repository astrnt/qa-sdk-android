package co.astrnt.qasdk.utils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SectionInfo extends RealmObject {

    @PrimaryKey
    private long id;
    private int index;

    public SectionInfo() {
    }

    public SectionInfo(int index) {
        this.index = index;
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

    public void increaseIndex() {
        this.index++;
    }
}
