package co.astrnt.qasdk.core

import co.astrnt.qasdk.dao.InterviewApiDao
import co.astrnt.qasdk.dao.InterviewResultApiDao
import co.astrnt.qasdk.dao.LogDao
import co.astrnt.qasdk.type.InterviewType.ASTRONAUT_PROFILE
import co.astrnt.qasdk.type.InterviewType.CLOSE_APTITUDE
import co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW
import co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW_PROFILE
import co.astrnt.qasdk.type.InterviewType.CLOSE_SECTION
import co.astrnt.qasdk.type.InterviewType.CLOSE_TEST
import co.astrnt.qasdk.type.InterviewType.OPEN
import co.astrnt.qasdk.utils.LogUtil.addNewLog

abstract class InterviewObserver : MyObserver<InterviewResultApiDao>() {

    override fun onApiResultOk(resultApiDao: InterviewResultApiDao) {
        val data = resultApiDao.interview
        if (data == null) {
            onApiResultError("", "Code not found or interview already finished", "error")
        } else {
            astrntSDK.saveInterviewResult(resultApiDao, data, false)
            if (data.type!!.contains(OPEN)) {
                if (data.interviewCode != null) {
                    addNewLog(data.interviewCode,
                            LogDao("Response API",
                                    "Success, will move to Register"
                            )
                    )
                }
                onNeedToRegister(data)
            } else {
                astrntSDK.saveInterview(data, resultApiDao.token, data.interviewCode)
                val information = resultApiDao.information
                if (information != null && information.isFinished) {
                    onApiResultError("", information.message, "error")
                } else {
                    val interviewCode = astrntSDK.interviewCode
                    when (data.type) {
                        CLOSE_INTERVIEW, CLOSE_INTERVIEW_PROFILE -> {
                            if (interviewCode != null) {
                                addNewLog(interviewCode,
                                        LogDao("Response API",
                                                "Success, will move to Video Interview"
                                        )
                                )
                            }
                            onInterviewType(data)
                        }
                        CLOSE_TEST -> {
                            if (interviewCode != null) {
                                addNewLog(interviewCode,
                                        LogDao("Response API",
                                                "Success, will move to MCQ Interview"
                                        )
                                )
                            }
                            onTestType(data)
                        }
                        CLOSE_SECTION -> {
                            if (interviewCode != null) {
                                addNewLog(interviewCode,
                                        LogDao("Response API",
                                                "Success, will move to Section Interview"
                                        )
                                )
                            }
                            onSectionType(data)
                        }
                        ASTRONAUT_PROFILE -> {
                            if (interviewCode != null) {
                                addNewLog(interviewCode,
                                        LogDao("Response API",
                                                "Success, will move to Astronaut Profile"
                                        )
                                )
                            }
                            onAstronautProfileType(data)
                        }
                        CLOSE_APTITUDE -> {
                            if (interviewCode != null) {
                                addNewLog(interviewCode,
                                        LogDao("Response API",
                                                "Success, will move to Close Aptitude Rating Scale"
                                        )
                                )
                            }
                            onAptitudeType(data)
                        }
                        else -> {
                            if (resultApiDao.title != null) {
                                onApiResultError(resultApiDao.title.toString(), resultApiDao.message.toString(), "error")
                            } else {
                                onApiResultError("", resultApiDao.message.toString(), "error")
                            }
                            if (interviewCode != null) {
                                addNewLog(interviewCode,
                                        LogDao("Enter Code Response API",
                                                "Error : " + resultApiDao.message
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    abstract fun onNeedToRegister(interview: InterviewApiDao)
    abstract fun onInterviewType(interview: InterviewApiDao)
    abstract fun onTestType(interview: InterviewApiDao)
    abstract fun onSectionType(interview: InterviewApiDao)
    abstract fun onAstronautProfileType(interview: InterviewApiDao)
    abstract fun onAptitudeType(interview: InterviewApiDao)
}