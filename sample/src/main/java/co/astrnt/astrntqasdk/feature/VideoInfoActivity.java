package co.astrnt.astrntqasdk.feature;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import co.astrnt.astrntqasdk.R;
import co.astrnt.astrntqasdk.base.BaseActivity;
import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.InterviewStartApiDao;
import co.astrnt.qasdk.dao.JobApiDao;
import co.astrnt.qasdk.repository.InterviewRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class VideoInfoActivity extends BaseActivity {

    private InterviewRepository mInterviewRepository;
    private TextView txtCompanyName;
    private TextView txtJobTitle;
    private TextView txtTotalQuestion;
    private TextView txtTitleInfo;
    private TextView txtTotalMinutes;
    private TextView txtTotalUpload;
    private Button btnStart;

    private ProgressDialog progressDialog;

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoInfoActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_info);

        mInterviewRepository = new InterviewRepository(getApi());

        txtCompanyName = findViewById(R.id.txt_company_name);
        txtJobTitle = findViewById(R.id.txt_job_title);
        txtTotalQuestion = findViewById(R.id.txt_total_question);
        txtTitleInfo = findViewById(R.id.txt_title_info);
        txtTotalMinutes = findViewById(R.id.txt_total_minutes);
        txtTotalUpload = findViewById(R.id.txt_total_upload);
        btnStart = findViewById(R.id.btn_start);

        showInfo();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInterview();
            }
        });
    }

    private void showInfo() {
        JobApiDao job = interviewApiDao.getJob();
        txtCompanyName.setText(interviewApiDao.getCompany().getTitle());
        txtJobTitle.setText(job.getTitle());

        txtTotalQuestion.setText(String.valueOf(interviewApiDao.getQuestions().size()));
        txtTitleInfo.setText(context.getResources().getQuantityString(R.plurals.video_question,
                interviewApiDao.getTotalQuestion()));
        txtTotalMinutes.setText(String.valueOf(interviewApiDao.getEstimatedTime()));
        txtTotalUpload.setText(String.valueOf(interviewApiDao.getTotalUpload()));
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
//                            TODO: finish view or show dialog that interview already finished
                            Toast.makeText(context, apiDao.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            moveToNext();
                            Toast.makeText(context, apiDao.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void moveToNext() {
        //TODO: show video instruction
//        VideoInstructionActivity.start(context);
    }
}
