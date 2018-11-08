package co.astrnt.qasdk;

import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;

public class VideoSDK {

    private AstrntSDK astrntSDK;

    public VideoSDK() {
        astrntSDK = new AstrntSDK();
    }

    public InterviewApiDao getCurrentInterview() {
        return astrntSDK.getCurrentInterview();
    }

    public QuestionApiDao getCurrentQuestion() {
        return astrntSDK.getCurrentQuestion();
    }

    public int getQuestionAttempt() {
        return astrntSDK.getQuestionAttempt();
    }

    public int getQuestionIndex() {
        return astrntSDK.getQuestionIndex();
    }

    public int getTotalQuestion() {
        return astrntSDK.getTotalQuestion();
    }

    public void decreaseQuestionAttempt() {
        astrntSDK.decreaseQuestionAttempt();
    }

    public void increaseQuestionIndex() {
        astrntSDK.increaseQuestionIndex();
    }

    public void markAsPending(QuestionApiDao questionApiDao, String rawFilePath) {
        astrntSDK.markAsPending(questionApiDao, rawFilePath);
    }

    public boolean isNotLastQuestion() {
        return astrntSDK.isNotLastQuestion();
    }

    public boolean isLastQuestion() {
        return !astrntSDK.isNotLastQuestion();
    }

    public boolean isLastAttempt() {
        return astrntSDK.isLastAttempt();
    }

    public long getAvailableMemory() {
        return astrntSDK.getAvailableStorage();
    }

    public void markNotAnswer(QuestionApiDao questionApiDao) {
        astrntSDK.markNotAnswer(questionApiDao);
    }

    public void clearDb() {
        astrntSDK.clearDb();
    }

}
