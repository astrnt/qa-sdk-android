package co.astrnt.qasdk.core;

import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.utils.LogUtil;

import static co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_SECTION;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_TEST;

/**
 * Created by deni rohimat on 23/05/18.
 */
public abstract class ContinueObserver extends MyObserver<InterviewResultApiDao> {

    @Override
    public void onApiResultOk(InterviewResultApiDao resultApiDao) {
        InterviewApiDao currentInterview = astrntSDK.getCurrentInterview();
        boolean isContinue = astrntSDK.isContinueInterview();
        switch (resultApiDao.getInterview().getType()) {
            case CLOSE_INTERVIEW:
                if (currentInterview.getInterviewCode().equals(resultApiDao.getInterview_code())) {
                    currentInterview = astrntSDK.updateQuestionData(currentInterview, resultApiDao.getInterview());
                    astrntSDK.saveInterviewResult(resultApiDao, currentInterview, isContinue);
                } else {
                    astrntSDK.saveInterviewResult(resultApiDao, resultApiDao.getInterview(), isContinue);
                }
                onContinueInterview();
                break;
            case CLOSE_TEST:
                astrntSDK.saveInterviewResult(resultApiDao, resultApiDao.getInterview(), isContinue);
                onContinueInterview();
                break;
            case CLOSE_SECTION:
                if (currentInterview.getInterviewCode().equals(resultApiDao.getInterview_code())) {
                    currentInterview = astrntSDK.updateQuestionData(currentInterview, resultApiDao.getInterview());
                    astrntSDK.saveInterviewResult(resultApiDao, currentInterview, isContinue);
                } else {
                    astrntSDK.saveInterviewResult(resultApiDao, resultApiDao.getInterview(), isContinue);
                }
                onContinueInterview();
                break;
            default:
                if (resultApiDao.getTitle() != null) {
                    onApiResultError(resultApiDao.getTitle(), resultApiDao.getMessage(), "error");
                } else {
                    onApiResultError("", resultApiDao.getMessage(), "error");
                }
                if (currentInterview != null && currentInterview.getInterviewCode() != null) {
                    LogUtil.addNewLog(currentInterview.getInterviewCode(),
                            new LogDao("Continue",
                                    "Hit API Error " + resultApiDao.getMessage()
                            )
                    );
                }
                break;
        }
    }

    public abstract void onContinueInterview();

}