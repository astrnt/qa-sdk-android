package co.astrnt.qasdk.core

import co.astrnt.qasdk.AstrntSDK
import co.astrnt.qasdk.dao.InterviewApiDao
import co.astrnt.qasdk.dao.InterviewResultApiDao
import co.astrnt.qasdk.dao.LogDao
import co.astrnt.qasdk.type.InterviewType.ASTRONAUT_PROFILE
import co.astrnt.qasdk.type.InterviewType.CLOSE_APTITUDE
import co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW
import co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW_PROFILE
import co.astrnt.qasdk.type.InterviewType.CLOSE_SECTION
import co.astrnt.qasdk.type.InterviewType.CLOSE_TEST
import co.astrnt.qasdk.utils.LogUtil.addNewLog


abstract class RegisterObserver : MyObserver<InterviewResultApiDao>() {
    override fun onApiResultOk(resultApiDao: InterviewResultApiDao?) {
        astrntSDK.clearDb()
        astrntSDK = AstrntSDK()
        val data = resultApiDao?.interview
        val interviewCode = data?.interviewCode
        astrntSDK.saveInterviewResult(resultApiDao, data, false)
        when (data?.type) {
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
            CLOSE_APTITUDE -> {
                if (interviewCode != null) {
                    addNewLog(interviewCode,
                            LogDao("Response API",
                                    "Success, will move to Close Aptitude Rating Scale"
                            )
                    )
                }
                onAptitudeType(data)
                if (interviewCode != null) {
                    addNewLog(interviewCode,
                            LogDao("Response API",
                                    "Success, will move to Astronaut Profile"
                            )
                    )
                }
                onAstronautProfileType(data)
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
            else -> {
                val message = resultApiDao?.message
                if (resultApiDao?.title != null) {
                    onApiResultError(resultApiDao.title.toString(), message.toString(), "error")
                } else {
                    onApiResultError("", message.toString(), "error")
                }
                addNewLog(interviewCode,
                        LogDao("Register Response API",
                                "Error : $message"
                        )
                )
            }
        }
    }

    abstract fun onInterviewType(interview: InterviewApiDao?)
    abstract fun onTestType(interview: InterviewApiDao?)
    abstract fun onSectionType(interview: InterviewApiDao?)
    abstract fun onAstronautProfileType(interview: InterviewApiDao?)
    abstract fun onAptitudeType(interview: InterviewApiDao?)
}