package co.astrnt.astrntqasdk.base;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import co.astrnt.astrntqasdk.AstronautApp;
import co.astrnt.qasdk.VideoSDK;
import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.dao.InterviewApiDao;

public class BaseActivity extends AppCompatActivity {
    protected Context context = this;
    protected VideoSDK videoSDK;
    protected InterviewApiDao interviewApiDao;

    public static AstronautApi getApi() {
        return AstronautApp.getApi();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoSDK = new VideoSDK();
        interviewApiDao = videoSDK.getCurrentInterview();
    }
}
