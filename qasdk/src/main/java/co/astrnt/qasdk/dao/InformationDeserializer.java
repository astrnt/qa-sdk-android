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
        int questionIndex = 0;
        if (json.getAsJsonObject().get("question_index") != null) {
            questionIndex = json.getAsJsonObject().get("question_index").getAsInt();
        }
        int interviewSubIndex = 0;
        if (json.getAsJsonObject().get("interviewSubIndex") != null) {
            interviewSubIndex = json.getAsJsonObject().get("interviewSubIndex").getAsInt();
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
                try {
                    sectionIndex = json.getAsJsonObject().get("section_index").getAsInt();
                } catch (Exception e) {
                    sectionIndex = 0;
                }
            }
            int preparationTime = 0;
            if (json.getAsJsonObject().get("preparation_time") != null) {
                preparationTime = json.getAsJsonObject().get("preparation_time").getAsInt();
            }
            String sectionInfo = "";
            if (json.getAsJsonObject().get("section_info") != null) {
                sectionInfo = json.getAsJsonObject().get("section_info").getAsString();
            }

            JsonElement questionsInfo;
            if (json.getAsJsonObject().get("questions_info") != null) {
                questionsInfo = json.getAsJsonObject().get("questions_info");

                if (questionsInfo.isJsonArray()) {
                    //MCQ Type
                    QuestionInfoMcqApiDao[] questionInfoMcqApiDaos = context.deserialize(questionsInfo.getAsJsonArray(), QuestionInfoMcqApiDao[].class);
                    return new InformationApiDao(interviewIndex, interviewSubIndex, questionIndex, finished, status, sectionIndex, preparationTime, sectionDurationLeft, sectionInfo, message, questionInfoMcqApiDaos);
                } else if (questionsInfo.isJsonObject()) {
                    //Video Type
                    QuestionInfoApiDao questionInfoApiDao = context.deserialize(questionsInfo.getAsJsonObject(), QuestionInfoApiDao.class);
                    return new InformationApiDao(questionIndex, finished, status, sectionIndex, preparationTime, sectionDurationLeft, sectionInfo, message, questionInfoApiDao);
                } else {
                    return new InformationApiDao(interviewIndex, interviewSubIndex, questionIndex, finished, status, sectionIndex, preparationTime, sectionDurationLeft, sectionInfo, message);
                }
            } else {
                return new InformationApiDao(interviewIndex, interviewSubIndex, questionIndex, finished, status, sectionIndex, preparationTime, sectionDurationLeft, sectionInfo, message);
            }
        } else {
            QuestionInfoMcqApiDao[] questionInfoMcqApiDaos = null;
            QuestionInfoApiDao questionInfoApiDao = null;
            JsonElement questionsInfo;
            if (json.getAsJsonObject().get("questions_info") != null) {
                questionsInfo = json.getAsJsonObject().get("questions_info");
                if (questionsInfo.isJsonArray()) {
                    //MCQ Type
                    questionInfoMcqApiDaos = context.deserialize(questionsInfo.getAsJsonArray(), QuestionInfoMcqApiDao[].class);
                } else if (questionsInfo.isJsonObject()) {
                    //Video Type
                    questionInfoApiDao = context.deserialize(questionsInfo.getAsJsonObject(), QuestionInfoApiDao.class);
                }
            }

            //Non Section
            PrevQuestionStateApiDao[] prevQuestionStateApiDaos = context.deserialize(prevQuestionState.getAsJsonArray(), PrevQuestionStateApiDao[].class);

            if (questionInfoMcqApiDaos != null) {
                return new InformationApiDao(questionIndex, finished, interviewIndex, interviewSubIndex, interviewAttempt, status, message, prevQuestionStateApiDaos, questionInfoMcqApiDaos);
            }  else {
                return new InformationApiDao(questionIndex, finished, interviewIndex, interviewSubIndex, interviewAttempt, status, message, prevQuestionStateApiDaos);
            }
        }
    }
}