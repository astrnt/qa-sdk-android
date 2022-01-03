package co.astrnt.qasdk.dao

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class InvitationVideoApiDao : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var interview_video_url: String? = null
    var interview_video_thumb_url: String? = null
    var width = 0
    var height = 0
}
