package co.astrnt.qasdk.videocompressor.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.videocompressor.VideoCompress;
import io.reactivex.annotations.Nullable;
import timber.log.Timber;

public class CompressService extends Service {

    public static final String EXT_PATH = "CompressService.Path";

    public static final long NOTIFY_INTERVAL = 60 * 1000;

    private File inputFile, outputFile;
    private String inputPath, outputPath;

    private Context context;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    public static void start(Context context, String inputPath) {
        context.startService(
                new Intent(context, CompressService.class)
                        .putExtra(EXT_PATH, inputPath)
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            inputPath = intent.getStringExtra(EXT_PATH);

            inputFile = new File(inputPath);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        outputFile = new File(context.getFilesDir(), AstrntSDK.getCurrentQuestion().getId() + "_video.mp4");

        outputPath = outputFile.getAbsolutePath();

        if (mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
        }
        mTimer.scheduleAtFixedRate(new CompressService.TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void doCompress() {
        VideoCompress.compressVideoLow(inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                Timber.d("Video Compress compress START %s %s", inputPath, outputPath);
            }

            @Override
            public void onSuccess() {
                Timber.d("Video Compress compress %s %s %s", inputPath, outputPath, "SUCCESS");
                //TODO : start upload service
                stopService();
            }

            @Override
            public void onFail() {
                Timber.e("Video Compress compress %s %s %s", inputPath, outputPath, "FAILED");
                stopService();
            }

            @Override
            public void onProgress(float percent) {
                Timber.e("Video Compress progress %s", percent);
            }
        });
    }

    public void stopService() {
        mTimer.cancel();
        stopSelf();
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            // display toast
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    doCompress();
                }
            });
        }
    }
}