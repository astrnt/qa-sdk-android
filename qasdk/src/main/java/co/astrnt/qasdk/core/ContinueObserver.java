package co.astrnt.qasdk.core;

import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.utils.LogUtil;

import static co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW_PROFILE;
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
            case CLOSE_SECTION:
            case CLOSE_INTERVIEW_PROFILE:
            case CLOSE_TEST:
                if (currentInterview.getInterviewCode().equals(resultApiDao.getInterview_code())) {
                    currentInterview = astrntSDK.updateInterviewData(currentInterview, resultApiDao.getInterview());
                    astrntSDK.saveInterviewResult(resultApiDao, currentInterview, isContinue);
                } else {
                    astrntSDK.saveInterviewResult(resultApiDao, resultApiDao.getInterview(), isContinue);
                }

                InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
                if (interviewApiDao != null && interviewApiDao.getInterviewCode() != null) {
                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                            new LogDao("Response API",
                                    "Success, move to Continue Interview"
                            )
                    );
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
                            new LogDao("Continue Response API",
                                    "Error : " + resultApiDao.getMessage()
                            )
                    );
                }
                break;
        }
    }

    public abstract void onContinueInterview();

}