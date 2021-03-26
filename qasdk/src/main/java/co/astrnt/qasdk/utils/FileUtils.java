package co.astrnt.qasdk.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

public class FileUtils {

    public static File makeAndGetSubDirectory(Context context, String interviewCode, String subFolderName) {
        // determine the profile directory
        File interviewDirectory = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), interviewCode);
        if (!interviewDirectory.exists()) {
            interviewDirectory.mkdir();
        }

        File subDirectory = new File(interviewDirectory.getAbsolutePath(), subFolderName);
        if (!subDirectory.exists()) {
            subDirectory.mkdirs();
        }

        return subDirectory;
    }

    public static File makeAndGetSubDirectoryDownload(String appName, String subFolderName, String jobNameFolder) {
        File appNameDirectory = new File(Environment.getExternalStorageDirectory(),appName);

        if (!appNameDirectory.exists()) {
            Timber.e("Create app folder in root");
            appNameDirectory.mkdirs();
        }

        File subDirectory = new File(appNameDirectory.getAbsolutePath(), subFolderName);
        if (!subDirectory.exists()) {
            Timber.e("Create sub folder in AstronautQ&A");
            subDirectory.mkdirs();
        }

        File jobName = new File(subDirectory.getAbsolutePath(), jobNameFolder);
        if (!subDirectory.exists()) {
            Timber.e("Create sub folder in job name");
            jobName.mkdirs();
        }
        return jobName;
    }

}