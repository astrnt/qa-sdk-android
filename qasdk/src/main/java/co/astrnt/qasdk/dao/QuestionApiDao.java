package co.astrnt.qasdk.dao;

import co.astrnt.qasdk.type.UploadStatusType;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 19/04/18.
 */
public class QuestionApiDao extends RealmObject {

    @PrimaryKey
    private long id;
    private String title;
    private String tags;
    private int qType;
    private int takesCount;
    private int prepTime;
    private int maxTime;
    private int job_id;
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private int image_id;
    private String image_url;
    private String type_child;
    private RealmList<MultipleAnswerApiDao> multiple_answers;
    private String type_parent;

    //this field below is additional field for Video Interview
    private String videoPath;
    private String uploadStatus;
    private double uploadProgress;

    //this field below is additional field for MCQ
    private RealmList<MultipleAnswerApiDao> selectedAnswer;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(int maxTime) {
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

    public RealmList<MultipleAnswerApiDao> getMultiple_answers() {
        return multiple_answers;
    }

    public void setMultiple_answers(RealmList<MultipleAnswerApiDao> multiple_answers) {
        this.multiple_answers = multiple_answers;
    }

    public String getType_parent() {
        return type_parent;
    }

    public void setType_parent(String type_parent) {
        this.type_parent = type_parent;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getUploadStatus() {
        if (uploadStatus == null) {
            if (getVideoPath() != null) {
                return UploadStatusType.PENDING;
            } else {
                return UploadStatusType.NOT_ANSWER;
            }
        } else {
            return uploadStatus;
        }
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public double getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(double uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    //MCQ method support

    public boolean isMultipleChoice() {
        return getType_child().equals("multiple_options_for_test");
    }

    public boolean isAnswered() {
        return selectedAnswer != null && selectedAnswer.size() > 0;
    }

    public RealmList<MultipleAnswerApiDao> getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(RealmList<MultipleAnswerApiDao> selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }
}
