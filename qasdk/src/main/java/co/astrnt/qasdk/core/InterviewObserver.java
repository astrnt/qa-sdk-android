package co.astrnt.qasdk.core;

import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;

/**
 * Created by deni rohimat on 09/04/18.
 */
public abstract class InterviewObserver extends MyObserver<InterviewResultApiDao> {

    @Override
    public void onApiResultCompleted() {

    }

    @Override
    public void onApiResultOk(InterviewResultApiDao resultApiDao) {
        if (resultApiDao.getInterview().getType().contains("open")) {
            onNeedToRegister(resultApiDao.getInterview());
        } else {
            switch (resultApiDao.getInterview().getType()) {
                case "close interview":
                    onInterviewType(resultApiDao.getInterview());
                    break;
                case "close test":
                    onTestType(resultApiDao.getInterview());
                    break;
                case "close section":
                    onSectionType(resultApiDao.getInterview());
                    break;
                default:
                    onApiResultError(resultApiDao.getMessage(), "error");
                    break;
            }
        }
    }

    public abstract void onNeedToRegister(InterviewApiDao interview);

    public abstract void onInterviewType(InterviewApiDao interview);

    public abstract void onTestType(InterviewApiDao interview);

    public abstract void onSectionType(InterviewApiDao interview);

}