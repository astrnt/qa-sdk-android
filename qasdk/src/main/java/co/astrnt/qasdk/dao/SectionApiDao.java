package co.astrnt.qasdk.dao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import co.astrnt.qasdk.type.MediaTypes;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 25/05/18.
 */
public class SectionApiDao extends RealmObject {

    @PrimaryKey
    private long id;
    private String title;
    private String instruction;
    private String type; //SectionType
    private String sub_type;
    private int duration;
    private int duration_section;
    private int preparation_time;
    private int preparation_time_api;
    private boolean randomize;
    private String image;
    private String parent_id;
    private RealmList<QuestionApiDao> section_questions;
    private RealmList<SupportMaterialDao> support_materials;

    //for rating scale
    private RealmList<QuestionApiDao> sample_question;
    private boolean show_title;
    private int try_sample_question;
    private int instruction_time;
    private int sample_question_time;
    private String assessment_type ;
    private MediaDao media;
    private @MediaTypes String media_type;
    private int media_id;

    //additional field
    private boolean isOnGoing;
    private boolean showReview;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RealmList<SupportMaterialDao> getSupport_materials() {
        return support_materials;
    }

    public void setSupport_materials(RealmList<SupportMaterialDao> support_materials) {
        this.support_materials = support_materials;
    }

    public int getInstruction_time() {
        return instruction_time;
    }

    public void setInstruction_time(int instruction_time) {
        this.instruction_time = instruction_time;
    }

    public int getSample_question_time() {
        return sample_question_time;
    }

    public void setSample_question_time(int sample_question_time) {
        this.sample_question_time = sample_question_time;
    }

    public boolean isShow_title() {
        return show_title;
    }

    public void setShow_title(boolean show_title) {
        this.show_title = show_title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSub_type() {
        return sub_type;
    }

    public void setSub_type(String sub_type) {
        this.sub_type = sub_type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDurationSection() {
        return duration_section;
    }

    public void setDurationSection(int duration_section) {
        this.duration_section = duration_section;
    }

    public int getPreparationTime() {
        return preparation_time;
    }

    public void setPreparationTime(int preparation_time) {
        this.preparation_time = preparation_time;
    }

    public String getAssessment_type() {
        return assessment_type;
    }

    public void setAssessment_type(String assessment_type) {
        this.assessment_type = assessment_type;
    }

    public int getPreparationTimeApi() {
        return preparation_time_api;
    }

    public void setPreparationTimeApi(int preparation_time_api) {
        this.preparation_time_api = preparation_time_api;
    }

    public boolean isRandomize() {
        return randomize;
    }

    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public RealmList<QuestionApiDao> getSectionQuestions() {
        return section_questions;
    }

    public void setSectionQuestions(RealmList<QuestionApiDao> section_questions) {
        this.section_questions = section_questions;
    }

    public int getTotalQuestion() {
        RealmList<QuestionApiDao> questionsAndSubs = new RealmList<>();
        for (QuestionApiDao question : getSectionQuestions()) {
            if (question.getSub_questions() != null && !question.getSub_questions().isEmpty()) {
                questionsAndSubs.addAll(question.getSub_questions());
            } else {
                questionsAndSubs.add(question);
            }
        }
        return questionsAndSubs.size();
    }

    public int getTotalAttempt() {
        int totalAttempt = 0;

        for (QuestionApiDao item : getSectionQuestions()) {
            totalAttempt += item.getTakesCount();
        }
        return totalAttempt;
    }

    public int getTotalUpload() {
        return getTotalQuestion() * 5;
    }

    public int getEstimatedTime() {
        return getTotalAttempt() * 3;
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

// Support Method

    public boolean isOnGoing() {
        return isOnGoing;
    }

    public void setOnGoing(boolean onGoing) {
        isOnGoing = onGoing;
    }

    public boolean isShowReview() {
        return showReview;
    }

    public void setShowReview(boolean showReview) {
        this.showReview = showReview;
    }

    public RealmList<QuestionApiDao> getSample_question() {
        return sample_question;
    }

    public void setSample_question(RealmList<QuestionApiDao> sample_question) {
        this.sample_question = sample_question;
    }

    public int getTry_sample_question() {
        return try_sample_question;
    }

    public void setTry_sample_question(int try_sample_question) {
        this.try_sample_question = try_sample_question;
    }


}
