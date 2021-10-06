package co.astrnt.qasdk.dao

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestionInfoApiDao : RealmObject() {
    @PrimaryKey
    private var id: Long = 0
    var isFinished = false
    var interviewIndex = 0
    var interviewSubIndex = 0
    var interviewAttempt = 0
    var status: String? = null
    var message: String? = null
    var prevQuestStates: RealmList<PrevQuestionStateApiDao>? = null

    override fun toString(): String {
        return "QuestionInfoApiDao{" +
                "id=" + id +
                ", finished=" + isFinished +
                ", interviewIndex=" + interviewIndex +
                ", interviewSubIndex=" + interviewSubIndex +
                ", interviewAttempt=" + interviewAttempt +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", prevQuestStates=" + prevQuestStates +
                '}'
    }
}