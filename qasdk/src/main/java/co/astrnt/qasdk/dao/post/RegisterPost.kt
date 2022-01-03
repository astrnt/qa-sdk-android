package co.astrnt.qasdk.dao.post

import co.astrnt.qasdk.type.CustomField

class RegisterPost {
    var job_id: Long = 0
    var company_id: Long = 0
    var interview_code: String? = null
    var fullname: String? = null
    var preferred_name: String? = null
    var email: String? = null
    var phone: String? = null
    var device: String? = null
    var version = 0
    var is_profile: Int? = 0
    var custom_fields: List<CustomFieldsPost>? = null

    class CustomFieldsPost {
        var id: Long = 0
        var value: String? = null
        var values: List<String>? = null

        @get:CustomField
        @CustomField
        var inputType: String? = null
    }
}