package co.astrnt.qasdk.dao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class MediaDao extends RealmObject {

    @Expose
    @SerializedName("height")
    private int height;
    @Expose
    @SerializedName("width")
    private int width;
    @Expose
    @SerializedName("media_thumbnail_url")
    private String mediaThumbnailUrl;
    @Expose
    @SerializedName("media_url")
    private String mediaUrl;
    @Expose
    @SerializedName("offline_path")
    private String offlinePath;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getMediaThumbnailUrl() {
        return mediaThumbnailUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getOfflinePath() {
        return offlinePath;
    }

    public void setOfflinePath(String offlinePath) {
        this.offlinePath = offlinePath;
    }
}
