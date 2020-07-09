package co.astrnt.profile.data.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoryResponse extends BaseApiResponse {

    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("id")
    private int id;
    @Expose
    @SerializedName("questions")
    private List<QuestionDao> questions;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<QuestionDao> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDao> questions) {
        this.questions = questions;
    }
}
