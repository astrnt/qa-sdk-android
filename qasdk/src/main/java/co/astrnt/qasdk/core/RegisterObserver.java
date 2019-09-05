package co.astrnt.qasdk.core;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;

import static co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_SECTION;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_TEST;

/**
 * Created by deni rohimat on 12/04/18.
 */
public abstract class RegisterObserver extends MyObserver<InterviewResultApiDao> {

    @Override
    public void onApiResultOk(InterviewResultApiDao resultApiDao) {
        astrntSDK.clearDb();

        astrntSDK = new AstrntSDK();

        astrntSDK.saveInterviewResult(resultApiDao, resultApiDao.getInterview(), false);

        switch (resultApiDao.getInterview().getType()) {
            case CLOSE_INTERVIEW:
                onInterviewType(resultApiDao.getInterview());
                break;
            case CLOSE_TEST:
                onTestType(resultApiDao.getInterview());
                break;
            case CLOSE_SECTION:
                onSectionType(resultApiDao.getInterview());
                break;
            default:
                onApiResultError(resultApiDao.getTitle(), resultApiDao.getMessage(), "error");
                break;
        }
    }

    public abstract void onInterviewType(InterviewApiDao interview);

    public abstract void onTestType(InterviewApiDao interview);

    public abstract void onSectionType(InterviewApiDao interview);

}