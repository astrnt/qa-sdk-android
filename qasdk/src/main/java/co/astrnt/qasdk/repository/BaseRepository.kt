package co.astrnt.qasdk.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import co.astrnt.qasdk.AstrntSDK
import co.astrnt.qasdk.core.AstronautApi
import co.astrnt.qasdk.utils.services.SendLogWorkerJava


open class BaseRepository(@JvmField val mAstronautApi: AstronautApi) {
    @JvmField var astrntSDK = AstrntSDK()
    fun sendLog(context: Context?) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequest.Builder(SendLogWorkerJava::class.java)
            .addTag("sendlog")
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context!!).enqueue(request)
    }
}
