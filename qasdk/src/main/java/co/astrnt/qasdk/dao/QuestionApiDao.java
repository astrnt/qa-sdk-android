package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deni rohimat on 19/04/18.
 */
public class QuestionApiDao implements Parcelable {

    public static final Creator<QuestionApiDao> CREATOR = new Creator<QuestionApiDao>() {
        @Override
        public QuestionApiDao createFromParcel(Parcel source) {
            return new QuestionApiDao(source);
        }

        @Override
        public QuestionApiDao[] newArray(int size) {
            return new QuestionApiDao[size];
        }
    };
    private int id;
    private String title;
    private String tags;
    private int qType;
    private int takesCount;
    private String prepTime;
    private String maxTime;
    private int job_id;
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private int image_id;
    private String image_url;
    private String type_child;
    //    private List<?> multiple_answers;
    private String type_parent;

    public QuestionApiDao() {
    }

    protected QuestionApiDao(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.tags = in.readString();
        this.qType = in.readInt();
        this.takesCount = in.readInt();
        this.prepTime = in.readString();
        this.maxTime = in.readString();
        this.job_id = in.readInt();
        this.created_at = in.readString();
        this.updated_at = in.readString();
        this.deleted_at = in.readString();
        this.image_id = in.readInt();
        this.image_url = in.readString();
        this.type_child = in.readString();
        this.type_parent = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getqType() {
        return qType;
    }

    public void setqType(int qType) {
        this.qType = qType;
    }

    public int getTakesCount() {
        return takesCount;
    }

    public void setTakesCount(int takesCount) {
        this.takesCount = takesCount;
    }

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }

    public int getJob_id() {
        return job_id;
    }

    public void setJob_id(int job_id) {
        this.job_id = job_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(String deleted_at) {
        this.deleted_at = deleted_at;
    }

    public int getImage_id() {
        return image_id;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getType_child() {
        return type_child;
    }

    public void setType_child(String type_child) {
        this.type_child = type_child;
    }

    public String getType_parent() {
        return type_parent;
    }

    public void setType_parent(String type_parent) {
        this.type_parent = type_parent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeString(this.tags);
        dest.writeInt(this.qType);
        dest.writeInt(this.takesCount);
        dest.writeString(this.prepTime);
        dest.writeString(this.maxTime);
        dest.writeInt(this.job_id);
        dest.writeString(this.created_at);
        dest.writeString(this.updated_at);
        dest.writeString(this.deleted_at);
        dest.writeInt(this.image_id);
        dest.writeString(this.image_url);
        dest.writeString(this.type_child);
        dest.writeString(this.type_parent);
    }
}
