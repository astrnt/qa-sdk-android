package co.astrnt.qasdk;

import java.util.HashMap;

import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import co.astrnt.qasdk.dao.SummaryApiDao;
import co.astrnt.qasdk.dao.SummarySectionApiDao;
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
    @POST("v2/interview")
    Observable<InterviewResultApiDao> enterCode(@Header("token") String token,
                                                @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/user/register")
    Observable<InterviewResultApiDao> registerUser(@Header("token") String token,
                                                   @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/interview/start")
    Observable<InterviewStartApiDao> startInterview(@Header("token") String token,
                                                    @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/interview/finish")
    Observable<BaseApiDao> finishInterview(@Header("token") String token,
                                           @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/cv/status")
    Observable<BaseApiDao> cvStatus(@Header("token") String token,
                                    @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/cv/start")
    Observable<BaseApiDao> cvStart(@Header("token") String token,
                                   @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/question/attempt")
    Observable<BaseApiDao> addAttempt(@Header("token") String token,
                                      @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/question/answer")
    Observable<BaseApiDao> answerQuestion(@Header("token") String token,
                                          @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/question/finish")
    Observable<BaseApiDao> finishQuestion(@Header("token") String token,
                                          @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/section/start")
    Observable<InterviewStartApiDao> startSection(@Header("token") String token,
                                                  @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/section/stop")
    Observable<BaseApiDao> stopSection(@Header("token") String token,
                                       @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/interview/update/elapsedTime")
    Observable<BaseApiDao> updateElapsedTime(@Header("token") String token,
                                             @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/get/interview/summary")
    Observable<SummarySectionApiDao> summarySection(@Header("token") String token,
                                                    @FieldMap HashMap<String, String> data);

    @FormUrlEncoded
    @POST("v2/get/interview/summary")
    Observable<SummaryApiDao> summary(@Header("token") String token,
                                      @FieldMap HashMap<String, String> data);

    @GET("v2/interview/ping")
    Observable<BaseApiDao> pingNetwork(@Header("token") String token);

}