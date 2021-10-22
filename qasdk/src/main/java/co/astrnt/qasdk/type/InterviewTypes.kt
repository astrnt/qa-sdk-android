package co.astrnt.qasdk.type

import androidx.annotation.StringDef
import co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW
import co.astrnt.qasdk.type.InterviewType.CLOSE_SECTION
import co.astrnt.qasdk.type.InterviewType.CLOSE_TEST

@StringDef(CLOSE_INTERVIEW, CLOSE_TEST, CLOSE_SECTION)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class InterviewTypes 