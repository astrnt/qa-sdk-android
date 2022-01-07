package co.astrnt.qasdk.dao

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by deni rohimat on 22/05/18.
 */
open class PrevQuestionStateApiDao : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var durationLeft = 0
    var questionId = 0
    var startTime: String? = null
    private var status_answered = 0
    private var status_finish = 0
    var isAnswered: Boolean
        get() = status_answered != 0
        set(finished) {
            if (finished) {
                status_answered = 1
            } else {
                status_answered = 0
            }
        }
    var isFinished: Boolean
        get() = status_finish != 0
        set(finished) {
            if (finished) {
                status_finish = 1
            } else {
                status_finish = 0
            }
        }

    override fun toString(): String {
        return "PrevQuestionStateApiDao{" +
                "id=" + id +
                ", duration_left_in_second=" + durationLeft +
                ", question_id=" + questionId +
                ", start_time='" + startTime + '\'' +
                ", status_answered=" + status_answered +
                ", status_finish=" + status_finish +
                '}'
    }
}