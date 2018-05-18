package co.astrnt.qasdk.dao;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 17/05/18.
 */
public class MultipleAnswerApiDao extends RealmObject {

    @PrimaryKey
    private long id;
    private String label;
    private int image_id;
    private int question_id;
    private String image_url;

    //additional field not from API, just for locally checked
    private boolean selected;

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

    public int getImage_id() {
        return image_id;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }

    public int getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(int question_id) {
        this.question_id = question_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
