package co.astrnt.qasdk.repository;

import java.util.HashMap;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.dao.SectionApiDao;
import co.astrnt.qasdk.dao.SummarySectionApiDao;
import co.astrnt.qasdk.type.ElapsedTime;
import co.astrnt.qasdk.type.ElapsedTimeType;
import co.astrnt.qasdk.utils.LogUtil;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
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

        if (!astrntSDK.isSelfPace()) {
            updateElapsedTime(ElapsedTimeType.PREPARATION, sectionApiDao.getId());
        }

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/section/start)",
                        "Start Section, number" + (astrntSDK.getSectionIndex() + 1) +
                                ", sectionId = " + sectionApiDao.getId()
                )

        );
        astrntSDK.saveLastApiCall("(/section/start)");

        astrntSDK.updateSectionOnGoing(sectionApiDao, true);
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

        if (!astrntSDK.isSelfPace()) {
            updateElapsedTime(ElapsedTimeType.SECTION, sectionApiDao.getId());
        }

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/section/stop)",
                        "Finish Section, number " + (astrntSDK.getSectionIndex() + 1) +
                                ", sectionId = " + sectionApiDao.getId()
                )

        );
        astrntSDK.saveLastApiCall("(/section/stop)");

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
        final String interviewCode = astrntSDK.getInterviewCode();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("type", type);
        map.put("ref_id", String.valueOf(refId));

        String token = interviewApiDao.getToken();

        LogUtil.addNewLog(interviewCode,
                new LogDao("Hit API (/interview/update/elapsedTime)",
                        "Update Elapsed Time Section, type = " + type +
                                ", number " + (astrntSDK.getSectionIndex() + 1)
                                + ", refId = " + refId

                )
        );

        astrntSDK.saveLastApiCall("(/interview/update/elapsedTime)");

        mAstronautApi.getApiService().updateElapsedTime(token, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new MyObserver<BaseApiDao>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                    }

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

                        astrntSDK.saveLastApiCall("(Elapsed Time Section)");
                    }

                    @Override
                    public void onApiResultOk(BaseApiDao apiDao) {
                        Timber.d(apiDao.getMessage());
                    }
                });
    }

    public Observable<BaseApiDao> addLastSeen(int questionId) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("question_id", String.valueOf(questionId));

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/question/last_seen)",
                        "Add Last Seen, number " + (astrntSDK.getQuestionIndex() + 1) +
                                ", questionId = " + questionId
                )
        );
        astrntSDK.saveLastApiCall("(/question/last_seen)");

        return mAstronautApi.getApiService().addLastSeen(token, map);
    }

}
