package co.astrnt.qasdk.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import net.gotev.uploadservice.Placeholders
import net.gotev.uploadservice.UploadNotificationConfig

object UploadNotifConfig {
    @JvmStatic
    fun getNotificationConfig(context: Context?, activity: Activity?, uploadCounter: String?): UploadNotificationConfig {
        val config = UploadNotificationConfig()
        config.setTitleForAllStatuses(uploadCounter)
                .setNotificationChannelId("Astronaut Q&A")
                .setRingToneEnabled(false)
                .setClearOnActionForAllStatuses(true)
        if (activity != null) {
            val clickIntent = PendingIntent.getActivity(
                    context, 1, Intent(context, activity.javaClass), PendingIntent.FLAG_UPDATE_CURRENT)
            config.setClickIntentForAllStatuses(clickIntent)
        }
        config.progress.message = "Uploaded " + Placeholders.UPLOAD_RATE + " - " + Placeholders.PROGRESS
        config.progress.iconColorResourceID = Color.parseColor("#7C9A76")
        config.completed.message = "Upload completed successfully in " + Placeholders.ELAPSED_TIME
        config.completed.iconColorResourceID = Color.parseColor("#2F80ED")
        config.error.message = "Error while uploading"
        config.error.iconColorResourceID = Color.parseColor("#EA4D4D")
        config.cancelled.message = "Upload has been cancelled"
        config.cancelled.iconColorResourceID = Color.parseColor("#EA4D4D")
        return config
    }

    @JvmStatic
    fun getSingleNotificationConfig(uploadCounter: String?): UploadNotificationConfig {
        return getNotificationConfig(null, null, uploadCounter)
    }
}