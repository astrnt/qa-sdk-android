package co.astrnt.qasdk.core;

import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;

import static co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW_PROFILE;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_SECTION;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_TEST;
import static co.astrnt.qasdk.type.InterviewType.OPEN;

/**
 * Created by deni rohimat on 09/04/18.
 */
public abstract class InterviewObserver extends MyObserver<InterviewResultApiDao> {

    @Override
    public void onApiResultOk(InterviewResultApiDao resultApiDao) {
        InterviewApiDao data = resultApiDao.getInterview();
        if (data == null) {
            onApiResultError("", "Code not found or interview already finished", "error");
        } else {
            astrntSDK.saveInterviewResult(resultApiDao, data, false);

            if (resultApiDao.getInterview().getType().contains(OPEN)) {
                onNeedToRegister(data);
            } else {
                astrntSDK.saveInterview(data, data.getToken(), data.getInterviewCode());
                switch (data.getType()) {
                    case CLOSE_INTERVIEW:
                    case CLOSE_INTERVIEW_PROFILE:
                        onInterviewType(data);
                        break;
                    case CLOSE_TEST:
                        onTestType(data);
                        break;
                    case CLOSE_SECTION:
                        onSectionType(data);
                        break;
                    default:
                        if (resultApiDao.getTitle() != null) {
                            onApiResultError(resultApiDao.getTitle(), resultApiDao.getMessage(), "error");
                        } else {
                            onApiResultError("", resultApiDao.getMessage(), "error");
                        }
                        break;
                }
            }
        }
    }

    public abstract void onNeedToRegister(InterviewApiDao interview);

    public abstract void onInterviewType(InterviewApiDao interview);

    public abstract void onTestType(InterviewApiDao interview);

    public abstract void onSectionType(InterviewApiDao interview);

}