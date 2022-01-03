package co.astrnt.qasdk.repository

import co.astrnt.qasdk.core.AstronautApi
import co.astrnt.qasdk.core.MyObserver
import co.astrnt.qasdk.dao.*
import co.astrnt.qasdk.type.ElapsedTime
import co.astrnt.qasdk.type.ElapsedTimeType
import co.astrnt.qasdk.utils.LogUtil.addNewLog
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

class SectionRepository(astronautApi: AstronautApi) : BaseRepository(astronautApi) {
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

    fun summarySection(): Observable<SummarySectionApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        val token = interviewApiDao.token
        return mAstronautApi.apiService.summarySection((token)!!, map)
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
                        addNewLog(interviewCode,
                                LogDao("Hit API (Elapsed Time Section)",
                                        "Error $message"
                                )
                        )
                        astrntSDK.saveLastApiCall("(Elapsed Time Section)")
                    }

                    override fun onApiResultOk(t: BaseApiDao) {
                        Timber.d(t.message)
                    }
                })
    }

    fun addLastSeen(questionId: Int): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val token = interviewApiDao.token
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["question_id"] = questionId.toString()
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/question/last_seen)",
                        ("Add Last Seen, number " + (astrntSDK.questionIndex + 1) +
                                ", questionId = " + questionId)
                )
        )
        astrntSDK.saveLastApiCall("(/question/last_seen)")
        return mAstronautApi.apiService.addLastSeen((token)!!, map)
    }
}