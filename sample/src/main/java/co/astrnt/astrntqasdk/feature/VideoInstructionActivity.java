package co.astrnt.astrntqasdk.feature;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import co.astrnt.astrntqasdk.R;
import co.astrnt.astrntqasdk.base.BaseActivity;
import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.repository.InterviewRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class VideoInstructionActivity extends BaseActivity {

    private InterviewRepository mInterviewRepository;
    private TextView txtIndexOfSize;
    private TextView txtTotalAttempt;
    private TextView txtContinueInfo;
    private TextView txtAttemptInfo;
    private AppCompatButton btnContinue;
    private AppCompatCheckBox cbxUnderstand;

    private ProgressDialog progressDialog;

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoInstructionActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_instruction);

        mInterviewRepository = new InterviewRepository(getApi());

        txtIndexOfSize = findViewById(R.id.txt_index_of_size);
        txtTotalAttempt = findViewById(R.id.txt_total_attempt);
        txtContinueInfo = findViewById(R.id.txt_continue_info);
        txtAttemptInfo = findViewById(R.id.txt_attempt_info);
        btnContinue = findViewById(R.id.btn_continue);
        cbxUnderstand = findViewById(R.id.cbx_understand);

        showInfo();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNext();
            }
        });

        cbxUnderstand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnContinue.setVisibility(View.VISIBLE);
                } else {
                    btnContinue.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showInfo() {

        int questionAttempt = astrntSDK.getQuestionAttempt();
        if (questionAttempt == 0) {
            if (astrntSDK.isNotLastQuestion()) {
                astrntSDK.increaseQuestionIndex();
                questionAttempt = astrntSDK.getQuestionAttempt();
            } else {
                moveToNext();
                return;
            }
        } else {
            if (!astrntSDK.isNotLastQuestion()) {
                moveToNext();
                return;
            }
        }

        QuestionApiDao currentQuestion = astrntSDK.getCurrentQuestion();
        int questionIndex = astrntSDK.getQuestionIndex();

        if (!astrntSDK.isPractice() && questionIndex == 0) {
            startInterview();
        }

        txtIndexOfSize.setText(getString(R.string.index_question_of, questionIndex + 1, astrntSDK.getTotalQuestion()));
        txtTotalAttempt.setText(String.valueOf(questionAttempt));
        txtAttemptInfo.setText(context.getResources().getQuantityString(R.plurals.attempt, questionAttempt));
        if (questionAttempt == currentQuestion.getTakesCount()) {
            txtContinueInfo.setVisibility(View.GONE);
        } else {
            int attemptTake = currentQuestion.getTakesCount() - questionAttempt;
            txtContinueInfo.setVisibility(View.VISIBLE);
            String continueInfo = context.getResources().getQuantityString(R.plurals.instruction_continue_info,
                    attemptTake, attemptTake);
            txtContinueInfo.setText(continueInfo);
        }

    }

    private void startInterview() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mInterviewRepository.startInterview()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<InterviewStartApiDao>() {
                    @Override
                    public void onApiResultCompleted() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onApiResultError(String message, String code) {
                    }

                    @Override
                    public void onApiResultOk(InterviewStartApiDao apiDao) {
                        if (apiDao.isFinished()) {
                            Toast.makeText(context, apiDao.getMessage(), Toast.LENGTH_LONG).show();
                            EnterCodeActivity.start(context);
                            finish();
                        } else {
                            Toast.makeText(context, apiDao.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void moveToNext() {
        VideoRecordActivity.start(context);
        finish();
    }
}
