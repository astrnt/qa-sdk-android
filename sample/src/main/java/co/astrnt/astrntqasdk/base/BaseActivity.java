package co.astrnt.astrntqasdk.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import co.astrnt.astrntqasdk.AstronautApp;
import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.InterviewApiDao;

public class BaseActivity extends AppCompatActivity {
    protected Context context = this;
    protected AstrntSDK astrntSDK;
    protected InterviewApiDao interviewApiDao;

    public static AstronautApi getApi() {
        return AstronautApp.getApi();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        astrntSDK = new AstrntSDK();
        interviewApiDao = astrntSDK.getCurrentInterview();
    }
}
