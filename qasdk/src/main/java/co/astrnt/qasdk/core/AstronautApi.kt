package co.astrnt.qasdk.core

import android.content.Context
import android.content.res.Resources
import android.os.Build

import com.google.gson.GsonBuilder
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor

import java.util.concurrent.TimeUnit

import co.astrnt.qasdk.ApiService
import co.astrnt.qasdk.dao.CustomFieldApiDao
import co.astrnt.qasdk.dao.CustomFieldDeserializer
import co.astrnt.qasdk.dao.InformationApiDao
import co.astrnt.qasdk.dao.InformationDeserializer
import co.astrnt.qasdk.dao.SummaryQuestionApiDao
import co.astrnt.qasdk.dao.SummaryQuestionDeserializer
//import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory



class AstronautApi(baseUrl: String, isDebugable: Boolean, context: Context) {
    val apiService: ApiService

    companion object {
        private val screenWidth: Int
            get() = Resources.getSystem().displayMetrics.widthPixels
        private val screenHeight: Int
            get() = Resources.getSystem().displayMetrics.heightPixels
    }

    init {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val device = String.format("%s %s", manufacturer, model)
        val os = "Android " + Build.VERSION.RELEASE

        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.readTimeout(60, TimeUnit.SECONDS)
        httpClientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        httpClientBuilder.addInterceptor(Interceptor { chain: Interceptor.Chain ->
            val request = chain.request().newBuilder()
                .addHeader("device", device)
                .addHeader("os", os)
                .addHeader("browser", "")
                .addHeader("screenresolution", screenWidth.toString() + "x" + screenHeight)
                .build()
            chain.proceed(request)
        })
        if (isDebugable) {
            httpClientBuilder.addInterceptor(OkHttpProfilerInterceptor())
//            httpClientBuilder.addInterceptor(ChuckerInterceptor.Builder(context).build())
        }
        val gson = GsonBuilder()
            .registerTypeAdapter(InformationApiDao::class.java, InformationDeserializer())
            .registerTypeAdapter(CustomFieldApiDao::class.java, CustomFieldDeserializer())
            .registerTypeAdapter(
                SummaryQuestionApiDao::class.java,
                SummaryQuestionDeserializer()
            )
            .create()
        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClientBuilder.build())
            .baseUrl(baseUrl)
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }
}