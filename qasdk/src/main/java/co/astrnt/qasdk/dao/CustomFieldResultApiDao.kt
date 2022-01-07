package co.astrnt.qasdk.dao

import io.realm.RealmList
import io.realm.RealmObject


open class CustomFieldResultApiDao : RealmObject() {
    lateinit var fields: RealmList<CustomFieldApiDao>
}