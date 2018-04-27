package co.astrnt.qasdk.repository;

import java.util.HashMap;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import io.reactivex.Observable;

/**
 * Created by deni rohimat on 27/04/18.
 */
public class VideoRepository extends BaseRepository {
    private final AstronautApi mAstronautApi;

    public VideoRepository(AstronautApi astronautApi) {
        mAstronautApi = astronautApi;
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
