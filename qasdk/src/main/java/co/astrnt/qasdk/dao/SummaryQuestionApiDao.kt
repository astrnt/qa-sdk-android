package co.astrnt.qasdk.dao

class SummaryQuestionApiDao {
    var question_id = 0
    var answered_counter = 0

    //Arrays
    var question_ids: List<Int>? = null
    var answered_counters: List<Int>? = null

    constructor(question_id: Int, answered_counter: Int) {
        this.question_id = question_id
        this.answered_counter = answered_counter
    }

    constructor(question_ids: List<Int>?, answered_counters: List<Int>?) {
        this.question_ids = question_ids
        this.answered_counters = answered_counters
    }
}