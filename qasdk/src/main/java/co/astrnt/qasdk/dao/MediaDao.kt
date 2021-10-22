package co.astrnt.qasdk.dao

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class MediaDao : RealmObject() {
    var height:Int? = 0
    var width: Int? = 0

    @Expose
    @SerializedName("media_thumbnail_url")
    var mediaThumbnailUrl: String? = null

    @Expose
    @SerializedName("media_url")
    var mediaUrl: String? = null

    @Expose
    @SerializedName("offline_path")
    var offlinePath: String? = null
}