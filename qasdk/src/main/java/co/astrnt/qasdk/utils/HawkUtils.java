package co.astrnt.qasdk.utils;

import com.orhanobut.hawk.Hawk;

import co.astrnt.qasdk.dao.GdprDao;
import co.astrnt.qasdk.dao.WelcomeVideoDao;

import static co.astrnt.qasdk.constants.PreferenceKey.KEY_CONTINUE;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_CV_START_CALLED;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_DOWNLOAD_ID;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_FINISH_INTERVIEW;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_FINISH_QUESTION;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_FINISH_SESSION;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_FIRST_OPEN;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_GDPR;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_INTERVIEW_CODE;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_IS_LAST_QUESTION;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_IS_PROFILE;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_IS_RUNNING_COMPRESSING;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_IS_RUNNING_UPLOADING;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_IS_SOURCING;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_LAST_API_CALL;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_LAST_TIMER;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_NEED_REGISTER;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_PRACTICE_RETAKE;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_SELF_PACE;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_SHOW_RATING;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_SHOW_UPLOAD;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_START_SESSION;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_UNAUTHORIZED;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_UPLOAD_ID;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_WATCH_WELCOME_VIDEO;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_WELCOME_VIDEO;
import static co.astrnt.qasdk.constants.PreferenceKey.KEY_WELCOME_VIDEO_URI;

public class HawkUtils {

    public boolean isContinueInterview() {
        return Hawk.get(KEY_CONTINUE, false);
    }

    public void setContinueInterview(boolean isContinue) {
        Hawk.put(KEY_CONTINUE, isContinue);
    }

    public boolean isShowUpload() {
        return Hawk.get(KEY_SHOW_UPLOAD, false);
    }

    public void setShowUpload(boolean showUpload) {
        Hawk.put(KEY_SHOW_UPLOAD, showUpload);
    }

    public boolean isFinishSession() {
        return Hawk.get(KEY_FINISH_SESSION, false);
    }

    public void setFinishSession(boolean finishSession) {
        Hawk.put(KEY_FINISH_SESSION, finishSession);
    }

    public boolean isStartSessionTimer() {
        return Hawk.get(KEY_START_SESSION, false);
    }

    public void setStartSessionTimer(boolean finishSession) {
        Hawk.put(KEY_START_SESSION, finishSession);
    }

    public boolean isFinishQuestionSession() {
        return Hawk.get(KEY_FINISH_QUESTION, false);
    }

    public void setFinishQuestionSession(boolean finishQuestionSession) {
        Hawk.put(KEY_FINISH_QUESTION, finishQuestionSession);
    }



    public boolean isFinishInterview() {
        return Hawk.get(KEY_FINISH_INTERVIEW, true);
    }

    public void setFinishInterview(boolean isFinish) {
        Hawk.put(KEY_FINISH_INTERVIEW, isFinish);
    }

    public boolean isGdprComplied() {
        GdprDao gdprDao = Hawk.get(KEY_GDPR);
        return gdprDao.isGdprComplied();
    }

    public GdprDao getGdprDao() {
        return Hawk.get(KEY_GDPR);
    }

    protected void saveGdprDao(GdprDao gdprDao) {
        Hawk.put(KEY_GDPR, gdprDao);
    }

    public String getUploadId() {
        return Hawk.get(KEY_UPLOAD_ID);
    }

    public void saveUploadId(String uploadId) {
        Hawk.put(KEY_UPLOAD_ID, uploadId);
    }

    public void removeUploadId() {
        Hawk.delete(KEY_UPLOAD_ID);
    }

    public WelcomeVideoDao getWelcomeVideoDao() {
        return Hawk.get(KEY_WELCOME_VIDEO);
    }

    protected void saveWelcomeVideoDao(WelcomeVideoDao welcomeVideoDao) {
        Hawk.put(KEY_WELCOME_VIDEO, welcomeVideoDao);
    }

    public boolean isFinishWatchWelcomeVideo() {
        return Hawk.get(KEY_WATCH_WELCOME_VIDEO, false);
    }

    public void saveFinishWatchWelcomeVideo(boolean finished) {
        Hawk.put(KEY_WATCH_WELCOME_VIDEO, finished);
    }

    public String getWelcomeVideoUri() {
        return Hawk.get(KEY_WELCOME_VIDEO_URI, "");
    }

    public void saveWelcomeVideoUri(String videoUri) {
        Hawk.put(KEY_WELCOME_VIDEO_URI, videoUri);
    }

    public int getDownloadId() {
        return Hawk.get(KEY_DOWNLOAD_ID, 0);
    }

    public void saveDownloadId(int downloadId) {
        Hawk.put(KEY_DOWNLOAD_ID, downloadId);
    }

    public void removeDownloadId() {
        Hawk.delete(KEY_DOWNLOAD_ID);
    }

    public boolean isShowRating() {
        return Hawk.get(KEY_SHOW_RATING, false);
    }

    public void saveShowRating(boolean value) {
        Hawk.put(KEY_SHOW_RATING, value);
    }

    public boolean isFirstOpen() {
        return Hawk.get(KEY_FIRST_OPEN, true);
    }

    public void saveFirstOpen(boolean value) {
        Hawk.put(KEY_FIRST_OPEN, value);
    }

    public boolean isNeedRegister() {
        return Hawk.get(KEY_NEED_REGISTER, true);
    }

    public void saveNeedRegister(boolean value) {
        Hawk.put(KEY_NEED_REGISTER, value);
    }

    public boolean isSourcing() {
        return Hawk.get(KEY_IS_SOURCING, true);
    }

    protected void saveSourcing(boolean value) {
        Hawk.put(KEY_IS_SOURCING, value);
    }

    public boolean isUnauthorized() {
        return Hawk.get(KEY_UNAUTHORIZED, false);
    }

    public void saveUnauthorized(boolean value) {
        Hawk.put(KEY_UNAUTHORIZED, value);
    }

    public boolean isProfile() {
        return Hawk.get(KEY_IS_PROFILE, false);
    }

    public void saveIsProfile(boolean value) {
        Hawk.put(KEY_IS_PROFILE, value);
    }

    protected void saveInterviewCode(String interviewCode) {
        Hawk.put(KEY_INTERVIEW_CODE, interviewCode);
    }

    public String getInterviewCode() {
        return Hawk.get(KEY_INTERVIEW_CODE);
    }

    protected void saveLastTimeLeft(int lastTimer) {
        Hawk.put(KEY_LAST_TIMER, lastTimer);
    }

    public int getLastTimer() {
        return Hawk.get(KEY_LAST_TIMER, -1);
    }

    protected void saveSelfPace(boolean isSelfPace) {
        Hawk.put(KEY_SELF_PACE, isSelfPace);
    }

    public boolean isSelfPace() {
        return Hawk.get(KEY_SELF_PACE, false);
    }

    public void savePracticeRetake(boolean isPracticeRetake) {
        Hawk.put(KEY_PRACTICE_RETAKE, isPracticeRetake);
    }

    public boolean isPracticeRetake() {
        return Hawk.get(KEY_PRACTICE_RETAKE, false);
    }

    public void saveLastApiCall(String apiPath) {
        Hawk.put(KEY_LAST_API_CALL, apiPath);
    }

    public String getLastApiCall() {
        return Hawk.get(KEY_LAST_API_CALL);
    }

    public boolean isCvStartCalled() {
        return Hawk.get(KEY_CV_START_CALLED, false);
    }

    public void saveCvStartCalled(boolean finished) {
        Hawk.put(KEY_CV_START_CALLED, finished);
    }

    public boolean isRunningUploading() {
        return Hawk.get(KEY_IS_RUNNING_UPLOADING, false);
    }

    public void saveRunningUploading(boolean running) {
        Hawk.put(KEY_IS_RUNNING_UPLOADING, running);
    }

    public boolean isRunningCompressing() {
        return Hawk.get(KEY_IS_RUNNING_COMPRESSING, false);
    }

    public void saveRunningCompressing(boolean running) {
        Hawk.put(KEY_IS_RUNNING_COMPRESSING, running);
    }

    public boolean isLastQuestion() {
        return Hawk.get(KEY_IS_LAST_QUESTION, false);
    }

    public void saveIsLastQuestion(boolean isLast) {
        Hawk.put(KEY_IS_LAST_QUESTION, isLast);
    }


    protected void removeHawkSaved() {
        Hawk.delete(KEY_WATCH_WELCOME_VIDEO);
        Hawk.delete(KEY_WELCOME_VIDEO);
        Hawk.delete(KEY_WELCOME_VIDEO_URI);
        Hawk.delete(KEY_GDPR);
        Hawk.delete(KEY_CONTINUE);
        Hawk.delete(KEY_SHOW_UPLOAD);
        Hawk.delete(KEY_FINISH_INTERVIEW);
        Hawk.delete(KEY_UNAUTHORIZED);
        Hawk.delete(KEY_IS_PROFILE);
        Hawk.delete(KEY_LAST_TIMER);
        Hawk.delete(KEY_SELF_PACE);
        Hawk.delete(KEY_PRACTICE_RETAKE);
        Hawk.delete(KEY_LAST_API_CALL);
        Hawk.delete(KEY_CV_START_CALLED);
        Hawk.delete(KEY_IS_RUNNING_UPLOADING);
        Hawk.delete(KEY_IS_RUNNING_COMPRESSING);
        Hawk.delete(KEY_FINISH_SESSION);
        Hawk.delete(KEY_FINISH_QUESTION);
        Hawk.delete(KEY_START_SESSION);
        Hawk.delete(KEY_IS_LAST_QUESTION);
        removeDownloadId();
        removeUploadId();
    }

}
