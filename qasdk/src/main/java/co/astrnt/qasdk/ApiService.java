package co.astrnt.qasdk;

import co.astrnt.qasdk.dao.InterviewApiDao;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by deni rohimat on 06/04/18.
 */
public interface ApiService {

    @FormUrlEncoded
    @POST("interview")
    Observable<InterviewApiDao> enterCode(@Field("interviewCode") String interviewCode);

}