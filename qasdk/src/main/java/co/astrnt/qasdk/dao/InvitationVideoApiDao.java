package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deni rohimat on 19/04/18.
 */
public class InvitationVideoApiDao implements Parcelable {

    public static final Creator<InvitationVideoApiDao> CREATOR = new Creator<InvitationVideoApiDao>() {
        @Override
        public InvitationVideoApiDao createFromParcel(Parcel source) {
            return new InvitationVideoApiDao(source);
        }

        @Override
        public InvitationVideoApiDao[] newArray(int size) {
            return new InvitationVideoApiDao[size];
        }
    };
    private String interview_video_url;
    private String interview_video_thumb_url;
    private int width;
    private int height;

    public InvitationVideoApiDao() {
    }

    protected InvitationVideoApiDao(Parcel in) {
        this.interview_video_url = in.readString();
        this.interview_video_thumb_url = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.interview_video_url);
        dest.writeString(this.interview_video_thumb_url);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
    }
}
