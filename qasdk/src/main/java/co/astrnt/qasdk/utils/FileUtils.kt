package co.astrnt.qasdk.utils

import android.content.Context
import android.os.Environment
import co.astrnt.qasdk.dao.LogDao
import timber.log.Timber
import java.io.File

object FileUtils {
    @JvmStatic
    fun makeAndGetSubDirectory(context: Context, interviewCode: String?, subFolderName: String?): File {
        // determine the profile directory
        val interviewDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), interviewCode)
        if (!interviewDirectory.exists()) {
            interviewDirectory.mkdir()
        }
        val subDirectory = File(interviewDirectory.absolutePath, subFolderName)
        if (!subDirectory.exists()) {
            subDirectory.mkdirs()
        }
        return subDirectory
    }

    @JvmStatic
    fun makeAndGetSubDirectoryDownload(appName: String?, subFolderName: String, jobNameFolder: String, interviewCode: String?): File {
        val appNameDirectory = File(Environment.getExternalStorageDirectory(), appName)
        if (!appNameDirectory.exists()) {
            appNameDirectory.mkdirs()
            LogUtil.addNewLog(interviewCode,
                    LogDao("CheatSheet Info",
                            "Create app folder in root"
                    )
            )
        }
        val subDirectory = File(appNameDirectory.absolutePath, subFolderName)
        if (!subDirectory.exists()) {
            subDirectory.mkdirs()
            LogUtil.addNewLog(interviewCode,
                    LogDao("CheatSheet Info",
                            "Create sub folder $subFolderName"
                    )
            )
        }
        val jobName = File(subDirectory.absolutePath, jobNameFolder)
        if (!jobName.exists()) {
            jobName.mkdirs()
            LogUtil.addNewLog(interviewCode,
                    LogDao("CheatSheet Info",
                            "Create sub folder in job name $jobNameFolder"
                    )
            )
        }
        return jobName
    }
}