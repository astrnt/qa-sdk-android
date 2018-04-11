package co.astrnt.qasdk.core;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.astrnt.qasdk.ApiService;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class AstronautApi {

    private ApiService mApiService;

    public AstronautApi(String baseUrl, boolean isDebugable) {

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.readTimeout(60, TimeUnit.SECONDS);
        httpClientBuilder.connectTimeout(60, TimeUnit.SECONDS);

        if (isDebugable) {
            Timber.plant(new Timber.DebugTree());

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Timber.d("API Request : %s", message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClientBuilder.addInterceptor(loggingInterceptor);
            httpClientBuilder.addInterceptor(new MyInterceptor());
        }

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .baseUrl(baseUrl)
                .build();

        mApiService = retrofit.create(ApiService.class);
    }

    public ApiService getApiService() {
        return mApiService;
    }

    private class MyInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request().newBuilder()
                    //TODO customize if need authorization like secret key
//                    .addHeader("timestamp", ts)
//                    .addHeader("deviceid", deviceId)
                    .build();

            Response response = chain.proceed(request);

            Timber.d("API Request : %s", request.url());

            return response;
        }
    }
}
