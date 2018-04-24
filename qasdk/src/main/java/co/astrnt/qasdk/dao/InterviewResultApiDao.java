package co.astrnt.qasdk.dao;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class InterviewResultApiDao extends BaseApiDao implements RealmModel {

    @PrimaryKey
    private long id;
    private String token;
    private String interview_code;
    private InterviewApiDao interview;
    private InformationApiDao information;
    private InvitationVideoApiDao invitation_video;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
