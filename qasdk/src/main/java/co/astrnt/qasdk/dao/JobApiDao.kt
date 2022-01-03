package co.astrnt.qasdk.dao

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class JobApiDao : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var title: String? = null
    private var requireCv = 0
    var description: String? = null
    var location: String? = null
    var requirement: String? = null
    var responsibility: String? = null
    var documentType: String? = null
    var type: String? = null

    @SerializedName("recruitment_type")
    var recruitmentType: String? = null

    @SerializedName("job_url")
    var jobUrl: String? = null

    fun isRequireCv(): Boolean {
        return requireCv != 0
    }

    fun setRequireCv(requireCv: Int) {
        this.requireCv = requireCv
    }
}