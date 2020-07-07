package co.astrnt.qasdk.type;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.StringDef;

import static co.astrnt.qasdk.type.CustomFiledType.CHECK_BOX;
import static co.astrnt.qasdk.type.CustomFiledType.TEXT_AREA;
import static co.astrnt.qasdk.type.CustomFiledType.TEXT_FIELD;

@StringDef({CHECK_BOX, TEXT_FIELD, TEXT_AREA})
@Retention(RetentionPolicy.SOURCE)
public @interface CustomField {
}