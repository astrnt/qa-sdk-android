package co.astrnt.qasdk.core;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class ApiInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request requestWithUserAgent = originalRequest.newBuilder()
//                .header("User-Agent", Constant.USER_AGENT)
                .build();
//        System.out.println("API START " + chain.request().url().toString());
        return chain.proceed(requestWithUserAgent);
    }
}