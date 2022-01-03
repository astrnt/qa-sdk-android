package co.astrnt.qasdk.type

import androidx.annotation.StringDef

@StringDef(UploadStatusType.PENDING, UploadStatusType.NOT_ANSWER, UploadStatusType.UPLOADING, UploadStatusType.UPLOADED, UploadStatusType.COMPRESSED, UploadStatusType.COMPRESSING)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class UploadStatusState 