package co.astrnt.qasdk.dao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import co.astrnt.qasdk.constants.Constants;
import co.astrnt.qasdk.type.MediaTypes;
import co.astrnt.qasdk.type.TestType;
import co.astrnt.qasdk.type.UploadStatusState;
import co.astrnt.qasdk.type.UploadStatusType;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import timber.log.Timber;

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
    private String maxTime;
    private int job_id;
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private int image_id;
    private String image_url;
    private String type_child;
    private RealmList<MultipleAnswerApiDao> multiple_answers;
    private RealmList<MultipleAnswerApiDao> answers;
    private String type_parent;

    //this field below is additional field for Video Interview
    private String videoPath;
    private String uploadStatus;
    private double uploadProgress;

    //this field below is additional field for Free Text Question
    private int min_length;
    private int max_length;
    private String answer;

    //this field below is additional field for MCQ
    private int timeLeft;
    private long answerId;
    private Boolean isAnswered;
    private RealmList<MultipleAnswerApiDao> selectedAnswer;
    private RealmList<Long> answer_ids;
    @Expose
    @SerializedName("offline_path")
    private String offlinePath;

    //media file
    private MediaDao media;
    private @MediaTypes String media_type;
    private int media_id;
    private int media_attempt;
    private int media_attempt_left;
    private boolean ready_answer;
    private String aptitude_option_type;
    private String display_answer;

    private RealmList<QuestionApiDao> sub_questions;

    private boolean isRetake;
    private boolean show_title = true;
    private int disable_copy_paste = 0;

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

    public boolean isAnswer() {
        return ready_answer;
    }

    public void setIsAnswer(boolean is_answer) {
        this.ready_answer = is_answer;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public RealmList<Long> getAnswer_ids() {
        return answer_ids;
    }

    public void setAnswer_ids(RealmList<Long> answer_ids) {
        this.answer_ids = answer_ids;
    }

    public boolean isShow_title() {
        return show_title;
    }

    public int getDisableCopyPaste() {
        return disable_copy_paste;
    }

    public void setDisable_copy_paste(int disable_copy_paste) {
        this.disable_copy_paste = disable_copy_paste;
    }

    public void setShow_title(boolean show_title) {
        this.show_title = show_title;
    }

    public int getMaxTime() {
        if (getSub_questions() != null && !getSub_questions().isEmpty()) {
            int maxTime = 0;
            for (QuestionApiDao question : getSub_questions()) {
                if (question.getTimeLeft() != 0) {
                    maxTime += question.getTimeLeft();
                } else {
                    maxTime += Integer.parseInt(question.maxTime);
                }
            }
            return maxTime;
        } else {
            if (getTimeLeft() != 0) {
                return getTimeLeft();
            } else {
                return Integer.parseInt(maxTime);
            }
        }
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = String.valueOf(maxTime);
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

    public RealmList<MultipleAnswerApiDao> getAnswers() {
        return answers;
    }

    public void setAnswers(RealmList<MultipleAnswerApiDao> answers) {
        this.answers = answers;
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

    public @UploadStatusState
    String getUploadStatus() {
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

    public void setUploadStatus(@UploadStatusState String uploadStatus) {
        try {
            this.uploadStatus = uploadStatus;
        } catch (Exception e) {

        }

    }

    public double getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(double uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    //MCQ method support

    private int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public boolean isMultipleChoice() {
        return getType_child() != null && getType_child().equals("multiple_options_for_test");
    }

    public boolean isAnswered() {
        if (sub_questions != null && !sub_questions.isEmpty()) {
            return false;
        } else {
            if (type_child != null && type_child.equals(TestType.FREE_TEXT)) {
                return answer != null && !answer.isEmpty();
            } else {
                return selectedAnswer != null && selectedAnswer.size() > 0;
            }
        }
    }

    public void setAnswered(boolean answered) {
        this.isAnswered = answered;
    }

    public RealmList<MultipleAnswerApiDao> getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(RealmList<MultipleAnswerApiDao> selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(long answerId) {
        this.answerId = answerId;
    }

    //Free Text method support
    public int getMin_length() {
        return min_length;
    }

    public void setMin_length(int min_length) {
        this.min_length = min_length;
    }

    public int getMax_length() {
        return max_length;
    }

    public void setMax_length(int max_length) {
        this.max_length = max_length;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public MediaDao getMedia() {
        return media;
    }

    public void setMedia(MediaDao media) {
        this.media = media;
    }

    public @MediaTypes
    String getMediaType() {
        return media_type;
    }

    public void setMediaType(@MediaTypes String media_type) {
        this.media_type = media_type;
    }

    public int getMediaId() {
        return media_id;
    }

    public void setMediaId(int media_id) {
        this.media_id = media_id;
    }

    public String getAptitude_option_type() {
        return aptitude_option_type;
    }

    public void setAptitude_option_type(String aptitude_option_type) {
        this.aptitude_option_type = aptitude_option_type;
    }

    public String getDisplay_answer() {
        return display_answer;
    }

    public void setDisplay_answer(String display_answer) {
        this.display_answer = display_answer;
    }

    public int getMediaAttempt() {
        return media_attempt;
    }

    public void setMediaAttempt(int media_attempt) {
        this.media_attempt = media_attempt;
    }

    public int getMediaAttemptLeft() {
        return media_attempt_left;
    }

    public void setMediaAttemptLeft(int media_attempt_left) {
        this.media_attempt_left = media_attempt_left;
    }

    public String getOfflinePath() {
        return offlinePath;
    }

    public void setOfflinePath(String offlinePath) {
        this.offlinePath = offlinePath;
    }

    public void resetMediaAttempt() {
        this.media_attempt_left = 3;
    }

    public void decreaseMediaAttempt() {
        this.media_attempt_left--;
    }

    public RealmList<QuestionApiDao> getSub_questions() {
        return sub_questions;
    }

    public void setSub_questions(RealmList<QuestionApiDao> sub_questions) {
        this.sub_questions = sub_questions;
    }

    public boolean isRetake() {
        return isRetake;
    }

    public void setRetake(boolean retake) {
        isRetake = retake;
    }

    public int getEstimationUpload() {
        int totalQuestionStorage = 0;

        if (getUploadStatus().equals(UploadStatusType.UPLOADED)) {
            return totalQuestionStorage;
        }

        if (getMaxTime() <= 30) {
            totalQuestionStorage += Constants.UPLOAD_ESTIMATION_30S;
        } else if (getMaxTime() <= 45) {
            totalQuestionStorage += Constants.UPLOAD_ESTIMATION_45S;
        } else if (getMaxTime() <= 60) {
            totalQuestionStorage += Constants.UPLOAD_ESTIMATION_60S;
        } else if (getMaxTime() <= 120) {
            totalQuestionStorage += Constants.UPLOAD_ESTIMATION_120S;
        } else {
            totalQuestionStorage += Constants.UPLOAD_ESTIMATION_BIG;
        }
        return totalQuestionStorage;
    }

    public int getEstimationRawStorage() {
        int totalQuestionStorage = 0;

        if (getUploadStatus().equals(UploadStatusType.UPLOADED)) {
            return totalQuestionStorage;
        }

        if (getUploadStatus().equals(UploadStatusType.COMPRESSED)) {
            return getEstimationUpload();
        }

        if (getMaxTime() <= 30) {
            totalQuestionStorage += Constants.RAW_ESTIMATION_30S;
        } else if (getMaxTime() <= 45) {
            totalQuestionStorage += Constants.RAW_ESTIMATION_45S;
        } else if (getMaxTime() <= 60) {
            totalQuestionStorage += Constants.RAW_ESTIMATION_60S;
        } else if (getMaxTime() <= 120) {
            totalQuestionStorage += Constants.RAW_ESTIMATION_120S;
        } else {
            totalQuestionStorage += Constants.RAW_ESTIMATION_BIG;
        }
        return totalQuestionStorage;
    }


}
