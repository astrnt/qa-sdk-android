package co.astrnt.qasdk.repository

import android.R.id
import co.astrnt.qasdk.core.AstronautApi
import co.astrnt.qasdk.core.MyObserver
import co.astrnt.qasdk.dao.BaseApiDao
import co.astrnt.qasdk.dao.LogDao
import co.astrnt.qasdk.dao.QuestionApiDao
import co.astrnt.qasdk.type.ElapsedTimeType
import co.astrnt.qasdk.type.InterviewType
import co.astrnt.qasdk.type.TestType
import co.astrnt.qasdk.utils.LogUtil.addNewLog
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*


class QuestionRepository(astronautApi: AstronautApi) : BaseRepository(astronautApi) {
    fun addQuestionAttempt(question: QuestionApiDao): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val token = interviewApiDao.token
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["candidate_id"] = interviewApiDao.candidate!!.id.toString()
        map["question_id"] = question.id.toString()
        if (astrntSDK.isSectionInterview) {
            val currentSection = astrntSDK.currentSection
            map["section_id"] = currentSection.id.toString()
        }
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/question/attempt)",
                        "Add Question Attempt, number " + (astrntSDK.questionIndex + 1) +
                                ", questionId = " + question.id
                )
        )
        astrntSDK.saveLastApiCall("(/question/attempt)")
        return mAstronautApi.apiService.addAttempt((token)!!, map)
    }

    fun addMediaAttempt(question: QuestionApiDao): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val token = interviewApiDao.token
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["candidate_id"] = interviewApiDao.candidate!!.id.toString()
        map["question_id"] = question.id.toString()
        if (astrntSDK.isSectionInterview) {
            val currentSection = astrntSDK.currentSection
            map["section_id"] = currentSection.id.toString()
        }
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/media/attempt)",
                        ("Add Media Attempt, number " + (astrntSDK.questionIndex + 1) +
                                ", questionId = " + question.id)
                )
        )
        astrntSDK.saveLastApiCall("(/media/attempt)")
        return mAstronautApi.apiService.addMediaAttempt((token)!!, map)
    }

    fun finishQuestion(question: QuestionApiDao): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val token = interviewApiDao.token
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["candidate_id"] = interviewApiDao.candidate!!.id.toString()
        val subQuestions = question.sub_questions
        if (subQuestions != null && !subQuestions.isEmpty()) {
            for (i in subQuestions.indices) {
                val subQuestion = subQuestions[i]
                assert(subQuestion != null)
                map["question_ids[$i]"] = subQuestion!!.id.toString()
            }
        } else {
            map["question_id"] = question.id.toString()
        }
        if (astrntSDK.isSectionInterview) {
            val currentSection = astrntSDK.currentSection
            map["section_id"] = currentSection.id.toString()
        }
        if ((interviewApiDao.type == InterviewType.CLOSE_TEST)) {
            updateElapsedTime(question.id)
        }
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/question/finish)",
                        ("Finish Question, number " + (astrntSDK.questionIndex + 1) +
                                ", questionId = " + question.id)
                )
        )
        astrntSDK.saveLastApiCall("(/question/finish)")
        return mAstronautApi.apiService.finishQuestion((token)!!, map)
    }


    fun answerQuestion(question: QuestionApiDao, subQuestion: QuestionApiDao?): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val token = interviewApiDao.token
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["candidate_id"] = interviewApiDao.candidate!!.id.toString()
        map["invite_id"] = interviewApiDao.invite_id.toString()
        if (subQuestion != null) {
            map["question_id"] = subQuestion.id.toString()
            if ((subQuestion.type_child == TestType.FREE_TEXT)) {
                map["type"] = "1"
                var currentAnswer = subQuestion.answer
                if (currentAnswer == null) {
                    currentAnswer = ""
                }
                map["text_answer"] = currentAnswer
            } else {
                map["type"] = "0"
                val selectedAnswer = subQuestion.selectedAnswer
                if (selectedAnswer != null) {
                    for (i in selectedAnswer.indices) {
                        val answerItem = selectedAnswer[i]
                        assert(answerItem != null)
                        map["answer_ids[$i]"] = answerItem!!.id.toString()
                    }
                }
            }
        } else {
            map["question_id"] = question.id.toString()
            if ((question.type_child == TestType.FREE_TEXT)) {
                map["type"] = "1"
                var currentAnswer = question.answer
                if (currentAnswer == null) {
                    currentAnswer = ""
                }
                map["text_answer"] = currentAnswer
            } else {
                map["type"] = "0"
                val selectedAnswer = question.selectedAnswer
                if (selectedAnswer != null) {
                    for (i in selectedAnswer.indices) {
                        val answerItem = selectedAnswer[i]
                        assert(answerItem != null)
                        map["answer_ids[$i]"] = answerItem!!.id.toString()
                    }
                }
            }
        }
        if (question.sub_questions != null && !question.sub_questions!!.isEmpty()) {
            map["group_question"] = "true"
        } else {
            map["group_question"] = "false"
        }
        if (astrntSDK.isSectionInterview) {
            map["interview_type"] = "section"
        } else {
            map["interview_type"] = "test"
        }
        if (astrntSDK.isSectionInterview) {
            addNewLog(interviewApiDao.interviewCode,
                    LogDao("Hit API (/question/answer)",
                            ("Answer Question " + (astrntSDK.questionIndex + 1) +
                                    ", questionId = " + question.id +
                                    ", sectionId = " + astrntSDK.currentSection.id +
                                    " duration left = " + astrntSDK.currentSection.duration +
                                    " seconds")
                    )
            )
        } else {
            addNewLog(interviewApiDao.interviewCode,
                    LogDao("Hit API (/question/answer)",
                            ("Answer Question " + (astrntSDK.questionIndex + 1) +
                                    ", questionId = " + question.id)
                    )
            )
        }
        astrntSDK.saveLastApiCall("(/question/answer)")
        return mAstronautApi.apiService.answerQuestion((token)!!, map)
    }

    private fun updateElapsedTime(refId: Long) {
        val interviewApiDao = astrntSDK.currentInterview
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["type"] = ElapsedTimeType.TEST
        map["ref_id"] = refId.toString()
        val token = interviewApiDao.token
        val interviewCode = astrntSDK.interviewCode
        addNewLog(interviewCode,
                LogDao("Hit API (/interview/update/elapsedTime)",
                        ("Update Elapsed Time, type =  " + ElapsedTimeType.TEST +
                                ", number " + (astrntSDK.questionIndex + 1)
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
                                    LogDao("Hit API (Elapsed Time)",
                                            "Failed, No Internet Connection"
                                    )
                            )
                        } else {
                            addNewLog(interviewCode,
                                    LogDao("Hit API (Elapsed Time)",
                                            "Error $message"
                                    )
                            )
                        }
                        astrntSDK.saveLastApiCall("(Elapsed Time)")
                    }

                    override fun onApiResultOk(t: BaseApiDao) {
                        Timber.d(t.message)
                    }
                })
    }

    fun addLastSeen(question: QuestionApiDao): Observable<BaseApiDao> {
        val interviewApiDao = astrntSDK.currentInterview
        val token = interviewApiDao.token
        val map = HashMap<String, String?>()
        map["interview_code"] = interviewApiDao.interviewCode
        map["question_id"] = question.id.toString()
        addNewLog(interviewApiDao.interviewCode,
                LogDao("Hit API (/question/last_seen)",
                        ("Add Last Seen, number " + (astrntSDK.questionIndex + 1) +
                                ", questionId = " + question.id)
                )
        )
        astrntSDK.saveLastApiCall("(/question/last_seen)")
        return mAstronautApi.apiService.addLastSeen((token)!!, map)
    }
}