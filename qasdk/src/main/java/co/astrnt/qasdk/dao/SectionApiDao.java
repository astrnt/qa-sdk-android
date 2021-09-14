package co.astrnt.qasdk.dao;

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
    private int preparation_time;
    private boolean randomize;
    private String image;
    private String parent_id;
    private RealmList<QuestionApiDao> section_questions;
    private RealmList<SupportMaterialDao> support_materials;

    //for rating scale
    private QuestionApiDao sample_question;
    private int try_sample_question;

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

    public int getPreparationTime() {
        return preparation_time;
    }

    public void setPreparationTime(int preparation_time) {
        this.preparation_time = preparation_time;
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

    public QuestionApiDao getSample_question() {
        return sample_question;
    }

    public void setSample_question(QuestionApiDao sample_question) {
        this.sample_question = sample_question;
    }

    public int getTry_sample_question() {
        return try_sample_question;
    }

    public void setTry_sample_question(int try_sample_question) {
        this.try_sample_question = try_sample_question;
    }
}
