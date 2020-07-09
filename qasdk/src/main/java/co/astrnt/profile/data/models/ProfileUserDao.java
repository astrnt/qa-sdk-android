package co.astrnt.profile.data.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ProfileUserDao extends RealmObject {

    @PrimaryKey
    private long id;
    @Expose
    @SerializedName("profile_picture")
    private String profilePicture;
    @Expose
    @SerializedName("profile_url")
    private String profileUrl;
    @Expose
    @SerializedName("bio")
    private String bio;
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("email")
    private String email;

    private RealmList<VideoDao> videos;
    private QuestionDao introductionQuestion;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
