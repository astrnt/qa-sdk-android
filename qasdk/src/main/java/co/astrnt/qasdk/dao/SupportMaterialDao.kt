package co.astrnt.qasdk.dao

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SupportMaterialDao : RealmObject() {
    @PrimaryKey
    var id = 0
    var size = 0
    var name: String? = null
    var title: String? = null
    var type: String? = null
    var url: String? = null

    @Expose
    @SerializedName("offline_path")
    var offlinePath: String? = null
}