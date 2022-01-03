package co.astrnt.qasdk.dao

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestionInfoMcqApiDao : RealmObject() {
    @SerializedName("question_id")
    @PrimaryKey
    var id: Long = 0
    var answer_ids: RealmList<Int>? = null
    var freetext_answer: String? = null

    override fun toString(): String {
        return "QuestionInfoMcqApiDao{" +
                "id=" + id +
                ", answer_ids=" + answer_ids +
                ", freetext_answer='" + freetext_answer + '\'' +
                '}'
    }
}