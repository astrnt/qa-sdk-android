package co.astrnt.qasdk.repository;

import java.util.HashMap;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.MultipleAnswerApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.type.InterviewType;
import io.reactivex.Observable;
import io.realm.RealmList;

/**
 * Created by deni rohimat on 27/04/18.
 */
public class QuestionRepository extends BaseRepository {
    private final AstronautApi mAstronautApi;

    public QuestionRepository(AstronautApi astronautApi) {
        mAstronautApi = astronautApi;
    }

    public Observable<BaseApiDao> addQuestionAttempt(QuestionApiDao currentQuestion) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("question_id", String.valueOf(currentQuestion.getId()));
        //        TODO: check section
//        map.put("section_id", interviewApiDao.getToken());

        return mAstronautApi.getApiService().addAttempt(token, map);
    }

    public Observable<BaseApiDao> finishQuestion(QuestionApiDao currentQuestion) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("question_id", String.valueOf(currentQuestion.getId()));
        //        TODO: check section
//        map.put("section_id", mInterviewApiDao.getToken());

        return mAstronautApi.getApiService().finishQuestion(token, map);
    }

    public Observable<BaseApiDao> answerQuestion(QuestionApiDao currentQuestion) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("question_id", String.valueOf(currentQuestion.getId()));
        map.put("invite_id", String.valueOf(interviewApiDao.getInvite_id()));
        
        if (interviewApiDao.getType().equals(InterviewType.CLOSE_TEST)) {
            map.put("interview_type", "test");
        } else {
            map.put("interview_type", "section");
        }

        RealmList<MultipleAnswerApiDao> selectedAnswer = currentQuestion.getSelectedAnswer();
        
        if (selectedAnswer != null) {
            for (int i = 0; i < selectedAnswer.size(); i++) {
                MultipleAnswerApiDao answerItem = selectedAnswer.get(i);

                assert answerItem != null;
                map.put("answer_ids[" + i + "]", String.valueOf(answerItem.getId()));
            }
        }

        return mAstronautApi.getApiService().finishQuestion(token, map);
    }

}
