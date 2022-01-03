package co.astrnt.qasdk.upload

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import co.astrnt.qasdk.AstrntSDK
import co.astrnt.qasdk.R
import co.astrnt.qasdk.dao.BaseApiDao
import co.astrnt.qasdk.dao.InterviewApiDao
import co.astrnt.qasdk.dao.LogDao
import co.astrnt.qasdk.dao.QuestionApiDao
import co.astrnt.qasdk.event.UploadComplete
import co.astrnt.qasdk.event.UploadEvent
import co.astrnt.qasdk.type.UploadStatusType
import co.astrnt.qasdk.utils.FileUploadHelper.uploadVideo
import co.astrnt.qasdk.utils.LogUtil.addNewLog
import co.astrnt.qasdk.utils.ServiceUtils.isMyServiceRunning
import co.astrnt.qasdk.utils.UploadNotifConfig.getSingleNotificationConfig
import co.astrnt.qasdk.utils.services.SendLogService
import co.astrnt.qasdk.utils.services.SendLogService.Companion.start
import co.astrnt.qasdk.videocompressor.services.VideoCompressService
import com.google.gson.Gson
import net.gotev.uploadservice.ServerResponse
import net.gotev.uploadservice.UploadInfo
import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.UploadStatusDelegate
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.util.*

class SingleVideoUploadService : Service(), UploadStatusDelegate {
    private var questionId: Long = 0
    private var context: Context? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var mTimer: Timer? = null
    private var currentQuestion: QuestionApiDao? = null
    private var astrntSDK: AstrntSDK? = null
    private var isDoingCompress = true
    var interviewApiDao: InterviewApiDao? = null
    private var mNotifyManager: NotificationManager? = null
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent != null && intent.extras != null) {
            questionId = intent.getLongExtra(EXT_QUESTION_ID, 0)
            currentQuestion = astrntSDK!!.searchQuestionById(questionId)
            createNotification()
            if (mTimer != null) {
                mTimer!!.cancel()
                mTimer = null
                mTimer = Timer()
            } else {
                mTimer = Timer()
            }
            mTimer!!.scheduleAtFixedRate(TimeDisplayTimerTask(), 5000, NOTIFY_INTERVAL)
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        astrntSDK = AstrntSDK()
    }

    private fun createNotification() {
        if (currentQuestion == null) {
            return
        }
        val mNotificationId = currentQuestion!!.id.toInt()
        val channelId = "Astronaut Q&A"
        val mBuilder = NotificationCompat.Builder(context!!, channelId)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_autorenew_white_24dp)
                .setContentTitle("Astronaut Q&A")
                .setContentText("Upload Video")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Upload Video"
            val description = "Astronaut Q&A Upload Video"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setSound(null, null)
            mNotifyManager = context!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (mNotifyManager != null) {
                mNotifyManager!!.createNotificationChannel(channel)
            }
            startForeground(mNotificationId, mBuilder.build())
        } else {
            mNotifyManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }
        mNotifyManager!!.notify(mNotificationId, mBuilder.build())
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun doUploadVideo() {
        astrntSDK!!.saveRunningUploading(true)
        isDoingCompress = true
        interviewApiDao = astrntSDK!!.currentInterview
        try {
            if (currentQuestion!!.videoPath == null) {
                astrntSDK!!.getVideoFile(context, interviewApiDao?.interviewCode, currentQuestion!!.id)
                stopService()
            }
            val file = File(currentQuestion!!.videoPath)
            if (!file.exists()) {
                astrntSDK!!.getVideoFile(context, interviewApiDao?.interviewCode, currentQuestion!!.id)
                stopService()
            } else {
                if (currentQuestion!!.videoPath.contains("_raw.mp4")) {
                    astrntSDK!!.markAsPending(currentQuestion, currentQuestion!!.videoPath)
                    addNewLog(interviewApiDao?.interviewCode,
                            LogDao("Background Upload (Pending)",
                                    "Video Still RAW File, will start compress first")
                    )
                    if (!isMyServiceRunning(context!!, VideoCompressService::class.java)) {
                        if (!astrntSDK!!.isRunningCompressing!!) {
                            Handler(Looper.getMainLooper()).post {
                                Timber.i("Start compress from do Upload Video")
                                addNewLog(astrntSDK!!.interviewCode, LogDao("Start compress", "From Upload Video " + currentQuestion!!.id))
                                VideoCompressService.start(context, currentQuestion!!.videoPath, currentQuestion!!.id, astrntSDK!!.interviewCode)
                            }
                        }
                    }
                    stopService()
                } else {
                    if (currentQuestion!!.uploadStatus == UploadStatusType.NOT_ANSWER || currentQuestion!!.uploadStatus == UploadStatusType.UPLOADED) {
                        stopService()
                        return
                    }
                    addNewLog(interviewApiDao?.interviewCode,
                            LogDao("Background Upload (Upload)",
                                    "Video Still Starting Upload")
                    )
                    val allVideoQuestion: List<QuestionApiDao> = astrntSDK!!.allVideoQuestion
                    var counter = 0
                    val totalQuestion = allVideoQuestion.size
                    for (i in allVideoQuestion.indices) {
                        val item = allVideoQuestion[i]
                        if (item.id == currentQuestion!!.id) {
                            counter = i
                        }
                    }
                    val uploadMessage = "Uploading video " + (counter + 1) + " from " + totalQuestion
                    val notificationConfig = getSingleNotificationConfig(uploadMessage)
                    notificationConfig.setNotificationChannelId(UploadService.NAMESPACE)
                    notificationConfig.setClearOnActionForAllStatuses(true)
                    notificationConfig.setRingToneEnabled(false)
                    astrntSDK!!.markUploading(currentQuestion)
                    val apiUrl = astrntSDK!!.apiUrl + "v2/video/upload"
                    try {
                        val uploadId = uploadVideo(context, interviewApiDao!!, currentQuestion!!, apiUrl, " from service")
                                .setNotificationConfig(notificationConfig)
                                .setDelegate(this).startUpload()
                        astrntSDK!!.saveUploadId(uploadId)
                    } catch (exc: FileNotFoundException) {
                        addNewLog(interviewApiDao?.interviewCode,
                                LogDao("Uploading Info",
                                        "Failed FileNotFoundException " + exc.message)
                        )
                        Timber.e("File not exception")
                    } catch (exc: IllegalArgumentException) {
                        Timber.e("IllegalArgumentException exception")
                        addNewLog(interviewApiDao?.interviewCode,
                                LogDao("Uploading Info",
                                        "Failed IllegalArgumentException " + exc.message)
                        )
                    } catch (exc: MalformedURLException) {
                        addNewLog(interviewApiDao?.interviewCode,
                                LogDao("Uploading Info",
                                        "Failed MalformedURLException " + exc.message)
                        )
                        Timber.e("MalformedURLException exception")
                    }
                }
            }
        } catch (exc: Exception) {
            addNewLog(interviewApiDao?.interviewCode,
                    LogDao("Background Upload (Exc)",
                            exc.message!!)
            )
            stopService()
        }
    }

    private fun sendLog() {
        Handler(Looper.getMainLooper()).post {
            if (!isMyServiceRunning(context!!, SendLogService::class.java)) {
                start(context)
            }
        }
    }

    fun stopService() {
        astrntSDK!!.saveRunningUploading(false)
        sendLog()
        if (mTimer != null) mTimer!!.cancel()
        if (mNotifyManager != null) mNotifyManager!!.cancelAll()
        stopSelf()
    }

    override fun onProgress(context: Context, uploadInfo: UploadInfo) {
        if (uploadInfo != null) {
            try {
                astrntSDK!!.updateProgress(currentQuestion, uploadInfo.progressPercent.toDouble())
            } catch (e: Exception) {
                Timber.e("Error %s", e.message)
                if (e.message!!.contains(getString(R.string.error_deleted_thread))) {
                    stopService()
                }
            }
        } else {
            Timber.i("upload progress null")
        }
        EventBus.getDefault().post(UploadEvent())
    }

    override fun onError(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse, exception: Exception) {
        try {
            astrntSDK!!.removeUploadId()
        } catch (e: Exception) {
        }
        Timber.e("Video Upload Error : ")
        var message: String? = ""
        if (serverResponse.body != null) {
            try {
                val baseApiDao = Gson().fromJson(serverResponse.bodyAsString, BaseApiDao::class.java)
                message = baseApiDao.message
                Timber.e(baseApiDao.message)
            } catch (e: Exception) {
                message = e.message
            }
        } else {
            if (exception != null) {
                message = exception.message
            }
        }
        Timber.e("Video Upload Error : %s", message)
        try {
            addNewLog(interviewApiDao!!.interviewCode,
                    LogDao("Background Upload (Error)",
                            "Error $message"
                    )
            )
            astrntSDK!!.markAsCompressed(currentQuestion)
            stopService()
        } catch (e: Exception) {
        }
    }

    override fun onCompleted(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
        try {
            astrntSDK!!.removeUploadId()
            astrntSDK!!.markUploaded(currentQuestion)
            EventBus.getDefault().post(UploadComplete())
            stopService()
            addNewLog(interviewApiDao!!.interviewCode,
                    LogDao("Background Upload (Complete)",
                            "Success uploaded for question id " + currentQuestion!!.id
                                    + " on service")
            )
            val uploadingVideo = astrntSDK!!.getPending(UploadStatusType.PENDING)
            val compressedVideo = astrntSDK!!.getPending(UploadStatusType.COMPRESSED)
            isDoingCompress = true
            for (item in uploadingVideo) {
                if (isDoingCompress) {
                    if (!isMyServiceRunning(context, VideoCompressService::class.java)) {
                        if (!astrntSDK!!.isRunningCompressing) {
                            addNewLog(astrntSDK!!.interviewCode, LogDao("Start compress",
                                    "From pending status " + item.id))
                            Handler(Looper.getMainLooper()).postDelayed({ VideoCompressService.start(context, item.videoPath, item.id, astrntSDK!!.interviewCode) }, 1000)
                            isDoingCompress = false
                        }
                    }
                }
            }
            for (item in compressedVideo) {
                if (isDoingCompress) {
                    if (!isMyServiceRunning(context, SingleVideoUploadService::class.java)) {
                        if (!astrntSDK!!.isRunningUploading) {
                            addNewLog(astrntSDK!!.interviewCode, LogDao("Current status",
                                    "Uploading from compressed " + item.id))
                            start(context, item.id, interviewApiDao!!.interviewCode)
                            isDoingCompress = false
                        }
                    }
                }
            }
            val compressingVideo = astrntSDK!!.getPending(UploadStatusType.COMPRESSING)
            for (item in compressingVideo) {
                if (isDoingCompress) {
                    if (!isMyServiceRunning(context, VideoCompressService::class.java)) {
                        if (!astrntSDK!!.isRunningCompressing) {
                            astrntSDK!!.markAsPending(item, item.videoPath)
                            Timber.i("current status compress is compressing")
                            addNewLog(astrntSDK!!.interviewCode, LogDao("Status compressing", "From current compressing " + item.id))
                            Handler(Looper.getMainLooper()).postDelayed({ VideoCompressService.start(context, item.videoPath, item.id, astrntSDK!!.interviewCode) }, 1000)
                            isDoingCompress = false
                        }
                    } else {
                        Timber.i("still running compress successing")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("Error %s", e.message)
        }
    }

    override fun onCancelled(context: Context, uploadInfo: UploadInfo) {
        try {
            astrntSDK!!.removeUploadId()
        } catch (e: Exception) {
        }
        try {
            astrntSDK!!.markAsCompressed(currentQuestion)
            EventBus.getDefault().post(UploadEvent())
            addNewLog(interviewApiDao!!.interviewCode,
                    LogDao("Background Upload (Cancelled)",
                            "Cancelled with id " + currentQuestion!!.id
                    )
            )
            stopService()
        } catch (e: Exception) {
        }
    }

    internal inner class TimeDisplayTimerTask : TimerTask() {
        override fun run() {
            mHandler.post {
                currentQuestion = astrntSDK!!.searchQuestionById(questionId)
                if (currentQuestion != null) {
                    if (currentQuestion!!.uploadStatus == UploadStatusType.COMPRESSED) {
                        if (mNotifyManager != null) mNotifyManager!!.cancelAll()
                        doUploadVideo()
                    } else {
                        stopService()
                    }
                } else {
                    stopService()
                }
            }
        }
    }

    companion object {
        const val EXT_QUESTION_ID = "SingleVideoUploadService.QuestionId"
        const val NOTIFY_INTERVAL = (2 * 60 * 1000).toLong()
        @JvmStatic
        fun start(context: Context?, questionId: Long, interviewCode: String?) {
            try {
                val intent = Intent(context, SingleVideoUploadService::class.java)
                        .putExtra(EXT_QUESTION_ID, questionId)
                ContextCompat.startForegroundService(context!!, intent)
            } catch (e: Exception) {
                addNewLog(interviewCode,
                        LogDao("Failed to start upload",
                                "Because " + e.message
                        )
                )
            }
        }
    }
}