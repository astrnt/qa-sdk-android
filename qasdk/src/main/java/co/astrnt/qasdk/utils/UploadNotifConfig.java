package co.astrnt.qasdk.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import net.gotev.uploadservice.UploadNotificationConfig;

import static net.gotev.uploadservice.Placeholders.ELAPSED_TIME;
import static net.gotev.uploadservice.Placeholders.PROGRESS;
import static net.gotev.uploadservice.Placeholders.UPLOAD_RATE;

public class UploadNotifConfig {

    public static UploadNotificationConfig getNotificationConfig(Context context, Activity activity, String uploadCounter) {
        UploadNotificationConfig config = new UploadNotificationConfig();

        config.setTitleForAllStatuses(uploadCounter)
                .setNotificationChannelId("Astronaut")
                .setRingToneEnabled(false)
                .setClearOnActionForAllStatuses(true);

        if (activity != null) {
            PendingIntent clickIntent = PendingIntent.getActivity(
                    context, 1, new Intent(context, activity.getClass()), PendingIntent.FLAG_UPDATE_CURRENT);

            config.setClickIntentForAllStatuses(clickIntent);
        }

        config.getProgress().message = "Uploaded " + UPLOAD_RATE + " - " + PROGRESS;
        config.getProgress().iconColorResourceID = Color.parseColor("#7C9A76");

        config.getCompleted().message = "Upload completed successfully in " + ELAPSED_TIME;
        config.getCompleted().iconColorResourceID = Color.parseColor("#2F80ED");

        config.getError().message = "Error while uploading";
        config.getError().iconColorResourceID = Color.parseColor("#EA4D4D");

        config.getCancelled().message = "Upload has been cancelled";
        config.getCancelled().iconColorResourceID = Color.parseColor("#EA4D4D");

        return config;
    }

    public static UploadNotificationConfig getSingleNotificationConfig(String uploadCounter) {
        return getNotificationConfig(null, null, uploadCounter);
    }
}
