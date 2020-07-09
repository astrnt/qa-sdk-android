package co.astrnt.profile.data.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;

public class LoginResponse extends BaseApiResponse implements RealmModel {

    @PrimaryKey
    private long id;
    @Expose
    @SerializedName("videos")
    private RealmList<VideoDao> videos;
    @Expose
    @SerializedName("introduction_question")
    private QuestionDao introductionQuestion;
    @Expose
    @SerializedName("profile_user")
    private ProfileUserDao profileUser;
    @Expose
    @SerializedName("token")
    private String token;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RealmList<VideoDao> getVideos() {
        return videos;
    }

    public void setVideos(RealmList<VideoDao> videos) {
        this.videos = videos;
    }

    public QuestionDao getIntroductionQuestion() {
        return introductionQuestion;
    }

    public void setIntroductionQuestion(QuestionDao introductionQuestion) {
        this.introductionQuestion = introductionQuestion;
    }

    public ProfileUserDao getProfileUser() {
        return profileUser;
    }

    public void setProfileUser(ProfileUserDao profileUser) {
        this.profileUser = profileUser;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
