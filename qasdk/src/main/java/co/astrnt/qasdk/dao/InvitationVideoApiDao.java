package co.astrnt.qasdk.dao;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 19/04/18.
 */
public class InvitationVideoApiDao extends RealmObject {

    @PrimaryKey
    private long id;
    private String interview_video_url;
    private String interview_video_thumb_url;
    private int width;
    private int height;

    public InvitationVideoApiDao() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInterview_video_url() {
        return interview_video_url;
    }

    public void setInterview_video_url(String interview_video_url) {
        this.interview_video_url = interview_video_url;
    }

    public String getInterview_video_thumb_url() {
        return interview_video_thumb_url;
    }

    public void setInterview_video_thumb_url(String interview_video_thumb_url) {
        this.interview_video_thumb_url = interview_video_thumb_url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
