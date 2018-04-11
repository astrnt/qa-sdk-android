package co.astrnt.qasdk;

import co.astrnt.qasdk.dao.InterviewResultApiDao;
import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by deni rohimat on 06/04/18.
 */
public interface ApiService {

    @FormUrlEncoded
    @POST("interview")
    Observable<InterviewResultApiDao> enterCode(@Field("interviewCode") String interviewCode,
                                                @Field("device") String device,
                                                @Field("version") int version);

    @FormUrlEncoded
    @POST("register")
    Observable<InterviewResultApiDao> registerUser(@Field("interviewCode") String interviewCode,
                                                   @Field("full_name") String full_name,
                                                   @Field("version") int version);

}