package co.astrnt.astrntqasdk.feature;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.Nullable;
import co.astrnt.astrntqasdk.BuildConfig;
import co.astrnt.astrntqasdk.R;
import co.astrnt.astrntqasdk.base.BaseActivity;
import co.astrnt.astrntqasdk.widget.CustomFieldEditText;
import co.astrnt.qasdk.core.RegisterObserver;
import co.astrnt.qasdk.dao.CustomFieldApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.post.RegisterPost;
import co.astrnt.qasdk.repository.InterviewRepository;
import co.astrnt.qasdk.utils.StringUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RegisterActivity extends BaseActivity {

    private InterviewRepository mInterviewRepository;
    private TextView txtJobTitle, txtCompany;
    private EditText inpFullName, inpPreferredName, inpEmail, inpConfirmEmail, inpPhone;
    private LinearLayout lyCustomField;
    private Button btnSubmit;
    private List<CustomFieldApiDao> customFieldList = new ArrayList<>();
    private List<CustomFieldEditText> customFieldEditTextList = new ArrayList<>();
    private ProgressDialog progressDialog;

    public static void start(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        mInterviewRepository = new InterviewRepository(getApi());

        txtJobTitle = findViewById(R.id.txt_job_title);
        txtCompany = findViewById(R.id.txt_company_name);
        lyCustomField = findViewById(R.id.ly_custom_field);
        inpFullName = findViewById(R.id.inp_full_name);
        inpPreferredName = findViewById(R.id.inp_preferred_name);
        inpEmail = findViewById(R.id.inp_email);
        inpConfirmEmail = findViewById(R.id.inp_confirm_email);
        inpPhone = findViewById(R.id.inp_phone);
        btnSubmit = findViewById(R.id.btn_submit);

        if (BuildConfig.DEBUG) {
            Random w = new Random();
            int q = w.nextInt(30000);
            inpFullName.setText(q + "");
            inpPreferredName.setText(q + "");
            inpEmail.setText("test" + q + "@mailinator.com");
            inpConfirmEmail.setText("test" + q + "@mailinator.com");
            inpPhone.setText(q + "");
        }

        showInfo();
        generateCustomField();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInput();
            }
        });
    }

    private void showInfo() {
        txtJobTitle.setText(interviewApiDao.getJob().getTitle());
        txtCompany.setText(interviewApiDao.getCompany().getTitle());
    }

    private void validateInput() {

        String fullName = inpFullName.getText().toString();
        String preferredName = inpPreferredName.getText().toString();
        String email = inpEmail.getText().toString();
        String confirmEmail = inpConfirmEmail.getText().toString();
        String phone = inpPhone.getText().toString();

        RegisterPost registerPost = new RegisterPost();

        if (TextUtils.isEmpty(fullName)) {
            inpFullName.setError("Full Name is required");
            inpFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(preferredName)) {
            inpPreferredName.setError("Preferred Name is required");
            inpPreferredName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            inpEmail.setError("Email is required");
            inpEmail.requestFocus();
            return;
        }
        if (!StringUtils.isValidEmailAddress(email)) {
            inpEmail.setError("Email wrong format");
            inpEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmEmail)) {
            inpConfirmEmail.setError("Confirmation Email is required");
            inpConfirmEmail.requestFocus();
            return;
        }
        if (!StringUtils.isValidEmailAddress(confirmEmail)) {
            inpEmail.setError("Confirmation Email wrong format");
            inpEmail.requestFocus();
            return;
        }
        if (!email.equals(confirmEmail)) {
            inpConfirmEmail.setError("The email addresses you entered don\'t match");
            inpConfirmEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            inpPhone.setError("Phone Number is required");
            inpPhone.requestFocus();
            return;
        }

        List<RegisterPost.CustomFieldsPost> customFieldsPosts = new ArrayList<>();
        for (CustomFieldEditText editText : customFieldEditTextList) {
            String input = editText.getText().toString();

            CustomFieldApiDao customFieldField = editText.getFieldApiDao();
            if (customFieldField.isMandatory() && input.isEmpty()) {
                editText.requestFocus();
                editText.setError(String.format(context.getString(R.string.field_is_required), customFieldField.getLabel()));
                return;
            }

            RegisterPost.CustomFieldsPost item = new RegisterPost.CustomFieldsPost();
            item.setId(editText.getFieldApiDao().getId());
            item.setValue(editText.getText().toString());
            customFieldsPosts.add(item);
        }

        registerPost.setCompany_id(interviewApiDao.getCompany().getId());
        registerPost.setJob_id(interviewApiDao.getJob().getId());
        registerPost.setInterviewCode(interviewApiDao.getTemp_code());
        registerPost.setFullname(fullName);
        registerPost.setPreferred_name(preferredName);
        registerPost.setEmail(email);
        registerPost.setPhone(phone);
        registerPost.setVersion(BuildConfig.SDK_VERSION);

        if (!customFieldList.isEmpty()) {
            registerPost.setCustom_fields(customFieldsPosts);
        }

        registerUser(registerPost);
    }

    private void registerUser(RegisterPost param) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mInterviewRepository.registerUser(param)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RegisterObserver() {

                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onApiResultCompleted() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onApiResultError(String title, String message, String code) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onInterviewType(InterviewApiDao interview) {
                        Toast.makeText(context, "Video Interview", Toast.LENGTH_SHORT).show();
                        VideoInfoActivity.start(context);
                        finish();
                    }

                    @Override
                    public void onTestType(InterviewApiDao interview) {
                        Toast.makeText(context, "Test MCQ can't use for now", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSectionType(InterviewApiDao interview) {
                        Toast.makeText(context, "Section can't use for now", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAstronautProfileType(InterviewApiDao interview) {

                    }
                });
    }

    private void generateCustomField() {
        if (interviewApiDao.getCustom_fields() == null) {
            return;
        }

        inpPhone.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        customFieldList = interviewApiDao.getCustom_fields().getFields();
        customFieldEditTextList = new ArrayList<>();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        for (int i = 0; i < customFieldList.size(); i++) {
            CustomFieldApiDao f = customFieldList.get(i);

            String value = f.getLabel();
            long id = f.getId();
            LinearLayout container = (LinearLayout) layoutInflater.inflate(R.layout.view_register_custom_field, null);
            CustomFieldEditText editText = container.findViewById(R.id.inp_custom_field);
            editText.setHint(value);
            editText.setForField(value);
            editText.setFieldApiDao(f);

            editText.setFieldId(id);
            if (i == (customFieldList.size() - 1)) {
                editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }
            lyCustomField.addView(container);
            customFieldEditTextList.add(editText);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_job_desc:
                JobDescriptionActivity.start(context);
                return true;
            case R.id.action_exit:
                EnterCodeActivity.start(context);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
