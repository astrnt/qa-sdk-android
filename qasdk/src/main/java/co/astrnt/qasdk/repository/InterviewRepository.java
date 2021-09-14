package co.astrnt.qasdk.repository;

import java.util.HashMap;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.dao.SectionApiDao;
import co.astrnt.qasdk.dao.SummaryApiDao;
import co.astrnt.qasdk.dao.post.RegisterPost;
import co.astrnt.qasdk.type.CustomFiledType;
import co.astrnt.qasdk.type.ElapsedTime;
import co.astrnt.qasdk.type.ElapsedTimeType;
import co.astrnt.qasdk.utils.LogUtil;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class InterviewRepository extends BaseRepository {

    public InterviewRepository(AstronautApi astronautApi) {
        super(astronautApi);
    }

    public Observable<InterviewResultApiDao> enterCode(String interviewCode, int version) {
        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewCode);
        map.put("device", "android");
        map.put("version", String.valueOf(version));
        map.put("session_timer", String.valueOf(true));

        return mAstronautApi.getApiService().enterCode("", map);
    }

    public Observable<InterviewResultApiDao> registerUser(RegisterPost param) {
        param.setDevice("android");

        HashMap<String, String> map = new HashMap<>();
        map.put("job_id", String.valueOf(param.getJob_id()));
        map.put("company_id", String.valueOf(param.getCompany_id()));
        map.put("interview_code", param.getInterviewCode());
        map.put("fullname", param.getFullname());
        map.put("preferred_name", param.getPreferred_name());
        map.put("email", param.getEmail());
        map.put("phone", param.getPhone());
        map.put("device", param.getDevice());
        map.put("version", String.valueOf(param.getVersion()));
        map.put("session_timer", String.valueOf(true));

        if (param.getCustom_fields() != null) {
            for (int i = 0; i < param.getCustom_fields().size(); i++) {
                RegisterPost.CustomFieldsPost fieldsPost = param.getCustom_fields().get(i);

                map.put("custom_fields[" + i + "][id]", String.valueOf(fieldsPost.getId()));
                if (fieldsPost.getInputType().equals(CustomFiledType.CHECK_BOX)) {
                    for (int j = 0; j < fieldsPost.getValues().size(); j++) {
                        String item = fieldsPost.getValues().get(j);
                        map.put("custom_fields[" + i + "][value][" + j + "]", item);
                    }
                } else {
                    map.put("custom_fields[" + i + "][value]", fieldsPost.getValue());
                }
            }
        }

        return mAstronautApi.getApiService().registerUser("", map);
    }

    public Observable<InterviewStartApiDao> startInterview() {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        String token = interviewApiDao.getToken();

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/interview/start)",
                        "Start Interview"
                )
        );

        astrntSDK.saveLastApiCall("(/interview/start)");

        if (astrntSDK.isSectionInterview()) {
            astrntSDK.setContinueInterview(true);
        }
        if (astrntSDK.isSelfPace()) {
            astrntSDK.setContinueInterview(true);
        }
        astrntSDK.updateInterviewOnGoing(interviewApiDao, true);
        return mAstronautApi.getApiService().startInterview(token, map);
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

    public Observable<BaseApiDao> setTrySampleQuestion() {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));

        if (astrntSDK.isSectionInterview()) {
            SectionApiDao currentSection = astrntSDK.getCurrentSection();
            map.put("section_id", String.valueOf(currentSection.getId()));
            astrntSDK.updateSectionSampleQuestion(currentSection, 1);
        }

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/set/try-sample-question)",
                        "Sample Question"
                )
        );
        astrntSDK.saveLastApiCall("(/set/try-sample-question)");
        return mAstronautApi.getApiService().setTrySampleQuestion(token, map);
    }

    public Observable<BaseApiDao> finishSession(QuestionApiDao question) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/question/finish)",
                        "Finish Question, number " + (astrntSDK.getQuestionIndex() + 1) +
                                ", questionId = " + question.getId()
                )
        );
        astrntSDK.saveLastApiCall("(/question/finish)");

        return mAstronautApi.getApiService().finishQuestion(token, map);
    }

    public Observable<BaseApiDao> finishInterview() {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        String token = interviewApiDao.getToken();

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/interview/finish)",
                        "Finish Interview"
                )
        );

        astrntSDK.saveLastApiCall("(/interview/finish)");

        astrntSDK.setFinishInterview(true);
        return mAstronautApi.getApiService().finishInterview(token, map);
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

    public Observable<BaseApiDao> cvStatus() {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("job_id", String.valueOf(interviewApiDao.getJob().getId()));
        map.put("company_id", String.valueOf(interviewApiDao.getCompany().getId()));
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("interview_code", interviewApiDao.getInterviewCode());
        String token = interviewApiDao.getToken();

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/cv/status)",
                        "CV Status"
                )
        );

        astrntSDK.saveLastApiCall("(/cv/status)");

        return mAstronautApi.getApiService().cvStatus(token, map);
    }

    public Observable<BaseApiDao> cvStart() {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("job_id", String.valueOf(interviewApiDao.getJob().getId()));
        map.put("company_id", String.valueOf(interviewApiDao.getCompany().getId()));
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("interview_code", interviewApiDao.getInterviewCode());
        String token = interviewApiDao.getToken();

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/cv/start)",
                        "CV Start"
                )
        );

        astrntSDK.saveLastApiCall("(/cv/start)");
        astrntSDK.saveCvStartCalled(true);
        return mAstronautApi.getApiService().cvStart(token, map);
    }

    public Observable<BaseApiDao> pingNetwork() {
        return mAstronautApi.getApiService().pingNetwork("");
    }

    public Observable<SummaryApiDao> summary() {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        String token = interviewApiDao.getToken();

        return mAstronautApi.getApiService().summary(token, map);
    }

    public Observable<BaseApiDao> gdprComplied(String interviewCode) {
        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewCode);

        LogUtil.addNewLog(interviewCode,
                new LogDao("Hit API (/user/gdpr_complied)",
                        "GDPR Complied"
                )
        );

        astrntSDK.saveLastApiCall("(/user/gdpr_complied)");

        return mAstronautApi.getApiService().gdprComplied("", map);
    }

}
