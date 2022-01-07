package co.astrnt.qasdk.dao

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class CandidateApiDao : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var email: String? = null
    var fullname: String? = null
}