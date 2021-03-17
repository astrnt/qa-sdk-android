package co.astrnt.qasdk.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

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

    public static File makeAndGetSubDirectoryDownload(String subFolderName, String interviewCode) {
        // determine the profile directory
        File interviewDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), subFolderName);
        if (!interviewDirectory.exists()) {
            interviewDirectory.mkdir();
        }

        File subDirectory = new File(interviewDirectory.getAbsolutePath(), interviewCode);
        if (!subDirectory.exists()) {
            subDirectory.mkdirs();
        }

        return subDirectory;
    }

}