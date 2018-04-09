package co.astrnt.astrntqasdk.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import co.astrnt.qasdk.dao.CustomFieldApiDao;

public class CustomFieldEditText extends AppCompatEditText {
    String forField;
    long fieldId;
    CustomFieldApiDao fieldApiDao;

    public CustomFieldEditText(Context context) {
        super(context);
    }

    public CustomFieldEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFieldEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomFieldApiDao getFieldApiDao() {
        return fieldApiDao;
    }

    public void setFieldApiDao(CustomFieldApiDao fieldApiDao) {
        this.fieldApiDao = fieldApiDao;
    }

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public String getForField() {
        return forField;
    }

    public void setForField(String forField) {
        this.forField = forField;
    }
}
