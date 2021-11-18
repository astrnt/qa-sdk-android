package co.astrnt.qasdk.utils.services

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
import co.astrnt.qasdk.constants.PreferenceKey
import co.astrnt.qasdk.core.AstronautApi
import co.astrnt.qasdk.core.MyObserver
import co.astrnt.qasdk.dao.BaseApiDao
import co.astrnt.qasdk.dao.LogDao
import co.astrnt.qasdk.utils.LogUtil.addNewLog
import co.astrnt.qasdk.utils.LogUtil.clearSentLog
import co.astrnt.qasdk.utils.LogUtil.getLog
import co.astrnt.qasdk.utils.LogUtil.lastLogIndex
import co.astrnt.qasdk.utils.LogUtil.saveLastLogIndex
import co.astrnt.qasdk.utils.LogUtil.timeZone
import com.orhanobut.hawk.Hawk
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

class SendLogService : Service() {
    private var context: Context? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var mTimer: Timer? = null
    private var astrntSDK: AstrntSDK? = null
    private var astronautApi: AstronautApi? = null
    private var mNotifyManager: NotificationManager? = null
    private var mBuilder: NotificationCompat.Builder? = null
    private var mNotificationId = 0
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        astrntSDK = AstrntSDK()
        astronautApi = astrntSDK!!.api
        createNotification()
        if (mTimer != null) {
            mTimer!!.cancel()
        } else {
            mTimer = Timer()
        }
        mTimer!!.scheduleAtFixedRate(TimeDisplayTimerTask(), 5000, NOTIFY_INTERVAL)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun doSendLog() {
        val interviewCode = astrntSDK!!.interviewCode
        val token = Hawk.get<String>(PreferenceKey.KEY_TOKEN)
        val map = HashMap<String, String?>()
        val logDaoList: List<LogDao> = getLog(interviewCode)
        val sentLog: MutableList<LogDao> = ArrayList()
        if (logDaoList.isEmpty()) {
            stopService()
        } else {
            val companyId = Hawk.get<String>(PreferenceKey.KEY_COMPANY_ID)
            val jobId = Hawk.get<String>(PreferenceKey.KEY_JOB_ID)
            val candidateId = Hawk.get<String>(PreferenceKey.KEY_CANDIDATE_ID)
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val version = String.format("%s %s", manufacturer, model)
            val os = "Android " + Build.VERSION.RELEASE
            val appVersion = Hawk.get<Any>("versionCode").toString() + " / " + Hawk.get("versionName")

            //TODO: get imei is restricted since Android 10
            val imei = "-"
            val timeZone = timeZone
            val lastLogIndex = lastLogIndex
            for (i in logDaoList.indices) {
                val logDao = logDaoList[i]
                sentLog.add(logDao)
                map["logs[$i][candidate_id]"] = candidateId
                map["logs[$i][company_id]"] = companyId
                map["logs[$i][interviewCode]"] = interviewCode
                map["logs[$i][job_id]"] = jobId
                map["logs[$i][event]"] = logDao.event
                map["logs[$i][log_time]"] = logDao.log_time
                map["logs[$i][message]"] = logDao.message
                map["logs[$i][time_zone]"] = timeZone
                map["logs[$i][imei]"] = imei
                map["logs[$i][version]"] = version
                map["logs[$i][os]"] = os
                map["logs[$i][app_version]"] = appVersion
                val index = lastLogIndex + i
                saveLastLogIndex(index)
            }
        }
        astrntSDK!!.saveLastApiCall("(/candidate/logs)")
        astronautApi!!.apiService.sendLog(token, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : MyObserver<BaseApiDao>() {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onApiResultCompleted() {}
                    override fun onApiResultError(title: String?, message: String?, code: String?) {
                        Timber.e(message)
                        var errorMessage = ""
                        if (message != null) {
                            errorMessage = "message : $message"
                        }
                        if (code != null) {
                            errorMessage = "$errorMessage httpCode : $code"
                        }
                        addNewLog(interviewCode,
                                LogDao("Hit API",
                                        "Error to Send Log $errorMessage"
                                )
                        )
                        mNotifyManager!!.notify(mNotificationId, mBuilder!!.build())
                        mNotifyManager!!.cancel(mNotificationId)
                        if (astrntSDK.isShowUpload) {
                            createHandlerForReSendLog()
                        } else {
                            stopService()
                        }
                    }

                    override fun onApiResultOk(apiDao: BaseApiDao) {
                        Timber.d(apiDao.message)
                        mNotifyManager!!.notify(mNotificationId, mBuilder!!.build())
                        mNotifyManager!!.cancel(mNotificationId)
                        clearSentLog(interviewCode, sentLog)
                        stopService()
                    }
                })
    }

    private fun createNotification() {
        mNotificationId = 1001
        val channelId = "Astronaut Q&A"
        mBuilder = NotificationCompat.Builder(context!!, channelId)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_send_white_24dp)
                .setContentTitle("Astronaut Q&A")
                .setContentText("Sending Log")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Send Log"
            val description = "Astronaut Q&A Send Log"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setSound(null, null)
            mNotifyManager = context!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (mNotifyManager != null) {
                mNotifyManager!!.createNotificationChannel(channel)
            }
            startForeground(mNotificationId, mBuilder?.build())
        } else {
            mNotifyManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }
        mNotifyManager!!.notify(mNotificationId, mBuilder?.build())
    }

    fun stopService() {
        mTimer!!.cancel()
        if (mNotifyManager != null) mNotifyManager!!.cancelAll()
        stopSelf()
    }

    internal inner class TimeDisplayTimerTask : TimerTask() {
        override fun run() {
            mHandler.post { doSendLog() }
        }
    }

    private fun createHandlerForReSendLog() {
        val thread: Thread = object : Thread() {
            override fun run() {
                Looper.prepare()
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(object : Runnable {
                    override fun run() {
                        doSendLog()
                        handler.removeCallbacks(this)
                    }
                }, 5000)
                Looper.loop()
            }
        }
        thread.start()
    }

    companion object {
        const val NOTIFY_INTERVAL = (60 * 1000).toLong()
        @JvmStatic
        fun start(context: Context?) {
            val intent = Intent(context, SendLogService::class.java)
            ContextCompat.startForegroundService(context!!, intent)
        }
    }
}