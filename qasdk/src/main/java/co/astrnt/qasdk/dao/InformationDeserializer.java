package co.astrnt.qasdk.dao;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class InformationDeserializer implements JsonDeserializer<InformationApiDao> {

    @Override
    public InformationApiDao deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        boolean finished = json.getAsJsonObject().get("finished").getAsBoolean();
        String status = json.getAsJsonObject().get("status").getAsString();
        String message = json.getAsJsonObject().get("message").getAsString();
        int interviewIndex = 0;
        if (json.getAsJsonObject().get("interviewIndex") != null) {
            interviewIndex = json.getAsJsonObject().get("interviewIndex").getAsInt();
        }
        int interviewAttempt = 0;
        if (json.getAsJsonObject().get("interviewAttempt") != null) {
            interviewAttempt = json.getAsJsonObject().get("interviewAttempt").getAsInt();
        }
        JsonElement prevQuestionState = null;
        if (json.getAsJsonObject().get("prevQuestStates") != null) {
            prevQuestionState = json.getAsJsonObject().get("prevQuestStates");
        }

        if (prevQuestionState == null) {
            //Section
            int sectionDurationLeft = 0;
            if (json.getAsJsonObject().get("section_duration_left") != null) {
                sectionDurationLeft = json.getAsJsonObject().get("section_duration_left").getAsInt();
            }
            int sectionIndex = 0;
            if (json.getAsJsonObject().get("section_index") != null) {
                sectionIndex = json.getAsJsonObject().get("section_index").getAsInt();
            }
            int preparationTime = 0;
            if (json.getAsJsonObject().get("preparation_time") != null) {
                preparationTime = json.getAsJsonObject().get("preparation_time").getAsInt();
            }
            String sectionInfo = json.getAsJsonObject().get("section_info").getAsString();

            JsonElement questionsInfo;
            if (json.getAsJsonObject().get("questions_info") != null) {
                questionsInfo = json.getAsJsonObject().get("questions_info");

                if (questionsInfo.isJsonArray()) {
                    //MCQ Type
                    QuestionInfoMcqApiDao[] questionInfoMcqApiDaos = context.deserialize(questionsInfo.getAsJsonArray(), QuestionInfoMcqApiDao[].class);
                    return new InformationApiDao(finished, status, sectionIndex, preparationTime, sectionDurationLeft, sectionInfo, message, questionInfoMcqApiDaos);
                } else if (questionsInfo.isJsonObject()) {
                    //Video Type
                    QuestionInfoApiDao questionInfoApiDao = context.deserialize(questionsInfo.getAsJsonObject(), QuestionInfoApiDao.class);
                    return new InformationApiDao(finished, status, sectionIndex, preparationTime, sectionDurationLeft, sectionInfo, message, questionInfoApiDao);
                } else {
                    return new InformationApiDao(finished, status, sectionIndex, preparationTime, sectionDurationLeft, sectionInfo, message);
                }
            } else {
                return new InformationApiDao(finished, status, sectionIndex, preparationTime, sectionDurationLeft, sectionInfo, message);
            }
        } else {
            //Non Section
            PrevQuestionStateApiDao[] prevQuestionStateApiDaos = context.deserialize(prevQuestionState.getAsJsonArray(), PrevQuestionStateApiDao[].class);
            return new InformationApiDao(finished, interviewIndex, interviewAttempt, status, message, prevQuestionStateApiDaos);
        }
    }
}