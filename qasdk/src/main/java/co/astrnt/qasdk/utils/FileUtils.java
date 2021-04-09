package co.astrnt.qasdk.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import co.astrnt.qasdk.dao.LogDao;
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

    public static File makeAndGetSubDirectoryDownload(String appName, String subFolderName, String jobNameFolder, String interviewCode) {
        File appNameDirectory = new File(Environment.getExternalStorageDirectory(),appName);

        if (!appNameDirectory.exists()) {
            Timber.e("Create app folder in root");
            appNameDirectory.mkdirs();
            LogUtil.addNewLog(interviewCode,
                    new LogDao("CheatSheet Info",
                            "Create app folder in root"
                    )
            );
        }

        File subDirectory = new File(appNameDirectory.getAbsolutePath(), subFolderName);
        if (!subDirectory.exists()) {
            Timber.e("Create sub folder in AstronautQ&A");
            subDirectory.mkdirs();
            LogUtil.addNewLog(interviewCode,
                    new LogDao("CheatSheet Info",
                            "Create sub folder "+subFolderName
                    )
            );
        }

        File jobName = new File(subDirectory.getAbsolutePath(), jobNameFolder);
        if (!jobName.exists()) {
            Timber.e("Create sub folder in job name");
            jobName.mkdirs();
            LogUtil.addNewLog(interviewCode,
                    new LogDao("CheatSheet Info",
                            "Create sub folder in job name "+jobNameFolder
                    )
            );
        }
        return jobName;
    }

}