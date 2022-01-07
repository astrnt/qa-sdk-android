package co.astrnt.qasdk.type

import androidx.annotation.StringDef

@StringDef(CustomFiledType.CHECK_BOX, CustomFiledType.TEXT_FIELD, CustomFiledType.TEXT_AREA)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class CustomField 