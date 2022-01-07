package co.astrnt.qasdk.type

import androidx.annotation.StringDef
import co.astrnt.qasdk.type.MediaType.AUDIO
import co.astrnt.qasdk.type.MediaType.VIDEO

@StringDef(VIDEO, AUDIO)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class MediaTypes 