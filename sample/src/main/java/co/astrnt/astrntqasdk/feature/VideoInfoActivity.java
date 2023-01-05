package co.astrnt.astrntqasdk.feature;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import co.astrnt.astrntqasdk.R;
import co.astrnt.astrntqasdk.base.BaseActivity;
import co.astrnt.qasdk.dao.JobApiDao;

public class VideoInfoActivity extends BaseActivity {

    private TextView txtCompanyName;
    private TextView txtJobTitle;
    private TextView txtTotalQuestion;
    private TextView txtTitleInfo;
    private TextView txtTotalMinutes;
    private TextView txtTotalUpload;
    private Button btnStart;

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoInfoActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_info);

        txtCompanyName = findViewById(R.id.txt_company_name);
        txtJobTitle = findViewById(R.id.txt_job_title);
        txtTotalQuestion = findViewById(R.id.txt_total_question);
        txtTitleInfo = findViewById(R.id.txt_title_info);
        txtTotalMinutes = findViewById(R.id.txt_total_minutes);
        txtTotalUpload = findViewById(R.id.txt_total_upload);
        btnStart = findViewById(R.id.btn_start);

        showInfo();

        btnStart.setOnClickListener(v -> moveToNext());
    }

    private void showInfo() {
        JobApiDao job = interviewApiDao.getJob();
        txtCompanyName.setText(interviewApiDao.getCompany().getTitle());
        txtJobTitle.setText(job.getTitle());

        txtTotalQuestion.setText(String.valueOf(interviewApiDao.getQuestions().size()));
        txtTitleInfo.setText(context.getResources().getQuantityString(R.plurals.video_question,
                interviewApiDao.getTotalQuestion()));
        txtTotalMinutes.setText(String.valueOf(interviewApiDao.getEstimation_time()));
        txtTotalUpload.setText(String.valueOf(interviewApiDao.getTotalUpload()));
    }

    private void moveToNext() {
        VideoInstructionActivity.start(context);
        finish();
    }
}
