package co.astrnt.qasdk.repository;

import java.util.HashMap;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import io.reactivex.Observable;

/**
 * Created by deni rohimat on 27/04/18.
 */
public class QuestionRepository extends BaseRepository {
    private final AstronautApi mAstronautApi;

    public QuestionRepository(AstronautApi astronautApi) {
        mAstronautApi = astronautApi;
    }

    public Observable<BaseApiDao> addQuestionAttempt() {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        QuestionApiDao currentQuestion = astrntSDK.getCurrentQuestion();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("question_id", String.valueOf(currentQuestion.getId()));
        //        TODO: check section
//        map.put("section_id", interviewApiDao.getToken());

        return mAstronautApi.getApiService().addAttempt(token, map);
    }

    public Observable<BaseApiDao> finishQuestion() {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        QuestionApiDao currentQuestion = astrntSDK.getCurrentQuestion();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("question_id", String.valueOf(currentQuestion.getId()));
        //        TODO: check section
//        map.put("section_id", mInterviewApiDao.getToken());

        return mAstronautApi.getApiService().finishQuestion(token, map);
    }

}
