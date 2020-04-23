package co.astrnt.qasdk.dao;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 13/11/18.
 */
public class QuestionInfoMcqApiDao extends RealmObject {

    @SerializedName("question_id")
    @PrimaryKey
    private long id;
    private RealmList<Integer> answer_ids;
    private String freetext_answer;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RealmList<Integer> getAnswer_ids() {
        return answer_ids;
    }

    public void setAnswer_ids(RealmList<Integer> answer_ids) {
        this.answer_ids = answer_ids;
    }

    public String getFreetext_answer() {
        return freetext_answer;
    }

    public void setFreetext_answer(String freetext_answer) {
        this.freetext_answer = freetext_answer;
    }
}
