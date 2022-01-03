package co.astrnt.qasdk.dao

import co.astrnt.qasdk.type.CustomField
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class CustomFieldApiDao : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var label: String? = null
    private var is_mandatory = 0
    var description: String? = null
    var input_type = 0

    @get:CustomField
    var input_name: String? = null
    var minWords = 0
    var maxWords = 0
    var options: RealmList<String>? = null
    private var hasMultiple = 0
    var maxOptions = 0

    //Supported
    var answer: String? = null
    lateinit var answers: RealmList<String>

    constructor() {}
    constructor(id: Long, label: String?, is_mandatory: Int, description: String?, input_type: Int, input_name: String?, minWords: Int, maxWords: Int, options: RealmList<String>?, hasMultiple: Int, maxOptions: Int) {
        this.id = id
        this.label = label
        this.is_mandatory = is_mandatory
        this.description = description
        this.input_type = input_type
        this.input_name = input_name
        this.minWords = minWords
        this.maxWords = maxWords
        this.options = options
        this.hasMultiple = hasMultiple
        this.maxOptions = maxOptions
    }

    constructor(id: Long, label: String?, is_mandatory: Int, description: String?, input_type: Int, input_name: String?, minWords: Int, maxWords: Int, hasMultiple: Int, maxOptions: Int) {
        this.id = id
        this.label = label
        this.is_mandatory = is_mandatory
        this.description = description
        this.input_type = input_type
        this.input_name = input_name
        this.minWords = minWords
        this.maxWords = maxWords
        this.hasMultiple = hasMultiple
        this.maxOptions = maxOptions
    }

    constructor(id: Long, label: String?, is_mandatory: Int, description: String?, input_type: Int, input_name: String?, minWords: Int, maxWords: Int, options: RealmList<String>?, hasMultiple: Int, maxOptions: Int, answer: String?, answers: RealmList<String>) {
        this.id = id
        this.label = label
        this.is_mandatory = is_mandatory
        this.description = description
        this.input_type = input_type
        this.input_name = input_name
        this.minWords = minWords
        this.maxWords = maxWords
        this.options = options
        this.hasMultiple = hasMultiple
        this.maxOptions = maxOptions
        this.answer = answer
        this.answers = answers
    }

    val isMandatory: Boolean
        get() = is_mandatory != 0

    fun setIs_mandatory(is_mandatory: Int) {
        this.is_mandatory = is_mandatory
    }

    fun getHasMultiple(): Boolean {
        return hasMultiple != 0
    }

    fun setHasMultiple(hasMultiple: Int) {
        this.hasMultiple = hasMultiple
    }
}