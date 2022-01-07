package co.astrnt.qasdk.dao

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.util.*

class SummaryQuestionDeserializer : JsonDeserializer<SummaryQuestionApiDao> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SummaryQuestionApiDao {
        val `object` = json.asJsonObject
        return if (`object`["question_id"].isJsonArray) {
            val ids: MutableList<Int> = ArrayList()
            for (item in `object`["question_id"].asJsonArray) {
                ids.add(item.asInt)
            }
            val answerStatus: MutableList<Int> = ArrayList()
            for (item in `object`["answered_counter"].asJsonArray) {
                answerStatus.add(item.asInt)
            }
            SummaryQuestionApiDao(ids, answerStatus)
        } else {
            val id = `object`["question_id"].asInt
            val answerStatus = `object`["answered_counter"].asInt
            SummaryQuestionApiDao(id, answerStatus)
        }
    }
}