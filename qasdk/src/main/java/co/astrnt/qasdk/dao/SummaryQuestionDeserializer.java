package co.astrnt.qasdk.dao;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SummaryQuestionDeserializer implements JsonDeserializer<SummaryQuestionApiDao> {

    @Override
    public SummaryQuestionApiDao deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject object = json.getAsJsonObject();

        if (object.get("question_id").isJsonArray()) {

            List<Integer> ids = new ArrayList<>();

            for (JsonElement item : object.get("question_id").getAsJsonArray()) {
                ids.add(item.getAsInt());
            }

            List<Integer> answerStatus = new ArrayList<>();
            for (JsonElement item : object.get("answered_counter").getAsJsonArray()) {
                answerStatus.add(item.getAsInt());
            }

            return new SummaryQuestionApiDao(ids, answerStatus);
        } else {
            int id = object.get("question_id").getAsInt();
            int answerStatus = object.get("answered_counter").getAsInt();

            return new SummaryQuestionApiDao(id, answerStatus);
        }
    }
}