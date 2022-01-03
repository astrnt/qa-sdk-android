package co.astrnt.qasdk.dao

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey

class InterviewResultApiDao : BaseApiDao(), RealmModel {
    @PrimaryKey
    var id: Long = 0
    var token: String? = null
    var interview_code: String? = null
    var interview: InterviewApiDao? = null
    var information: InformationApiDao? = null
    var invitation_video: InvitationVideoApiDao? = null
    var welcomeVideo: WelcomeVideoDao? = null
    var gdpr_complied = 0
    var gdpr_text: String? = null
    var gdpr_aggrement_text: String? = null
    var compliance: String? = null
}