package co.astrnt.qasdk;

import java.util.HashMap;

import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by deni rohimat on 06/04/18.
 */
public interface ApiService {

    @FormUrlEncoded
    @POST("interview")
    Observable<InterviewResultApiDao> enterCode(@Header("token") String token,
                                                @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("user/register")
    Observable<InterviewResultApiDao> registerUser(@Header("token") String token,
                                                   @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("interview/start")
    Observable<InterviewStartApiDao> startInterview(@Header("token") String token,
                                                    @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("interview/finish")
    Observable<BaseApiDao> finishInterview(@Header("token") String token,
                                           @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("cv/status")
    Observable<BaseApiDao> cvStatus(@Header("token") String token,
                                   @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("cv/start")
    Observable<BaseApiDao> cvStart(@Header("token") String token,
                                   @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("question/attempt")
    Observable<BaseApiDao> addAttempt(@Header("token") String token,
                                      @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("question/finish")
    Observable<BaseApiDao> finishQuestion(@Header("token") String token,
                                          @FieldMap HashMap<String, String> data);

    @GET("interview/ping")
    Observable<BaseApiDao> pingNetwork(@Header("token") String token);

}