package co.astrnt.qasdk.utils

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestionInfo : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var index = 0
    var subIndex = 0
    var attempt = 0
    var isPractice = false

    constructor() {}
    constructor(index: Int, subIndex: Int, attempt: Int, isPractice: Boolean) {
        this.index = index
        this.subIndex = subIndex
        this.attempt = attempt
        this.isPractice = isPractice
    }

    fun increaseIndex() {
        index++
    }

    fun decreaseIndex() {
        index--
    }

    fun decreaseSubIndex() {
        subIndex--
    }

    fun increaseSubIndex() {
        subIndex++
    }

    fun resetSubIndex() {
        subIndex = 0
    }

    fun resetAttempt() {
        attempt = 0
    }

    fun decreaseAttempt() {
        attempt--
    }
}