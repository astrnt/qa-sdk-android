package co.astrnt.astrntqasdk.feature;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;
import co.astrnt.astrntqasdk.R;
import co.astrnt.astrntqasdk.base.BaseActivity;
import co.astrnt.qasdk.dao.JobApiDao;

public class JobDescriptionActivity extends BaseActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, JobDescriptionActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_job_description);

        TextView txtCompanyName = findViewById(R.id.txt_company_name);
        TextView txtJobTitle = findViewById(R.id.txt_job_title);
        TextView txtDescription = findViewById(R.id.txt_description);
        TextView txtResponsibility = findViewById(R.id.txt_responsibility);
        TextView txtRequirement = findViewById(R.id.txt_requirement);

        JobApiDao jobApiDao = interviewApiDao.getJob();

        txtCompanyName.setText(interviewApiDao.getCompany().getTitle());
        txtJobTitle.setText(jobApiDao.getTitle());

        if (TextUtils.isEmpty(jobApiDao.getDescription())) {
            txtDescription.setText(R.string.not_available);
        } else {
            txtDescription.setText(jobApiDao.getDescription());
        }
        if (TextUtils.isEmpty(jobApiDao.getResponsibility())) {
            txtResponsibility.setText(R.string.not_available);
        } else {
            txtResponsibility.setText(jobApiDao.getResponsibility());
        }

        if (TextUtils.isEmpty(jobApiDao.getRequirement())) {
            txtRequirement.setText(R.string.not_available);
        } else {
            txtRequirement.setText(jobApiDao.getRequirement());
        }

    }


}
