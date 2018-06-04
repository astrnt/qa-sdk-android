package co.astrnt.qasdk.dao;

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
    private int duration;
    private int preparation_time;
    private boolean randomize;
    private String image;
    private String parent_id;
    private RealmList<QuestionApiDao> section_questions;

    //additional field
    private int prepTimeLeft;
    private int timeLeft;
    private boolean isOnGoing;

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

    public int getDuration() {
        if (getTimeLeft() > 0) {
            return getTimeLeft();
        } else {
            return duration;
        }
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPreparationTime() {
        if (getPrepTimeLeft() > 0) {
            return getPrepTimeLeft();
        } else {
            return preparation_time;
        }
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
        return getSectionQuestions().size();
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

    // Support Method

    public int getTimerDuration() {
        if (preparation_time > 0) {
            return getPreparationTime();
        } else {
            return getDuration();
        }
    }

    private int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    private int getPrepTimeLeft() {
        return prepTimeLeft;
    }

    public void setPrepTimeLeft(int prepTimeLeft) {
        this.prepTimeLeft = prepTimeLeft;
    }

    public boolean isOnGoing() {
        return isOnGoing;
    }

    public void setOnGoing(boolean onGoing) {
        isOnGoing = onGoing;
    }
}
