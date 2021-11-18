package co.astrnt.qasdk

import co.astrnt.qasdk.dao.*
import io.reactivex.Observable
import retrofit2.http.*
import java.util.*

interface ApiService {
    @FormUrlEncoded
    @POST("v2/interview")
    fun enterCode(@Header("token") token: String,
                  @FieldMap data: HashMap<String, String>): Observable<InterviewResultApiDao>

    @FormUrlEncoded
    @POST("v2/user/register")
    fun registerUser(@Header("token") token: String,
                     @FieldMap data: HashMap<String, String?>): Observable<InterviewResultApiDao>

    @FormUrlEncoded
    @POST("v2/interview/start")
    fun startInterview(@Header("token") token: String,
                       @FieldMap data: HashMap<String, String?>): Observable<InterviewStartApiDao>

    @FormUrlEncoded
    @POST("v2/set/try-sample-question")
    fun setTrySampleQuestion(@Header("token") token: String,
                             @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/interview/finish")
    fun finishInterview(@Header("token") token: String,
                        @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/cv/status")
    fun cvStatus(@Header("token") token: String,
                 @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/cv/start")
    fun cvStart(@Header("token") token: String,
                @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/question/attempt")
    fun addAttempt(@Header("token") token: String,
                   @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/media/attempt")
    fun addMediaAttempt(@Header("token") token: String,
                        @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/question/answer")
    fun answerQuestion(@Header("token") token: String,
                       @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/question/finish")
    fun finishQuestion(@Header("token") token: String,
                       @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/section/start")
    fun startSection(@Header("token") token: String,
                     @FieldMap data: HashMap<String, String?>): Observable<InterviewStartApiDao>

    @FormUrlEncoded
    @POST("v2/section/stop")
    fun stopSection(@Header("token") token: String,
                    @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/interview/update/elapsedTime")
    fun updateElapsedTime(@Header("token") token: String,
                          @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/get/interview/summary")
    fun summarySection(@Header("token") token: String,
                       @FieldMap data: HashMap<String, String?>): Observable<SummarySectionApiDao>

    @FormUrlEncoded
    @POST("v2/get/interview/summary")
    fun summary(@Header("token") token: String,
                @FieldMap data: HashMap<String, String?>): Observable<SummaryApiDao>

    @GET("v2/interview/ping")
    fun pingNetwork(@Header("token") token: String): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/candidate/logs")
    fun sendLog(@Header("token") token: String,
                @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/user/gdpr_complied")
    fun gdprComplied(@Header("token") token: String,
                     @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>

    @FormUrlEncoded
    @POST("v2/question/last_seen")
    fun addLastSeen(@Header("token") token: String,
                    @FieldMap data: HashMap<String, String?>): Observable<BaseApiDao>
}