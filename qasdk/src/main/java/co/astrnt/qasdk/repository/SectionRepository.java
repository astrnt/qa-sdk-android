package co.astrnt.qasdk.repository;

import java.util.HashMap;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.SectionApiDao;
import co.astrnt.qasdk.dao.SummarySectionApiDao;
import co.astrnt.qasdk.type.ElapsedTime;
import co.astrnt.qasdk.type.ElapsedTimeType;
import co.astrnt.qasdk.utils.LogUtil;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by deni rohimat on 25/05/18.
 */
public class SectionRepository extends BaseRepository {

    public SectionRepository(AstronautApi astronautApi) {
        super(astronautApi);
    }

    public Observable<InterviewStartApiDao> startSection(SectionApiDao sectionApiDao) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("section_id", String.valueOf(sectionApiDao.getId()));
        String token = interviewApiDao.getToken();

        updateElapsedTime(ElapsedTimeType.PREPARATION, sectionApiDao.getId());

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API",
                        "Start Section, index = " + ((astrntSDK.getSectionIndex() + 1) + 1) +
                                ", sectionId = " + sectionApiDao.getId()
                )
        );

        astrntSDK.setContinueInterview(true);
        return mAstronautApi.getApiService().startSection(token, map);
    }

    public Observable<BaseApiDao> finishSection(SectionApiDao sectionApiDao) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("section_id", String.valueOf(sectionApiDao.getId()));
        String token = interviewApiDao.getToken();

        updateElapsedTime(ElapsedTimeType.SECTION, sectionApiDao.getId());

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API",
                        "Finish Section, index = " + (astrntSDK.getSectionIndex() + 1) +
                                ", sectionId = " + sectionApiDao.getId()
                )
        );

        sendLog();
        return mAstronautApi.getApiService().stopSection(token, map);
    }

    public Observable<SummarySectionApiDao> summarySection() {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        String token = interviewApiDao.getToken();

        return mAstronautApi.getApiService().summarySection(token, map);
    }

    private void updateElapsedTime(@ElapsedTime String type, long refId) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("type", type);
        map.put("ref_id", String.valueOf(refId));

        String token = interviewApiDao.getToken();

        final String interviewCode = interviewApiDao.getInterviewCode();
        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API",
                        "Update Elapsed Time Section, type = " + type +
                                ", index = " + (astrntSDK.getSectionIndex() + 1)
                                + ", refId = " + refId
                )
        );

        mAstronautApi.getApiService().updateElapsedTime(token, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new MyObserver<BaseApiDao>() {
                    @Override
                    public void onApiResultCompleted() {
                    }

                    @Override
                    public void onApiResultError(String title, String message, String code) {
                        Timber.e(message);

                        LogUtil.addNewLog(interviewCode,
                                new LogDao("Hit API (Elapsed Time Section)",
                                        "Error " + message
                                )
                        );
                    }

                    @Override
                    public void onApiResultOk(BaseApiDao apiDao) {
                        Timber.d(apiDao.getMessage());
                    }
                });
    }

}
