package co.astrnt.qasdk.repository;

import java.util.HashMap;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import co.astrnt.qasdk.dao.post.RegisterPost;
import io.reactivex.Observable;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class InterviewRepository extends BaseRepository {
    private final AstronautApi mAstronautApi;

    public InterviewRepository(AstronautApi astronautApi) {
        mAstronautApi = astronautApi;
    }

    public Observable<InterviewResultApiDao> enterCode(String interviewCode, int version) {
        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewCode);
        map.put("device", "android");
        map.put("version", String.valueOf(version));

        return mAstronautApi.getApiService().enterCode(map);
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

        return mAstronautApi.getApiService().registerUser(map);
    }

    public Observable<InterviewStartApiDao> startInterview() {

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", AstrntSDK.getCurrentInterview().getInterviewCode());
        map.put("token", AstrntSDK.getCurrentInterview().getToken());

        return mAstronautApi.getApiService().startInterview(map);
    }

    public Observable<BaseApiDao> finishInterview() {

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", AstrntSDK.getCurrentInterview().getInterviewCode());
        map.put("token", AstrntSDK.getCurrentInterview().getToken());

        return mAstronautApi.getApiService().finishInterview(map);
    }
}
