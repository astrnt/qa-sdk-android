package co.astrnt.qasdk.repository

import co.astrnt.qasdk.core.AstronautApi
import co.astrnt.qasdk.core.MyObserver
import co.astrnt.qasdk.dao.*
import co.astrnt.qasdk.dao.post.RegisterPost
import co.astrnt.qasdk.type.CustomFiledType
import co.astrnt.qasdk.type.ElapsedTime
import co.astrnt.qasdk.type.ElapsedTimeType
import co.astrnt.qasdk.utils.LogUtil.addNewLog
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*


class InterviewRepository(astronautApi: AstronautApi) : BaseRepository(astronautApi) {
    fun enterCode(interviewCode: String, version: Int): Observable<InterviewResultApiDao> {
        val map = HashMap<String, String>()
        map["interview_code"] = interviewCode
        map["device"] = "android"
        map["version"] = version.toString()
        map["session_timer"] = true.toString()
        return mAstronautApi.apiService.enterCode("", map)
    }

    fun registerUser(param: RegisterPost): Observable<InterviewResultApiDao> {
        param.device = "android"
        val map = HashMap<String, String?>()
        map["job_id"] = param.job_id.toString()
        map["company_id"] = param.company_id.toString()
        map["interview_code"] = param.interview_code
        map["fullname"] = param.fullname
        map["preferred_name"] = param.preferred_name
        map["email"] = param.email
        map["phone"] = param.phone
        map["device"] = param.device
        map["version"] = java.lang.String.valueOf(param.version)
        map["session_timer"] = true.toString()
        if (param.custom_fields != null) {
            for (i in param.custom_fields!!.indices) {
                val fieldsPost = param.custom_fields!![i]
                map["custom_fields[$i][id]"] = fieldsPost.id.toString()
                if (fieldsPost.inputType == CustomFiledType.CHECK_BOX) {
                    for (j in fieldsPost.values!!.indices) {
                        val item = fieldsPost.values!![j]
                        map["custom_fields[$i][value][$j]"] = item
                    }
                } else {
                    map["custom_fields[$i][value]"] = fieldsPost.value
                }
            }
        }
        return mAstronautApi.apiService.registerUser("", map)
    }

    fun startInterview(): Observable<InterviewStartApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        val token = interviewApiDao.token
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/interview/start)",
                        "Start Interview"
                )
        )
        astrntSDK.saveLastApiCall("(/interview/start)")
        if (astrntSDK.isSectionInterview) {
            astrntSDK.isContinueInterview = true
        }
        if (astrntSDK.isSelfPace) {
            astrntSDK.isContinueInterview = true
        }
        astrntSDK.updateInterviewOnGoing(interviewApiDao, true)
        return mAstronautApi.apiService.startInterview(token!!, map)
    }

    fun startSection(sectionApiDao: SectionApiDao): Observable<InterviewStartApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["candidate_id"] = interviewApiDao.candidate!!.id.toString()
        map["section_id"] = sectionApiDao.id.toString()
        val token = interviewApiDao.token
        if (!astrntSDK.isSelfPace) {
            updateElapsedTime(ElapsedTimeType.PREPARATION, sectionApiDao.id)
        }
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/section/start)",
                        "Start Section, number" + (astrntSDK.sectionIndex + 1) +
                                ", sectionId = " + sectionApiDao.id
                )
        )
        astrntSDK.saveLastApiCall("(/section/start)")
        astrntSDK.updateSectionOnGoing(sectionApiDao, true)
        astrntSDK.isContinueInterview = true
        return mAstronautApi.apiService.startSection((token)!!, map)
    }

    fun finishSection(sectionApiDao: SectionApiDao): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["candidate_id"] = interviewApiDao.candidate!!.id.toString()
        map["section_id"] = sectionApiDao.id.toString()
        val token = interviewApiDao.token
        if (!astrntSDK.isSelfPace) {
            updateElapsedTime(ElapsedTimeType.SECTION, sectionApiDao.id)
        }
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/section/stop)",
                        ("Finish Section, number " + (astrntSDK.sectionIndex + 1) +
                                ", sectionId = " + sectionApiDao.id)
                )
        )
        astrntSDK.saveLastApiCall("(/section/stop)")
        return mAstronautApi.apiService.stopSection((token)!!, map)
    }

    fun setTrySampleQuestion(): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val token = interviewApiDao.token
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["candidate_id"] = interviewApiDao.candidate!!.id.toString()
        if (astrntSDK.isSectionInterview) {
            val currentSection = astrntSDK.currentSection
            map["section_id"] = currentSection.id.toString()
            astrntSDK.updateSectionSampleQuestion(currentSection, 1)
        }
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/set/try-sample-question)",
                        "Sample Question"
                )
        )
        astrntSDK.saveLastApiCall("(/set/try-sample-question)")
        return mAstronautApi.apiService.setTrySampleQuestion((token)!!, map)
    }

    fun finishSession(question: QuestionApiDao): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val token = interviewApiDao.token
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["candidate_id"] = interviewApiDao.candidate!!.id.toString()
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/question/finish)",
                        ("Finish Question, number " + (astrntSDK.questionIndex + 1) +
                                ", questionId = " + question.id)
                )
        )
        astrntSDK.saveLastApiCall("(/question/finish)")
        return mAstronautApi.apiService.finishQuestion((token)!!, map)
    }

    fun finishInterview(): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        val token = interviewApiDao.token
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/interview/finish)",
                        "Finish Interview"
                )
        )
        astrntSDK.saveLastApiCall("(/interview/finish)")
        astrntSDK.isFinishInterview = true
        return mAstronautApi.apiService.finishInterview((token)!!, map)
    }

    private fun updateElapsedTime(@ElapsedTime type: String, refId: Long) {
        val interviewApiDao = astrntSDK.currentInterview
        val interviewCode = astrntSDK.interviewCode
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["type"] = type
        map["ref_id"] = refId.toString()
        val token = interviewApiDao.token
        addNewLog(interviewCode,
                LogDao("Hit API (/interview/update/elapsedTime)",
                        ("Update Elapsed Time Section, type = " + type +
                                ", number " + (astrntSDK.sectionIndex + 1)
                                + ", refId = " + refId)
                )
        )
        astrntSDK.saveLastApiCall("(/interview/update/elapsedTime)")
        mAstronautApi.apiService.updateElapsedTime((token)!!, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : MyObserver<BaseApiDao>() {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onApiResultCompleted() {}
                    override fun onApiResultError(title: String?, message: String?, code: String?) {
                        Timber.e(message)
                        if (message != null && message.toLowerCase().contains("unable to resolve host")) {
                            addNewLog(interviewCode,
                                    LogDao("Hit API (Elapsed Time Section)",
                                            "Failed, No Internet Connection"
                                    )
                            )
                        } else {
                            addNewLog(interviewCode,
                                    LogDao("Hit API (Elapsed Time Section)",
                                            "Error : $message"
                                    )
                            )
                        }
                        astrntSDK.saveLastApiCall("(Elapsed Time Section)")
                    }

                    override fun onApiResultOk(baseApiDao: BaseApiDao) {
                        Timber.d(baseApiDao.message)
                    }
                })
    }

    fun cvStatus(): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val map = HashMap<String, String?>()
        map["job_id"] = interviewApiDao.job!!.id.toString()
        map["company_id"] = interviewApiDao.company!!.id.toString()
        map["candidate_id"] = interviewApiDao.candidate!!.id.toString()
        map["interview_code"] = interviewApiDao.interviewCode
        val token = interviewApiDao.token
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/cv/status)",
                        "CV Status"
                )
        )
        astrntSDK.saveLastApiCall("(/cv/status)")
        return mAstronautApi.apiService.cvStatus((token)!!, map)
    }

    fun cvStart(): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val map = HashMap<String, String?>()
        map["job_id"] = interviewApiDao.job!!.id.toString()
        map["company_id"] = interviewApiDao.company!!.id.toString()
        map["candidate_id"] = interviewApiDao.candidate!!.id.toString()
        map["interview_code"] = interviewApiDao.interviewCode
        val token = interviewApiDao.token
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/cv/start)",
                        "CV Start"
                )
        )
        astrntSDK.saveLastApiCall("(/cv/start)")
        astrntSDK.saveCvStartCalled(true)
        return mAstronautApi.apiService.cvStart((token)!!, map)
    }

    fun pingNetwork(): Observable<BaseApiDao> {
        return mAstronautApi.apiService.pingNetwork("")
    }

    fun summary(): Observable<SummaryApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        val token = interviewApiDao.token
        return mAstronautApi.apiService.summary((token)!!, map)
    }

    fun gdprComplied(interviewCode: String): Observable<BaseApiDao> {
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewCode
        addNewLog(interviewCode,
                LogDao("Hit API (/user/gdpr_complied)",
                        "GDPR Complied"
                )
        )
        astrntSDK.saveLastApiCall("(/user/gdpr_complied)")
        return mAstronautApi.apiService.gdprComplied("", map)
    }
}
