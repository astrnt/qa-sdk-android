package co.astrnt.qasdk.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

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

    public static File makeAndGetSubDirectoryDownload(String subFolderName, String jobName) {
        // determine the profile directory
        File interviewDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), subFolderName);
        if (!interviewDirectory.exists()) {
            Timber.e("createfolderrr111 %s", interviewDirectory.getAbsolutePath());
            interviewDirectory.mkdirs();
        }

        File subDirectory = new File(interviewDirectory.getAbsolutePath(), jobName);
        if (!subDirectory.exists()) {
            Timber.e("createfolderrr2222 %s", subDirectory.getAbsolutePath());
            subDirectory.mkdirs();
        }

        return subDirectory;
    }

    public static File makeAndGetSubDownload(String subFolderName) {
        // determine the profile directory
        File interviewDirectory = new File(Environment.getExternalStorageDirectory().toString(), subFolderName);
        if (!interviewDirectory.exists()) {
            Timber.e("MKDIRRR 2222"+interviewDirectory.getPath());
            Timber.e("MKDIRRR 2222xx"+interviewDirectory.getAbsolutePath());
            interviewDirectory.mkdirs();
        }

        return interviewDirectory;
    }

}