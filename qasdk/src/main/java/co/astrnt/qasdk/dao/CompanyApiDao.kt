package co.astrnt.qasdk.dao

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class CompanyApiDao : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var coverPicture: String? = null
    var logo: String? = null
    var requirement: String? = null
    var nda: String? = null
    var title: String? = null
    var finish_page_url: String? = null
}