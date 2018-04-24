package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class InterviewResultApiDao extends BaseApiDao implements Parcelable {

    public static final Creator<InterviewResultApiDao> CREATOR = new Creator<InterviewResultApiDao>() {
        @Override
        public InterviewResultApiDao createFromParcel(Parcel source) {
            return new InterviewResultApiDao(source);
        }

        @Override
        public InterviewResultApiDao[] newArray(int size) {
            return new InterviewResultApiDao[size];
        }
    };
    private String token;
    private String interview_code;
    private InterviewApiDao interview;
    private InformationApiDao information;
    private InvitationVideoApiDao invitation_video;

    public InterviewResultApiDao() {
    }

    protected InterviewResultApiDao(Parcel in) {
        this.token = in.readString();
        this.interview_code = in.readString();
        this.interview = in.readParcelable(InterviewApiDao.class.getClassLoader());
        this.information = in.readParcelable(InformationApiDao.class.getClassLoader());
        this.invitation_video = in.readParcelable(InvitationVideoApiDao.class.getClassLoader());
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getInterview_code() {
        return interview_code;
    }

    public void setInterview_code(String interview_code) {
        this.interview_code = interview_code;
    }

    public InterviewApiDao getInterview() {
        return interview;
    }

    public void setInterview(InterviewApiDao interview) {
        this.interview = interview;
    }

    public InformationApiDao getInformation() {
        return information;
    }

    public void setInformation(InformationApiDao information) {
        this.information = information;
    }

    public InvitationVideoApiDao getInvitation_video() {
        return invitation_video;
    }

    public void setInvitation_video(InvitationVideoApiDao invitation_video) {
        this.invitation_video = invitation_video;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.token);
        dest.writeString(this.interview_code);
        dest.writeParcelable(this.interview, flags);
        dest.writeParcelable(this.information, flags);
        dest.writeParcelable(this.invitation_video, flags);
    }
}
