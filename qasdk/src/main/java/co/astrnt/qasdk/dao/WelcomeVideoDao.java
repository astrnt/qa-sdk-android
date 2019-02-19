package co.astrnt.qasdk.dao;

/**
 * Created by deni rohimat on 19/02/19.
 */
public class WelcomeVideoDao {

    private String welcome_video_url;
    private String welcome_video_thumbnail_url;
    private int width;
    private int height;

    public String getWelcomeVideoUrl() {
        return welcome_video_url;
    }

    public void setWelcomeVideoUrl(String welcome_video_url) {
        this.welcome_video_url = welcome_video_url;
    }

    public String getWelcomeVideoThumbnailUrl() {
        return welcome_video_thumbnail_url;
    }

    public void setWelcomeVideoThumbnailUrl(String welcome_video_thumbnail_url) {
        this.welcome_video_thumbnail_url = welcome_video_thumbnail_url;
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
