package co.astrnt.astrntqasdk;

import android.app.Application;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.core.AstronautApi;

public class AstronautApp extends Application {

    private static AstrntSDK astrntSDK;

    public static AstronautApi getApi() {
        return astrntSDK.getApi();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUpSDK();
    }

    private void setUpSDK() {
        if (astrntSDK == null) {
            astrntSDK = new AstrntSDK(this, BuildConfig.API_URL, BuildConfig.DEBUG);
        }
    }
}
