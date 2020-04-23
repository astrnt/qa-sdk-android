package co.astrnt.qasdk.utils;

import com.orhanobut.hawk.Hawk;

import co.astrnt.qasdk.constatnts.PreferenceKey;
import co.astrnt.qasdk.dao.GdprDao;
import co.astrnt.qasdk.dao.WelcomeVideoDao;

public class HawkUtils {

    public boolean isContinueInterview() {
        return Hawk.get(PreferenceKey.KEY_CONTINUE, false);
    }

    public void setContinueInterview(boolean isContinue) {
        Hawk.put(PreferenceKey.KEY_CONTINUE, isContinue);
    }

    public boolean isShowUpload() {
        return Hawk.get(PreferenceKey.KEY_SHOW_UPLOAD, false);
    }

    public void setShowUpload(boolean showUpload) {
        Hawk.put(PreferenceKey.KEY_SHOW_UPLOAD, showUpload);
    }

    public boolean isFinishInterview() {
        return Hawk.get(PreferenceKey.KEY_FINISH_INTERVIEW, true);
    }

    public void setFinishInterview(boolean isFinish) {
        Hawk.put(PreferenceKey.KEY_FINISH_INTERVIEW, isFinish);
    }

    public boolean isGdprComplied() {
        GdprDao gdprDao = Hawk.get(PreferenceKey.KEY_GDPR);
        return gdprDao.isGdprComplied();
    }

    public GdprDao getGdprDao() {
        return Hawk.get(PreferenceKey.KEY_GDPR);
    }

    protected void saveGdprDao(GdprDao gdprDao) {
        Hawk.put(PreferenceKey.KEY_GDPR, gdprDao);
    }

    public String getUploadId() {
        return Hawk.get(PreferenceKey.KEY_UPLOAD_ID);
    }

    public void saveUploadId(String uploadId) {
        Hawk.put(PreferenceKey.KEY_UPLOAD_ID, uploadId);
    }

    public void removeUploadId() {
        Hawk.delete(PreferenceKey.KEY_UPLOAD_ID);
    }

    public WelcomeVideoDao getWelcomeVideoDao() {
        return Hawk.get(PreferenceKey.KEY_WELCOME_VIDEO);
    }

    protected void saveWelcomeVideoDao(WelcomeVideoDao welcomeVideoDao) {
        Hawk.put(PreferenceKey.KEY_WELCOME_VIDEO, welcomeVideoDao);
    }

    public boolean isFinishWatchWelcomeVideo() {
        return Hawk.get(PreferenceKey.KEY_WATCH_WELCOME_VIDEO, false);
    }

    public void saveFinishWatchWelcomeVideo(boolean finished) {
        Hawk.put(PreferenceKey.KEY_WATCH_WELCOME_VIDEO, finished);
    }

    public String getWelcomeVideoUri() {
        return Hawk.get(PreferenceKey.KEY_WELCOME_VIDEO_URI, "");
    }

    public void saveWelcomeVideoUri(String videoUri) {
        Hawk.put(PreferenceKey.KEY_WELCOME_VIDEO_URI, videoUri);
    }

    public int getDownloadId() {
        return Hawk.get(PreferenceKey.KEY_DOWNLOAD_ID, 0);
    }

    public void saveDownloadId(int downloadId) {
        Hawk.put(PreferenceKey.KEY_DOWNLOAD_ID, downloadId);
    }

    public void removeDownloadId() {
        Hawk.delete(PreferenceKey.KEY_DOWNLOAD_ID);
    }

    public boolean isShowRating() {
        return Hawk.get(PreferenceKey.KEY_SHOW_RATING, false);
    }

    public void saveShowRating(boolean value) {
        Hawk.put(PreferenceKey.KEY_SHOW_RATING, value);
    }

    public boolean isFirstOpen() {
        return Hawk.get(PreferenceKey.KEY_FIRST_OPEN, true);
    }

    public void saveFirstOpen(boolean value) {
        Hawk.put(PreferenceKey.KEY_FIRST_OPEN, value);
    }

    public boolean isNeedRegister() {
        return Hawk.get(PreferenceKey.KEY_NEED_REGISTER, true);
    }

    public void saveNeedRegister(boolean value) {
        Hawk.put(PreferenceKey.KEY_NEED_REGISTER, value);
    }

    public boolean isSourcing() {
        return Hawk.get(PreferenceKey.KEY_IS_SOURCING, true);
    }

    protected void saveSourcing(boolean value) {
        Hawk.put(PreferenceKey.KEY_IS_SOURCING, value);
    }

    public boolean isUnauthorized() {
        return Hawk.get(PreferenceKey.KEY_UNAUTHORIZED, false);
    }

    public void saveUnauthorized(boolean value) {
        Hawk.put(PreferenceKey.KEY_UNAUTHORIZED, value);
    }

    public boolean isProfile() {
        return Hawk.get(PreferenceKey.KEY_IS_PROFILE, false);
    }

    public void saveIsProfile(boolean value) {
        Hawk.put(PreferenceKey.KEY_IS_PROFILE, value);
    }

    protected void saveInterviewCode(String interviewCode) {
        Hawk.put(PreferenceKey.KEY_INTERVIEW_CODE, interviewCode);
    }

    public String getInterviewCode() {
        return Hawk.get(PreferenceKey.KEY_INTERVIEW_CODE);
    }

    protected void removeHawkSaved() {
        Hawk.delete(PreferenceKey.KEY_WATCH_WELCOME_VIDEO);
        Hawk.delete(PreferenceKey.KEY_WELCOME_VIDEO);
        Hawk.delete(PreferenceKey.KEY_WELCOME_VIDEO_URI);
        Hawk.delete(PreferenceKey.KEY_GDPR);
        Hawk.delete(PreferenceKey.KEY_CONTINUE);
        Hawk.delete(PreferenceKey.KEY_SHOW_UPLOAD);
        Hawk.delete(PreferenceKey.KEY_FINISH_INTERVIEW);
        Hawk.delete(PreferenceKey.KEY_UNAUTHORIZED);
        removeDownloadId();
        removeUploadId();
    }

}
