package co.astrnt.qasdk.utils;

import android.content.Context;
import android.os.Build;

import net.gotev.uploadservice.MultipartUploadRequest;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.QuestionApiDao;

public class FileUploadHelper {

    public static MultipartUploadRequest uploadVideo(Context context, InterviewApiDao interviewApiDao, QuestionApiDao currentQuestion, String url) throws MalformedURLException, FileNotFoundException {

        String token = interviewApiDao.getToken();
        String interviewCode = interviewApiDao.getInterviewCode();
        String candidateId = interviewApiDao.getCandidate().getId() + "";
        String companyId = interviewApiDao.getCompany().getId() + "";
        String questionId = currentQuestion.getId() + "";
        String jobId = interviewApiDao.getJob().getId() + "";
        String filePath = currentQuestion.getVideoPath();

        LogUtil.addNewLog(interviewCode,
                new LogDao("Video Upload " + questionId,
                        "Video path : " + filePath
                )
        );

        return new MultipartUploadRequest(context, url)
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
                .setMaxRetries(3);
    }
}
