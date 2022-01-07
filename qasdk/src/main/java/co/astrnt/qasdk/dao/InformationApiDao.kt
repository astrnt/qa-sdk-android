package co.astrnt.qasdk.dao

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by deni rohimat on 19/04/18.
 */
open class InformationApiDao : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var isFinished = false
    var interviewIndex = 0
    var interviewSubIndex = 0
    var interviewAttempt = 0
    var status: String? = null
    var prevQuestStates: RealmList<PrevQuestionStateApiDao>? = null
    var sectionIndex = 0
    var question_index = 0
    var preparationTime = 0
    var sectionDurationLeft = 0
    var sectionInfo: String? = null
    var message: String? = null
    var questionsInfo: RealmList<QuestionInfoApiDao>? = null
        private set
    var questionsMcqInfo: RealmList<QuestionInfoMcqApiDao>? = null
        private set

    constructor() {}
    constructor(questionIndex: Int, finished: Boolean, interviewIndex: Int, interviewSubIndex: Int, interviewAttempt: Int, status: String?, message: String?, vararg prevQuestStates: PrevQuestionStateApiDao?) {
        question_index = questionIndex
        isFinished = finished
        this.status = status
        this.interviewIndex = interviewIndex
        this.interviewSubIndex = interviewSubIndex
        this.interviewAttempt = interviewAttempt
        this.message = message
        if (prevQuestStates != null) {
            this.prevQuestStates = RealmList(*prevQuestStates)
        }
    }

    constructor(questionIndex: Int, finished: Boolean, interviewIndex: Int, interviewSubIndex: Int, interviewAttempt: Int, status: String?, message: String?, prevQuestStates: Array<PrevQuestionStateApiDao?>?, vararg questionInfoMcqApiDao: QuestionInfoMcqApiDao?) {
        question_index = questionIndex
        isFinished = finished
        this.status = status
        this.interviewIndex = interviewIndex
        this.interviewSubIndex = interviewSubIndex
        this.interviewAttempt = interviewAttempt
        this.message = message
        if (prevQuestStates != null) {
            this.prevQuestStates = RealmList(*prevQuestStates)
        }
        if (questionInfoMcqApiDao != null) {
            questionsMcqInfo = RealmList(*questionInfoMcqApiDao)
        }
    }

    constructor(interviewIndex: Int, questionIndex: Int, finished: Boolean, status: String?, section_index: Int, preparation_time: Int, section_duration_left: Int, section_info: String?, message: String?) {
        this.interviewIndex = interviewIndex
        question_index = questionIndex
        isFinished = finished
        this.status = status
        sectionIndex = section_index
        preparationTime = preparation_time
        sectionDurationLeft = section_duration_left
        sectionInfo = section_info
        this.message = message
    }

    constructor(questionIndex: Int, finished: Boolean, status: String?, section_index: Int, preparation_time: Int, section_duration_left: Int, section_info: String?, message: String?, vararg questionInfos: QuestionInfoApiDao?) {
        question_index = questionIndex
        isFinished = finished
        this.status = status
        sectionIndex = section_index
        preparationTime = preparation_time
        sectionDurationLeft = section_duration_left
        sectionInfo = section_info
        this.message = message
        if (questionInfos != null) {
            questionsInfo = RealmList(*questionInfos)
        }
    }

    constructor(interviewIndex: Int, interviewSubIndex: Int, questionIndex: Int, finished: Boolean, status: String?, section_index: Int, preparation_time: Int, section_duration_left: Int, section_info: String?, message: String?, vararg questionInfoMcqApiDaos: QuestionInfoMcqApiDao?) {
        this.interviewIndex = interviewIndex
        this.interviewSubIndex = interviewSubIndex
        question_index = questionIndex
        isFinished = finished
        this.status = status
        sectionIndex = section_index
        preparationTime = preparation_time
        sectionDurationLeft = section_duration_left
        sectionInfo = section_info
        this.message = message
        if (questionInfoMcqApiDaos != null) {
            questionsMcqInfo = RealmList(*questionInfoMcqApiDaos)
        }
    }

    fun setQuestions_info(questions_info: RealmList<QuestionInfoApiDao>?) {
        questionsInfo = questions_info
    }

    fun setQuestions_mcq_info(questions_mcq_info: RealmList<QuestionInfoMcqApiDao>?) {
        questionsMcqInfo = questions_mcq_info
    }

    val isOnGoing: Boolean
        get() = sectionInfo == "ongoing"

    override fun toString(): String {
        return "InformationApiDao{" +
                "id=" + id +
                ", finished=" + isFinished +
                ", interviewIndex=" + interviewIndex +
                ", interviewSubIndex=" + interviewSubIndex +
                ", interviewAttempt=" + interviewAttempt +
                ", status='" + status + '\'' +
                ", prevQuestStates=" + prevQuestStates +
                ", section_index=" + sectionIndex +
                ", preparation_time=" + preparationTime +
                ", section_duration_left=" + sectionDurationLeft +
                ", section_info='" + sectionInfo + '\'' +
                ", message='" + message + '\'' +
                ", questions_info=" + questionsInfo +
                ", questions_mcq_info=" + questionsMcqInfo +
                '}'
    }
}