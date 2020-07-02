package co.astrnt.qasdk.repository;

import java.util.HashMap;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.SummaryApiDao;
import co.astrnt.qasdk.dao.post.RegisterPost;
import co.astrnt.qasdk.utils.LogUtil;
import io.reactivex.Observable;

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

        if (param.getCustom_fields() != null) {
            for (int i = 0; i < param.getCustom_fields().size(); i++) {
                RegisterPost.CustomFieldsPost customFieldsPost = param.getCustom_fields().get(i);

                map.put("custom_fields[" + i + "][id]", String.valueOf(customFieldsPost.getId()));
                map.put("custom_fields[" + i + "][value]", customFieldsPost.getValue());
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

        astrntSDK.setContinueInterview(true);
        return mAstronautApi.getApiService().startInterview(token, map);
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

        astrntSDK.setFinishInterview(true);
        return mAstronautApi.getApiService().finishInterview(token, map);
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

        return mAstronautApi.getApiService().gdprComplied("", map);
    }

}
