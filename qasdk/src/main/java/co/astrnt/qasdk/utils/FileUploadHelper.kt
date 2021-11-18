package co.astrnt.qasdk.utils

import android.content.Context
import android.os.Build
import co.astrnt.qasdk.dao.InterviewApiDao
import co.astrnt.qasdk.dao.LogDao
import co.astrnt.qasdk.dao.QuestionApiDao
import net.gotev.uploadservice.MultipartUploadRequest
import java.io.FileNotFoundException
import java.net.MalformedURLException

object FileUploadHelper {
    @JvmStatic
    @Throws(MalformedURLException::class, FileNotFoundException::class)
    fun uploadVideo(context: Context?, interviewApiDao: InterviewApiDao, currentQuestion: QuestionApiDao, url: String?, source: String): MultipartUploadRequest {
        val token = interviewApiDao.token
        val interviewCode = interviewApiDao.interviewCode
        val candidateId = interviewApiDao.candidate?.id.toString() + ""
        val companyId = interviewApiDao.company?.id.toString() + ""
        val questionId = currentQuestion.id.toString() + ""
        val jobId = interviewApiDao.job?.id.toString() + ""
        val filePath = currentQuestion.videoPath
        LogUtil.addNewLog(interviewCode,
                LogDao("Video Upload $questionId",
                        "Video path : " + filePath
                                + source
                )
        )
        return MultipartUploadRequest(context, url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .addHeader("token", token)
                .addParameter("interview_code", interviewCode)
                .addParameter("candidate_id", candidateId)
                .addParameter("company_id", companyId)
                .addParameter("question_id", questionId)
                .addParameter("job_id", jobId)
                .addParameter("device", "android")
                .addParameter("device_type", Build.MODEL)
                .addFileToUpload(filePath, "interview_video")
                .setUtf8Charset()
                .setAutoDeleteFilesAfterSuccessfulUpload(false)
                .setMaxRetries(3)
    }
}