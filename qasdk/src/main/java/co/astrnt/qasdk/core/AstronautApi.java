package co.astrnt.qasdk.core;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.astrnt.qasdk.ApiService;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class AstronautApi {

    ApiService mApiService;

    public AstronautApi(String baseUrl) {

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new ApiInterceptor())
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(new MyInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
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

            //TODO disable if published
            System.out.println("CALL API: " + request.url());

            return response;
        }
    }
}
