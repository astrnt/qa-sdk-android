package co.astrnt.qasdk.type;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.StringDef;

import static co.astrnt.qasdk.type.ElapsedTimeType.PREPARATION;
import static co.astrnt.qasdk.type.ElapsedTimeType.SECTION;
import static co.astrnt.qasdk.type.ElapsedTimeType.TEST;

@StringDef({SECTION, TEST, PREPARATION})
@Retention(RetentionPolicy.SOURCE)
public @interface ElapsedTime {
}