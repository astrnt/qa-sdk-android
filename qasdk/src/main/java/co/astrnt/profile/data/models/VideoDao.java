package co.astrnt.profile.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class VideoDao extends RealmObject implements Parcelable {

    @PrimaryKey
    @Expose
    @SerializedName("video_id")
    private int videoId;
    @Expose
    @SerializedName("views")
    private int views;
    @Expose
    @SerializedName("height")
    private int height;
    @Expose
    @SerializedName("width")
    private int width;
    @Expose
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    @Expose
    @SerializedName("video_url")
    private String videoUrl;
    @Expose
    @SerializedName("category_title")
    private String categoryTitle;
    @Expose
    @SerializedName("category_id")
    private int categoryId;
    @Expose
    @SerializedName("question_title")
    private String questionTitle;
    @Expose
    @SerializedName("question_id")
    private int questionId;
    @Expose
    @SerializedName("visibility_status")
    private String visibilityStatus;

    public VideoDao() {
    }

    protected VideoDao(Parcel in) {
        views = in.readInt();
        height = in.readInt();
        width = in.readInt();
        thumbnailUrl = in.readString();
        videoUrl = in.readString();
        categoryTitle = in.readString();
        categoryId = in.readInt();
        questionTitle = in.readString();
        questionId = in.readInt();
        videoId = in.readInt();
        visibilityStatus = in.readString();
    }

    public static final Creator<VideoDao> CREATOR = new Creator<VideoDao>() {
        @Override
        public VideoDao createFromParcel(Parcel in) {
            return new VideoDao(in);
        }

        @Override
        public VideoDao[] newArray(int size) {
            return new VideoDao[size];
        }
    };

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public String getVisibilityStatus() {
        return visibilityStatus;
    }

    public void setVisibilityStatus(String visibilityStatus) {
        this.visibilityStatus = visibilityStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(views);
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeString(thumbnailUrl);
        dest.writeString(videoUrl);
        dest.writeString(categoryTitle);
        dest.writeInt(categoryId);
        dest.writeString(questionTitle);
        dest.writeInt(questionId);
        dest.writeInt(videoId);
        dest.writeString(visibilityStatus);
    }
}
