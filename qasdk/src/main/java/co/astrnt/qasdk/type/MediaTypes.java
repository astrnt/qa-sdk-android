package co.astrnt.qasdk.type;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.StringDef;

import static co.astrnt.qasdk.type.MediaType.AUDIO;
import static co.astrnt.qasdk.type.MediaType.VIDEO;


@StringDef({VIDEO, AUDIO})
@Retention(RetentionPolicy.SOURCE)
public @interface MediaTypes {
}