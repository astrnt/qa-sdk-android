package co.astrnt.qasdk.type

import androidx.annotation.StringDef

@StringDef(ElapsedTimeType.SECTION, ElapsedTimeType.TEST, ElapsedTimeType.PREPARATION)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class ElapsedTime 