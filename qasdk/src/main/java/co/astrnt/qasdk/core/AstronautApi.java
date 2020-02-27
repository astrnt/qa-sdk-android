package co.astrnt.qasdk.core;

import android.content.res.Resources;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import co.astrnt.qasdk.ApiService;
import co.astrnt.qasdk.dao.InformationApiDao;
import co.astrnt.qasdk.dao.InformationDeserializer;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
        httpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .addHeader("device", device)
                        .addHeader("os", os)
                        .addHeader("browser", "")
                        .addHeader("screenresolution", getScreenWidth() + "x" + getScreenHeight())
                        .build();
                return chain.proceed(request);
            }
        });

        if (isDebugable) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClientBuilder.addInterceptor(loggingInterceptor);
        }

        Gson gson = new GsonBuilder().registerTypeAdapter(InformationApiDao.class, new InformationDeserializer()).create();

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
