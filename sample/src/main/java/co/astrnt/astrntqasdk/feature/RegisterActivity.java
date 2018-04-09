package co.astrnt.astrntqasdk.feature;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import co.astrnt.astrntqasdk.R;
import co.astrnt.astrntqasdk.base.BaseActivity;
import co.astrnt.astrntqasdk.widget.CustomFieldEditText;
import co.astrnt.qasdk.dao.CustomFieldApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.repository.InterviewRepository;

public class RegisterActivity extends BaseActivity {

    private InterviewRepository mInterviewRepository;
    private LinearLayout lyCustomField;
    private InterviewApiDao interviewApiDao;
    private List<CustomFieldApiDao> customFieldList;
    private List<CustomFieldEditText> customFieldEditTextList;

    public static void start(Context context, InterviewApiDao interviewApiDao) {
        Intent intent = new Intent(context, RegisterActivity.class);
        intent.putExtra(InterviewApiDao.class.getName(), interviewApiDao);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mContext = this;
        interviewApiDao = getIntent().getParcelableExtra(InterviewApiDao.class.getName());
        mInterviewRepository = new InterviewRepository(getApi());

        lyCustomField = findViewById(R.id.ly_custom_field);

        if (interviewApiDao.getCustom_fields() != null) {
            generateCustomField();
        }
    }

    private void generateCustomField() {
        customFieldList = interviewApiDao.getCustom_fields().getFields();
        customFieldEditTextList = new ArrayList<>();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        for (CustomFieldApiDao f : customFieldList) {
            String value = f.getLabel();
            long id = f.getId();
            LinearLayout container = (LinearLayout) layoutInflater.inflate(R.layout.view_register_custom_field, null);
            CustomFieldEditText editText = container.findViewById(R.id.inp_custom_field);
            editText.setHint(value);
            editText.setForField(value);
            editText.setFieldApiDao(f);

            editText.setFieldId(id);
            lyCustomField.addView(container);
            customFieldEditTextList.add(editText);
        }
    }

}
