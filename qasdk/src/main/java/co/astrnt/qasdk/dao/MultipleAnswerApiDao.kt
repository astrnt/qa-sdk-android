package co.astrnt.qasdk.dao

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MultipleAnswerApiDao : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var label: String? = null
    var is_correct: Int? = 0
    var image_id = 0
    var question_id = 0
    var image_url: String? = null
    var answer_type: String? = null
    var show_label: String? = null
    var show_option: String? = null
    var option_type: String? = null
    var description: String? = null

    //additional field not from API, just for locally checked
    var isSelected = false
    @Expose
    @SerializedName("offline_path")
    var offlinePath: String = ""
}