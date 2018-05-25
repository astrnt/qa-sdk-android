package co.astrnt.qasdk.repository;

import java.util.HashMap;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import co.astrnt.qasdk.dao.SectionApiDao;
import io.reactivex.Observable;

/**
 * Created by deni rohimat on 25/05/18.
 */
public class SectionRepository extends BaseRepository {
    private final AstronautApi mAstronautApi;

    public SectionRepository(AstronautApi astronautApi) {
        mAstronautApi = astronautApi;
    }

    public Observable<InterviewStartApiDao> startSection(SectionApiDao sectionApiDao) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("section_id", String.valueOf(sectionApiDao.getId()));
        String token = interviewApiDao.getToken();

        return mAstronautApi.getApiService().startSection(token, map);
    }

    public Observable<BaseApiDao> finishSection(SectionApiDao sectionApiDao) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("section_id", String.valueOf(sectionApiDao.getId()));
        String token = interviewApiDao.getToken();

        return mAstronautApi.getApiService().stopSection(token, map);
    }

}
