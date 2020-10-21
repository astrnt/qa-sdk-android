package co.astrnt.qasdk.core;

import android.content.res.Resources;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor;

import java.util.concurrent.TimeUnit;

import co.astrnt.qasdk.ApiService;
import co.astrnt.qasdk.dao.CustomFieldApiDao;
import co.astrnt.qasdk.dao.CustomFieldDeserializer;
import co.astrnt.qasdk.dao.InformationApiDao;
import co.astrnt.qasdk.dao.InformationDeserializer;
import co.astrnt.qasdk.dao.SummaryQuestionApiDao;
import co.astrnt.qasdk.dao.SummaryQuestionDeserializer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class AstronautApi {

    private ApiService mApiService;

    public AstronautApi(String baseUrl, boolean isDebugable) {

        final String manufacturer = Build.MANUFACTURER;
        final String model = Build.MODEL;
        final String device = String.format("%s %s", manufacturer, model);
        final String os = "Android " + Build.VERSION.RELEASE;

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.readTimeout(60, TimeUnit.SECONDS);
        httpClientBuilder.connectTimeout(60, TimeUnit.SECONDS);
        httpClientBuilder.addInterceptor(chain -> {
            Request request = chain.request().newBuilder()
                    .addHeader("device", device)
                    .addHeader("os", os)
                    .addHeader("browser", "")
                    .addHeader("screenresolution", getScreenWidth() + "x" + getScreenHeight())
                    .build();
            return chain.proceed(request);
        });

        if (isDebugable) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClientBuilder.addInterceptor(loggingInterceptor);
            httpClientBuilder.addInterceptor(new OkHttpProfilerInterceptor());
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(InformationApiDao.class, new InformationDeserializer())
                .registerTypeAdapter(CustomFieldApiDao.class, new CustomFieldDeserializer())
                .registerTypeAdapter(SummaryQuestionApiDao.class, new SummaryQuestionDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClientBuilder.build())
                .baseUrl(baseUrl)
                .build();

        mApiService = retrofit.create(ApiService.class);
    }

    private static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public ApiService getApiService() {
        return mApiService;
    }

}
