package co.astrnt.qasdk.core

import co.astrnt.qasdk.AstrntSDK
import co.astrnt.qasdk.dao.BaseApiDao
import co.astrnt.qasdk.dao.InterviewResultApiDao
import co.astrnt.qasdk.dao.LogDao
import co.astrnt.qasdk.utils.LogUtil.addNewLog
import com.google.gson.Gson
import io.reactivex.Observer
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


abstract class MyObserver<T : BaseApiDao> : Observer<T> {
    @JvmField var astrntSDK = AstrntSDK()
    override fun onComplete() {
        onApiResultCompleted()
    }

    override fun onError(e: Throwable) {
        onComplete()
        var message: String?
        if (e is HttpException) {
            try {
                val httpException = e
                if (httpException.code() == 429) {
                    message = httpException.message
                    onApiResultError("", message.toString(), "error")
                } else {
                    val body = e.response()!!.errorBody()
                    if (body != null) {
                        val apiDao = Gson().fromJson(body.string(), BaseApiDao::class.java)
                        message = apiDao.message
                        if (apiDao.title != null) {
                            onApiResultError(apiDao.title.toString(), message.toString(), "error")
                        } else {
                            onApiResultError("", message.toString(), "error")
                        }
                    } else {
                        message = e.message
                        onApiResultError("", message.toString(), "error")
                    }
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
                message = e2.message
                onApiResultError("", message.toString(), "exception")
            }
        } else if (e is UnknownHostException) {
            message = e.message
            onApiResultError("", message.toString(), "exception")
        } else if (e is SocketTimeoutException) {
            message = e.message
            onApiResultError("", message.toString(), "exception")
        } else {
            message = e.message
            System.err.println(message)
            e.printStackTrace()
            onApiResultError("", message.toString(), "exception")
        }
        val interviewCode = astrntSDK.interviewCode
        if (message != null && message.lowercase().contains("unable to resolve host")) {
            addNewLog(interviewCode,
                    LogDao("Response API",
                            "Failed, No Internet Connection"
                    )
            )
        } else {
            addNewLog(interviewCode,
                    LogDao("Response API",
                            "Response Error : $message"
                    )
            )
        }
    }

    override fun onNext(t: T) {
        onComplete()
        val interviewCode = astrntSDK.interviewCode
        if (t == null) {
            onApiResultError("", "Failed connect to server", "error")
            addNewLog(interviewCode,
                    LogDao("Response API",
                            "Error : Failed connect to server"
                    )
            )
            return
        }
        if (t.status != null && t.status!!.contains("error")) {
            if (t is InterviewResultApiDao) {
                val data = t as InterviewResultApiDao
                val interviewApiDao = astrntSDK.currentInterview
                if (interviewApiDao != null) {
                    if (data.token != null && data.token!!.isNotEmpty()) {
                        addNewLog(interviewCode,
                                LogDao("Token Changed",
                                        "New Token : " + data.token
                                )
                        )
                        astrntSDK.saveInterview(interviewApiDao, data.token, data.interview_code)
                    }
                    if (interviewCode != null) {
                        var message: String? = ""
                        if (t.message != null) {
                            message = t.message
                        }
                        addNewLog(interviewCode,
                                LogDao("Response API",
                                        "Error : $message"
                                )
                        )
                    }
                }
            }
            var message: String? = ""
            if (t.message != null) {
                message = t.message
            }
            if (t.title != null) {
                onApiResultError(t.title.toString(), message.toString(), "error")
            } else {
                onApiResultError("", message.toString(), "error")
            }
            if (interviewCode != null) {
                if (t.message != null) {
                    message = t.message
                }
                addNewLog(interviewCode,
                        LogDao("Response API",
                                "Error : $message"
                        )
                )
            }
        } else {
            var message: String? = ""
            if (t.message != null) {
                message = t.message
            }
            if (interviewCode != null) {
                addNewLog(interviewCode,
                        LogDao("Response API",
                                "Success : $message"
                        )
                )
            }
            onApiResultOk(t)
        }
    }

    abstract fun onApiResultCompleted()
    abstract fun onApiResultError(title: String?, message: String?, code: String?)
    abstract fun onApiResultOk(baseApiDao: T)
}