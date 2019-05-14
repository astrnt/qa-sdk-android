package co.astrnt.astrntqasdk.feature;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

import co.astrnt.astrntqasdk.R;
import co.astrnt.astrntqasdk.base.BaseActivity;
import co.astrnt.astrntqasdk.listener.PreviewListener;
import co.astrnt.astrntqasdk.utils.ServiceUtils;
import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.repository.QuestionRepository;
import co.astrnt.qasdk.videocompressor.services.VideoCompressService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class VideoPreviewActivity extends BaseActivity implements PreviewListener, View.OnClickListener {

    private static final String EXT_VIDEO_URI = "VideoPreviewActivity.VideoUri";

    private QuestionRepository mQuestionRepository;

    private TextView txtTitle;
    private TextView txtQuestion;
    private VideoView videoView;
    private TextView txtAttemptInfo;
    private TextView txtTimer;
    private Button btnRetake;
    private Button btnNext;

    private ProgressDialog progressDialog;

    private QuestionApiDao currentQuestion;
    private CountDownTimer countDownTimer;
    private Uri videoUri;
    private long videoDuration;
    private int questionAttempt;

    public static void start(Context context, Uri uri) {
        Intent intent = new Intent(context, VideoPreviewActivity.class);
        intent.putExtra(EXT_VIDEO_URI, uri);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_preview);

        txtTitle = findViewById(R.id.txt_title);
        txtQuestion = findViewById(R.id.txt_question);
        videoView = findViewById(R.id.video_view);
        txtTimer = findViewById(R.id.txt_timer);
        txtAttemptInfo = findViewById(R.id.txt_attempt_info);
        btnRetake = findViewById(R.id.btn_retake);
        btnNext = findViewById(R.id.btn_next);

        mQuestionRepository = new QuestionRepository(getApi());

        currentQuestion = videoSDK.getCurrentQuestion();

        videoUri = getIntent().getParcelableExtra(EXT_VIDEO_URI);
        videoView.setVideoURI(videoUri);

        btnRetake.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        showInfo();
        prepareVideoPlayer();
    }

    private void showInfo() {

        questionAttempt = videoSDK.getQuestionAttempt();
        currentQuestion = videoSDK.getCurrentQuestion();

        txtQuestion.setText(currentQuestion.getTitle());
        txtTitle.setText("Preview");

        if (videoSDK.isLastAttempt()) {
            btnRetake.setEnabled(false);
            txtAttemptInfo.setVisibility(View.GONE);
            finishQuestion(currentQuestion);
        } else {
            String attemptInfo = context.getResources().getQuantityString(R.plurals.you_have_more_attempt, questionAttempt, questionAttempt);
            txtAttemptInfo.setText(attemptInfo);
            btnRetake.setEnabled(true);
            txtAttemptInfo.setVisibility(View.VISIBLE);
        }

    }

    private void prepareVideoPlayer() {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                videoDuration = mp.getDuration();

                ViewGroup.LayoutParams lp = videoView.getLayoutParams();
                float videoWidth = mp.getVideoWidth();
                float videoHeight = mp.getVideoHeight();
                float viewWidth = videoView.getWidth();
                lp.height = (int) (viewWidth * (videoHeight / videoWidth));
                videoView.setLayoutParams(lp);
                onVideoPlay();
            }
        });
    }

    private void playVideo() {
        if (videoView.isPlaying()) {
            countDownTimer.cancel();
            return;
        }
        //TODO : video view check resume state
        videoView.start();
    }

    @Override
    public void onVideoFinished() {
        countDownTimer.cancel();
    }

    @Override
    public void onVideoPlay() {
        playVideo();
        countDownTimer = new CountDownTimer(videoDuration, 1000) {

            public void onTick(long millisUntilFinished) {
                txtTimer.setVisibility(View.VISIBLE);
                long currentProgress = millisUntilFinished / 1000;
                txtTimer.setText(String.valueOf(currentProgress + 1));
            }

            public void onFinish() {
                txtTimer.setVisibility(View.GONE);
                onVideoFinished();
            }
        }.start();
    }

    @Override
    public void onVideoPause() {
        videoView.pause();
        countDownTimer.cancel();
    }

    @Override
    public void onVideoRetake() {
        File file = new File(videoUri.getPath());
        file.delete();
        VideoRecordActivity.start(context);
        finish();
    }

    @Override
    public void onVideoDone() {
        if (videoSDK.isNotLastQuestion()) {
            videoSDK.increaseQuestionIndex();
        }
        showNextQuestion();
        compressVideo();
    }

    public void finishQuestion(QuestionApiDao currentQuestion) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mQuestionRepository.finishQuestion(currentQuestion)
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
                    public void onApiResultError(String title, String message, String code) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onApiResultOk(BaseApiDao baseApiDao) {
                        Toast.makeText(context, baseApiDao.getMessage(), Toast.LENGTH_SHORT).show();
                        onVideoDone();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_retake:
                onVideoRetake();
                break;
            case R.id.btn_next:
                finishQuestion(currentQuestion);
                break;
        }
    }

    private void compressVideo() {
        File file = new File(videoUri.getPath());
        videoSDK.markAsPending(currentQuestion, videoUri.getPath());
        if (!ServiceUtils.isMyServiceRunning(context, VideoCompressService.class)) {
            VideoCompressService.start(context, file.getAbsolutePath(), currentQuestion.getId());
        } else {
            showNextQuestion();
        }
    }

    private void showNextQuestion() {
        if (videoSDK.isNotLastQuestion()) {
            VideoInstructionActivity.start(context);
        } else {
//            TODO: video upload
            Toast.makeText(context, "Interview Already Finished", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
