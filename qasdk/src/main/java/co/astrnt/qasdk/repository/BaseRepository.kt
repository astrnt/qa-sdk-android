package co.astrnt.qasdk.repository

import android.content.Context
import co.astrnt.qasdk.AstrntSDK
import co.astrnt.qasdk.core.AstronautApi
import co.astrnt.qasdk.utils.ServiceUtils.isMyServiceRunning
import co.astrnt.qasdk.utils.services.SendLogService


open class BaseRepository(@JvmField val mAstronautApi: AstronautApi) {
    @JvmField var astrntSDK = AstrntSDK()
    fun sendLog(context: Context?) {
        if (!isMyServiceRunning(context!!, SendLogService::class.java)) {
            SendLogService.start(context)
        }
    }
}
