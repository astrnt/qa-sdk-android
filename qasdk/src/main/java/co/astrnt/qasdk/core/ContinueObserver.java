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
        InterviewApiDao currentInterview = astrntSDK.getCurrentInterview();
        switch (resultApiDao.getInterview().getType()) {
            case CLOSE_INTERVIEW:
                if (currentInterview.getInterviewCode().equals(resultApiDao.getInterview_code())) {
                    astrntSDK.saveInterviewResult(resultApiDao, currentInterview);
                } else {
                    astrntSDK.saveInterviewResult(resultApiDao, resultApiDao.getInterview());
                }
                onContinueInterview();
                break;
            case CLOSE_TEST:
                astrntSDK.saveInterviewResult(resultApiDao, resultApiDao.getInterview());
                onContinueInterview();
                break;
            case CLOSE_SECTION:
//                if (currentInterview.getInterviewCode().equals(resultApiDao.getInterview_code())) {
//                    astrntSDK.saveInterviewResult(resultApiDao, currentInterview);
//                } else {
                astrntSDK.saveInterviewResult(resultApiDao, resultApiDao.getInterview());
//                }
                onContinueInterview();
                break;
            default:
                onApiResultError(resultApiDao.getMessage(), "error");
                break;
        }
    }

    public abstract void onContinueInterview();

}