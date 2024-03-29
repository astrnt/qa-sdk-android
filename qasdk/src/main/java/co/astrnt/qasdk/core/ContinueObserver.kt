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
import co.astrnt.qasdk.utils.LogUtil.addNewLog

abstract class ContinueObserver : MyObserver<InterviewResultApiDao>() {

    override fun onApiResultOk(baseApiDao: InterviewResultApiDao) {
        var currentInterview = astrntSDK.currentInterview
        val interviewCode = astrntSDK.interviewCode
        val newInterview = baseApiDao.interview
        val information = baseApiDao.information
        if (information!!.isFinished) {
            astrntSDK.setInterviewFinished()
            astrntSDK.isFinishInterview = false
            astrntSDK.isShowUpload = true
        }
        when (newInterview?.type) {
            CLOSE_INTERVIEW, CLOSE_SECTION, CLOSE_INTERVIEW_PROFILE, CLOSE_APTITUDE, CLOSE_TEST -> {
                addNewLog(interviewCode,
                        LogDao("Response API",
                                "Success, will move to Info"
                        )
                )
                if (interviewCode == baseApiDao.interview_code) {
//                    astrntSDK.updateInterviewData(currentInterview, newInterview);
                    if (!astrntSDK.isSectionInterview) {
                        astrntSDK.updateDurationLeft(currentInterview, newInterview.duration_left)
                        astrntSDK.updateTrySampleQuestion(currentInterview, newInterview.trySampleQuestion)
                    }
                    currentInterview = astrntSDK.currentInterview
                    astrntSDK.saveInterviewResult(baseApiDao, currentInterview, true)
                } else {
                    astrntSDK.saveInterviewResult(baseApiDao, newInterview, true)
                }
                onContinueInterview()
            }
            ASTRONAUT_PROFILE -> {
                if (interviewCode != null) {
                    addNewLog(interviewCode,
                            LogDao("Response API",
                                    "Success, will move to Astronaut Profile"
                            )
                    )
                }
                onAstronautProfileType(newInterview)
            }
            else -> {
                val message = baseApiDao.message
                if (baseApiDao.title != null) {
                    onApiResultError(baseApiDao.title.toString(), message.toString(), "error")
                } else {
                    onApiResultError("", message.toString(), "error")
                }
                addNewLog(interviewCode,
                        LogDao("Continue Response API",
                                "Error : $message"
                        )
                )
            }
        }
    }

    abstract fun onContinueInterview()
    abstract fun onAstronautProfileType(interview: InterviewApiDao)
}