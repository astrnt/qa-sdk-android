package co.astrnt.astrntqasdk;

import android.app.Application;

import co.astrnt.qasdk.core.AstronautApi;

public class AstronautApp extends Application {

    private static AstronautApi mAstronautApi;

    public static AstronautApi getApi() {
        if (mAstronautApi == null) {
            mAstronautApi = new AstronautApi(BuildConfig.API_URL);
        }
        return mAstronautApi;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
