package co.astrnt.qasdk.dao

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import io.realm.RealmList
import java.lang.reflect.Type

class CustomFieldDeserializer : JsonDeserializer<CustomFieldApiDao> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CustomFieldApiDao {
        val field = json.asJsonObject
        val id = field["id"].asLong
        val label = field["label"].asString
        var description: String? = ""
        if (!field["description"].isJsonNull) {
            description = field["description"].asString
        }
        val input_name = field["input_name"].asString
        val input_type = field["input_type"].asInt
        val is_mandatory = field["is_mandatory"].asInt
        var maxOptions = 0
        if (field.has("maxOptions") && field["maxOptions"].asString != "") {
            maxOptions = field["maxOptions"].asInt
        }
        var maxWords = 0
        if (field.has("maxWords")) {
            maxWords = field["maxWords"].asInt
        }
        var minWords = 0
        if (field.has("minWords")) {
            minWords = field["minWords"].asInt
        }
        var hasMultiple = 0
        if (field.has("hasMultiple")) {
            hasMultiple = field["hasMultiple"].asInt
        }
        return if (field.has("options")) {
            val options = RealmList<String>()
            for (element in field["options"].asJsonArray) {
                val option = element.asString
                options.add(option)
            }
            CustomFieldApiDao(id, label, is_mandatory, description, input_type, input_name, minWords, maxWords, options, hasMultiple, maxOptions)
        } else {
            CustomFieldApiDao(id, label, is_mandatory, description, input_type, input_name, minWords, maxWords, hasMultiple, maxOptions)
        }
    }
}