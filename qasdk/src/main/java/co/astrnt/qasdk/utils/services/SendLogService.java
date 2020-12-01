package co.astrnt.qasdk.utils.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.R;
import co.astrnt.qasdk.constants.PreferenceKey;
import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.utils.LogUtil;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SendLogService extends Service {

    public static final long NOTIFY_INTERVAL = 60 * 1000;

    private Context context;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Timer mTimer = null;
    private AstrntSDK astrntSDK;
    private AstronautApi astronautApi;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int mNotificationId;

    public static void start(Context context) {
        Intent intent = new Intent(context, SendLogService.class);
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        astrntSDK = new AstrntSDK();
        astronautApi = astrntSDK.getApi();

        createNotification();

        if (mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
        }
        mTimer.scheduleAtFixedRate(new SendLogService.TimeDisplayTimerTask(), 5000, NOTIFY_INTERVAL);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void doSendLog() {
        String interviewCode = astrntSDK.getInterviewCode();
        String token = Hawk.get(PreferenceKey.KEY_TOKEN);

        HashMap<String, String> map = new HashMap<>();

        List<LogDao> logDaoList = LogUtil.getLog(interviewCode);
        List<LogDao> sentLog = new ArrayList<>();
        if (logDaoList.isEmpty()) {
            stopService();
        } else {
            String companyId = Hawk.get(PreferenceKey.KEY_COMPANY_ID);
            String jobId = Hawk.get(PreferenceKey.KEY_JOB_ID);
            String candidateId = Hawk.get(PreferenceKey.KEY_CANDIDATE_ID);

            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            String version = String.format("%s %s", manufacturer, model);
            String os = "Android " + Build.VERSION.RELEASE;
            String appVersion = Hawk.get("versionCode") + " / " + Hawk.get("versionName");

            //TODO: get imei is restricted since Android 10
            String imei = "-";
            String timeZone = LogUtil.getTimeZone();

            int lastLogIndex = LogUtil.getLastLogIndex();

            for (int i = 0; i < logDaoList.size(); i++) {
                LogDao logDao = logDaoList.get(i);
                sentLog.add(logDao);

                map.put("logs[" + i + "][candidate_id]", candidateId);
                map.put("logs[" + i + "][company_id]", companyId);
                map.put("logs[" + i + "][interviewCode]", interviewCode);
                map.put("logs[" + i + "][job_id]", jobId);

                map.put("logs[" + i + "][event]", logDao.getEvent());
                map.put("logs[" + i + "][log_time]", logDao.getLog_time());
                map.put("logs[" + i + "][message]", logDao.getMessage());

                map.put("logs[" + i + "][time_zone]", timeZone);
                map.put("logs[" + i + "][imei]", imei);
                map.put("logs[" + i + "][version]", version);
                map.put("logs[" + i + "][os]", os);
                map.put("logs[" + i + "][app_version]", appVersion);

                int index = lastLogIndex + i;
                LogUtil.saveLastLogIndex(index);
            }
        }

        astrntSDK.saveLastApiCall("(/candidate/logs)");

        astronautApi.getApiService().sendLog(token, map)
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

                        String errorMessage = "";

                        if (message != null) {
                            errorMessage = "message : " + message;
                        }
                        if (code != null) {
                            errorMessage = errorMessage + " httpCode : " + code;
                        }

                        LogUtil.addNewLog(interviewCode,
                                new LogDao("Hit API",
                                        "Error to Send Log " + errorMessage
                                )
                        );

                        mNotifyManager.notify(mNotificationId, mBuilder.build());
                        mNotifyManager.cancel(mNotificationId);

                        if (astrntSDK.isShowUpload()) {
                            createHandlerForReSendLog();
                        } else {
                            stopService();
                        }
                    }

                    @Override
                    public void onApiResultOk(BaseApiDao apiDao) {
                        Timber.d(apiDao.getMessage());

                        mNotifyManager.notify(mNotificationId, mBuilder.build());
                        mNotifyManager.cancel(mNotificationId);
                        LogUtil.clearSentLog(interviewCode, sentLog);
                        stopService();
                    }
                });
    }

    private void createNotification() {
        mNotificationId = 1001;

        final String channelId = "Astronaut Q&A";
        mBuilder = new NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_send_white_24dp)
                .setContentTitle("Astronaut Q&A")
                .setContentText("Sending Log")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Send Log";
            String description = "Astronaut Q&A Send Log";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);

            mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (mNotifyManager != null) {
                mNotifyManager.createNotificationChannel(channel);
            }

            startForeground(mNotificationId, mBuilder.build());
        } else {
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        mNotifyManager.notify(mNotificationId, mBuilder.build());
    }

    public void stopService() {
        mTimer.cancel();
        if (mNotifyManager != null) mNotifyManager.cancelAll();
        stopSelf();
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(SendLogService.this::doSendLog);
        }
    }

    private void createHandlerForReSendLog() {
        Thread thread = new Thread() {
            public void run() {
                Looper.prepare();

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doSendLog();
                        handler.removeCallbacks(this);
                    }
                }, 5000);

                Looper.loop();
            }
        };
        thread.start();
    }
}