package co.astrnt.astrntqasdk.base;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import co.astrnt.astrntqasdk.AstronautApp;
import co.astrnt.qasdk.core.AstronautApi;

public class BaseActivity extends AppCompatActivity {
    protected Context mContext = this;

    public static AstronautApi getApi() {
        return AstronautApp.getApi();
    }
}
