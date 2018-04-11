package co.astrnt.astrntqasdk.feature;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import co.astrnt.astrntqasdk.R;
import co.astrnt.astrntqasdk.base.BaseActivity;
import co.astrnt.qasdk.core.InterviewObserver;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.repository.InterviewRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class EnterCodeActivity extends BaseActivity {

    private InterviewRepository mInterviewRepository;
    private EditText inpCode;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        inpCode = findViewById(R.id.inp_code);
        btnSubmit = findViewById(R.id.btn_submit);

        mInterviewRepository = new InterviewRepository(getApi());

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = inpCode.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    inpCode.setError("Code still empty");
                    inpCode.setFocusable(true);
                    return;
                }
                enterCode(code);
            }
        });
    }

    private void enterCode(String code) {
        mInterviewRepository.enterCode(code, 98)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new InterviewObserver() {

                    @Override
                    public void onApiResultError(String message, String code) {
                        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNeedToRegister(InterviewApiDao interview) {
                        Toast.makeText(mContext, "Need Register", Toast.LENGTH_SHORT).show();
                        RegisterActivity.start(mContext, interview);
                    }

                    @Override
                    public void onInterviewType(InterviewApiDao interview) {
                        Toast.makeText(mContext, "Interview", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTestType(InterviewApiDao interview) {
                        Toast.makeText(mContext, "Test MCQ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSectionType(InterviewApiDao interview) {
                        Toast.makeText(mContext, "Section", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
