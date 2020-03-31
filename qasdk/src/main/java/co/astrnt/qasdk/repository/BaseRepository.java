package co.astrnt.qasdk.repository;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.orhanobut.hawk.Hawk;

import java.util.HashMap;
import java.util.List;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.utils.LogUtil;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class BaseRepository {

    protected final AstronautApi mAstronautApi;
    protected AstrntSDK astrntSDK = new AstrntSDK();

    public BaseRepository(AstronautApi astronautApi) {
        mAstronautApi = astronautApi;
    }

    public void sendLog() {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        final String interviewCode = interviewApiDao.getInterviewCode();
        HashMap<String, String> map = new HashMap<>();

        List<LogDao> logDaos = LogUtil.getLog(interviewCode);

        if (logDaos != null) {
            String token = interviewApiDao.getToken();

            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            String version = String.format("%s %s", manufacturer, model);
            String os = "Android " + Build.VERSION.RELEASE;
            String appVersion = Hawk.get("versionCode") + " / " + Hawk.get("versionName");

            //TODO: get imei is restricted since Android 10
            String imei = "-";
            String timeZone = LogUtil.getTimeZone();

            for (int i = 0; i < logDaos.size(); i++) {
                LogDao logDao = logDaos.get(i);

                map.put("logs[" + i + "][candidate_id]", String.valueOf(interviewApiDao.getCandidate().getId()));
                map.put("logs[" + i + "][company_id]", String.valueOf(interviewApiDao.getCompany().getId()));
                map.put("logs[" + i + "][interviewCode]", String.valueOf(interviewApiDao.getInterviewCode()));
                map.put("logs[" + i + "][job_id]", String.valueOf(interviewApiDao.getJob().getId()));

                map.put("logs[" + i + "][event]", logDao.getEvent());
                map.put("logs[" + i + "][log_time]", logDao.getLog_time());
                map.put("logs[" + i + "][message]", logDao.getMessage());

                map.put("logs[" + i + "][time_zone]", timeZone);
                map.put("logs[" + i + "][imei]", imei);
                map.put("logs[" + i + "][version]", version);
                map.put("logs[" + i + "][os]", os);
                map.put("logs[" + i + "][app_version]", appVersion);
            }

            if (!map.isEmpty()) {
                mAstronautApi.getApiService().sendLog(token, map)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(new MyObserver<BaseApiDao>() {

                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onApiResultCompleted() {
                            }

                            @Override
                            public void onApiResultError(String title, String message, String code) {
                                Timber.e(message);
                                createHandlerForReSendLog();
                            }

                            @Override
                            public void onApiResultOk(BaseApiDao apiDao) {
                                Timber.d(apiDao.getMessage());

                                LogUtil.clearLog(interviewCode);
                            }
                        });
            }
        }

    }

    private void createHandlerForReSendLog() {
        Thread thread = new Thread() {
            public void run() {
                Looper.prepare();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do Work
                        sendLog();
                        handler.removeCallbacks(this);
                        Looper.myLooper().quit();
                    }
                }, 2000);

                Looper.loop();
            }
        };
        thread.start();
    }
}
