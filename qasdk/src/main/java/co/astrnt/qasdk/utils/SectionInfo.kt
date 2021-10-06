package co.astrnt.qasdk.utils

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SectionInfo : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var index = 0

    constructor() {}
    constructor(index: Int) {
        this.index = index
    }

    fun increaseIndex() {
        index++
    }
}