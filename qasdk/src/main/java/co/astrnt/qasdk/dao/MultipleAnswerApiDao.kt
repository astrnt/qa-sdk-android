package co.astrnt.qasdk.dao

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MultipleAnswerApiDao : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var label: String? = null
    var image_id = 0
    var question_id = 0
    var image_url: String? = null

    //additional field not from API, just for locally checked
    var isSelected = false
}