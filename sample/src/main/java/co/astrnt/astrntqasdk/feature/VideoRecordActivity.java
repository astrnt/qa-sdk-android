package co.astrnt.astrntqasdk.feature;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;

import java.io.File;

import co.astrnt.astrntqasdk.R;
import co.astrnt.astrntqasdk.base.BaseActivity;
import co.astrnt.astrntqasdk.listener.RecordListener;
import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.repository.QuestionRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class VideoRecordActivity extends BaseActivity implements RecordListener {

    private static final String STATE_PRE_RECORD = "pre_record";
    private static final String STATE_ON_COUNTDOWN = "on_countdown";
    private static final String STATE_ON_RECORD = "on_record";
    private static final String STATE_ON_FINISH = "on_finish";
    private static String CURRENT_STATE = STATE_PRE_RECORD;

    private QuestionRepository mQuestionRepository;

    private TextView txtTitle;
    private TextView txtQuestion;
    private TextView txtCountDown;
    private TextView txtTimer;
    private Button btnControl;
    private CameraView cameraView;

    private ProgressDialog progressDialog;

    private QuestionApiDao currentQuestion;
    private CountDownTimer countDownTimer;
    private File recordFile;

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoRecordActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_record);

        txtTitle = findViewById(R.id.txt_title);
        txtQuestion = findViewById(R.id.txt_question);
        txtCountDown = findViewById(R.id.txt_count_down);
        txtTimer = findViewById(R.id.txt_timer);
        cameraView = findViewById(R.id.camera_view);
        btnControl = findViewById(R.id.btn_control);

        mQuestionRepository = new QuestionRepository(getApi());

        currentQuestion = videoSDK.getCurrentQuestion();

        btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToNextState();
            }
        });

        setUpCamera();
    }

    private void setUpCamera() {
        cameraView.addCameraListener(new CameraListener() {

            @Override
            public void onVideoTaken(File video) {
                recordFile = video;
                moveToPreview();
            }
        });
    }

    private void startRecording() {
        File directory = new File(context.getFilesDir(), "video");
        if (!directory.exists()) {
            directory.mkdir();
        }
        recordFile = new File(directory, currentQuestion.getId() + "_video.mp4");
        cameraView.setVideoMaxDuration(currentQuestion.getMaxTime() * 1000);
        cameraView.startCapturingVideo(recordFile);
    }

    private void decreaseAttempt() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mQuestionRepository.addQuestionAttempt(currentQuestion)
                .compose(SchedulerUtils.ioToMain())
                .doOnError(throwable -> {
                    getView().showProgress(false);
                    getView().showError(throwable);
                })
                .subscribe(new MyObserver<BaseApiDao>() {

                    @Override
                    public void onApiResultCompleted() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onApiResultError(String message, String code) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onApiResultOk(BaseApiDao baseApiDao) {
                        Toast.makeText(context, baseApiDao.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private String getCurrentState() {
        return CURRENT_STATE;
    }

    public void setCurrentState(String currentState) {
        CURRENT_STATE = currentState;
        switch (getCurrentState()) {
            case STATE_PRE_RECORD:
                btnControl.setText("Start");
                onPreRecord();
                break;
            case STATE_ON_COUNTDOWN:
                btnControl.setText("Stop");
                btnControl.setClickable(false);
                btnControl.setEnabled(false);
                btnControl.setVisibility(View.GONE);
                onCountdown();
                break;
            case STATE_ON_RECORD:
                btnControl.setText("Stop");
                btnControl.setVisibility(View.VISIBLE);
                onRecording();
                break;
            case STATE_ON_FINISH:
                txtCountDown.setVisibility(View.GONE);
                txtTimer.setVisibility(View.GONE);
                onFinished();
                break;
            default:
                break;
        }
    }

    private void setToNextState() {
        switch (getCurrentState()) {
            case STATE_PRE_RECORD:
                setCurrentState(STATE_ON_COUNTDOWN);
                break;
            case STATE_ON_COUNTDOWN:
                setCurrentState(STATE_ON_RECORD);
                break;
            case STATE_ON_RECORD:
                setCurrentState(STATE_ON_FINISH);
                break;
            case STATE_ON_FINISH:
                setCurrentState(STATE_PRE_RECORD);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPreRecord() {
        txtTitle.setText("Instruction");
        txtQuestion.setText("Record Instruction");
    }

    @Override
    public void onCountdown() {
        txtTitle.setText("Question");
        decreaseAttempt();
        txtQuestion.setText(currentQuestion.getTitle());
        videoSDK.decreaseQuestionAttempt();
        if (videoSDK.isLastAttempt()) {
            videoSDK.markNotAnswer(currentQuestion);
        }
        txtCountDown.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(10 * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                long currentProgress = millisUntilFinished / 1000;
                txtCountDown.setText(String.valueOf(currentProgress + 1));
            }

            public void onFinish() {
                txtCountDown.setText(String.valueOf(1));
                txtCountDown.setVisibility(View.GONE);
                setCurrentState(STATE_ON_RECORD);
            }
        }.start();
    }

    @Override
    public void onRecording() {
        txtTitle.setText("Recording");
        startRecording();

        txtTimer.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                countDownTimer = new CountDownTimer(currentQuestion.getMaxTime() * 1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        long currentProgress = millisUntilFinished / 1000;
                        txtTimer.setText(String.valueOf(currentProgress));
                        if (currentProgress < 40) {
                            btnControl.setClickable(true);
                            btnControl.setEnabled(true);
                        }
                        if (currentProgress < 5) {
                            if (txtCountDown.getVisibility() == View.GONE) {
                                txtCountDown.setVisibility(View.VISIBLE);
                            }
                            txtCountDown.setText(String.valueOf(currentProgress + 1));
                        }
                    }

                    public void onFinish() {
                        txtTimer.setVisibility(View.GONE);
                        setCurrentState(STATE_ON_FINISH);
                    }
                }.start();
            }
        }, 2000);
    }

    @Override
    public void onFinished() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        cameraView.stopCapturingVideo();
        txtCountDown.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void moveToPreview() {
        if (recordFile != null) {
            VideoPreviewActivity.start(context, Uri.fromFile(recordFile));
            finish();
        }
    }
}
