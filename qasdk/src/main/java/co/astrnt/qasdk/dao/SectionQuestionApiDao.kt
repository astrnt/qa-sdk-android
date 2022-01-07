package co.astrnt.qasdk.dao

import io.realm.RealmObject

open class SectionQuestionApiDao : RealmObject() {
    var id: Long = 0
    var section_id: Long = 0
}