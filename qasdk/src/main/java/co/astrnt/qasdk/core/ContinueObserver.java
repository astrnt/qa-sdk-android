package co.astrnt.qasdk.core;

import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;

import static co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_SECTION;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_TEST;

/**
 * Created by deni rohimat on 23/05/18.
 */
public abstract class ContinueObserver extends MyObserver<InterviewResultApiDao> {

    @Override
    public void onApiResultOk(InterviewResultApiDao resultApiDao) {
        astrntSDK.updateInterviewTimeLeft(resultApiDao.getInterview().getDuration_left());
        InterviewApiDao currentInterview = astrntSDK.getCurrentInterview();
        astrntSDK.saveInterviewResult(resultApiDao, currentInterview);
        switch (resultApiDao.getInterview().getType()) {
            case CLOSE_INTERVIEW:
                onContinueInterview();
                break;
            case CLOSE_TEST:
                onContinueInterview();
                break;
            case CLOSE_SECTION:
                onContinueInterview();
                break;
            default:
                onApiResultError(resultApiDao.getMessage(), "error");
                break;
        }
    }

    public abstract void onContinueInterview();

}