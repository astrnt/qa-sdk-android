package co.astrnt.qasdk;

import java.util.HashMap;

import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by deni rohimat on 06/04/18.
 */
public interface ApiService {

    @FormUrlEncoded
    @POST("interview")
    Observable<InterviewResultApiDao> enterCode(@Field("interview_code") String interviewCode,
                                                @Field("device") String device,
                                                @Field("version") int version);

    @FormUrlEncoded
    @POST("user/register")
    Observable<InterviewResultApiDao> registerUser(@FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("interview/start")
    Observable<InterviewStartApiDao> startInterview(@FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("interview/finish")
    Observable<BaseApiDao> finishInterview(@FieldMap HashMap<String, String> data);

}