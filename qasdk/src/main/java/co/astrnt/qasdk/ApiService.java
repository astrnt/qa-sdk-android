package co.astrnt.qasdk;

import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.post.RegisterPost;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Field;
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

    @POST("user/register")
    Observable<InterviewResultApiDao> registerUser(@Body RegisterPost param);

}