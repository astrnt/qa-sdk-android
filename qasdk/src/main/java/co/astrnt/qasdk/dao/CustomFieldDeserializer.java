package co.astrnt.qasdk.dao;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import io.realm.RealmList;

public class CustomFieldDeserializer implements JsonDeserializer<CustomFieldApiDao> {

    @Override
    public CustomFieldApiDao deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject field = json.getAsJsonObject();

        long id = field.get("id").getAsLong();
        String label = field.get("label").getAsString();
        String description = "";
        if (!field.get("description").isJsonNull()) {
            description = field.get("description").getAsString();
        }
        String input_name = field.get("input_name").getAsString();
        int input_type = field.get("input_type").getAsInt();
        int is_mandatory = field.get("is_mandatory").getAsInt();
        int maxOptions = 0;
        if (field.has("maxOptions") && !field.get("maxOptions").getAsString().equals("")) {
            maxOptions = field.get("maxOptions").getAsInt();
        }

        int maxWords = 0;
        if (field.has("maxWords")) {
            maxWords = field.get("maxWords").getAsInt();
        }

        int minWords = 0;
        if (field.has("minWords")) {
            minWords = field.get("minWords").getAsInt();
        }

        int hasMultiple = 0;
        if (field.has("hasMultiple")) {
            hasMultiple = field.get("hasMultiple").getAsInt();
        }

        if (field.has("options")) {

            RealmList<String> options = new RealmList<>();

            for (JsonElement element : field.get("options").getAsJsonArray()) {
                String option = element.getAsString();
                options.add(option);
            }

            return new CustomFieldApiDao(id, label, is_mandatory, description, input_type, input_name, minWords, maxWords, options, hasMultiple, maxOptions);
        } else {
            return new CustomFieldApiDao(id, label, is_mandatory, description, input_type, input_name, minWords, maxWords, hasMultiple, maxOptions);
        }
    }
}