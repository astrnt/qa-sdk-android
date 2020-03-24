package co.astrnt.qasdk.core;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.utils.LogUtil;

import static co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_INTERVIEW_PROFILE;
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

        InterviewApiDao data = resultApiDao.getInterview();
        astrntSDK.saveInterviewResult(resultApiDao, data, false);

        switch (data.getType()) {
            case CLOSE_INTERVIEW:
            case CLOSE_INTERVIEW_PROFILE:
                if (data.getInterviewCode() != null) {
                    LogUtil.addNewLog(data.getInterviewCode(),
                            new LogDao("Response API",
                                    "Success, will move to Video Interview"
                            )
                    );
                }
                onInterviewType(data);
                break;
            case CLOSE_TEST:
                if (data.getInterviewCode() != null) {
                    LogUtil.addNewLog(data.getInterviewCode(),
                            new LogDao("Response API",
                                    "Success, will move to MCQ Interview"
                            )
                    );
                }
                onTestType(data);
                break;
            case CLOSE_SECTION:
                if (data.getInterviewCode() != null) {
                    LogUtil.addNewLog(data.getInterviewCode(),
                            new LogDao("Response API",
                                    "Success, will move to Section Interview"
                            )
                    );
                }
                onSectionType(data);
                break;
            default:
                if (resultApiDao.getTitle() != null) {
                    onApiResultError(resultApiDao.getTitle(), resultApiDao.getMessage(), "error");
                } else {
                    onApiResultError("", resultApiDao.getMessage(), "error");
                }

                if (data.getInterviewCode() != null) {
                    LogUtil.addNewLog(data.getInterviewCode(),
                            new LogDao("Register Response API",
                                    "Error : " + resultApiDao.getMessage()
                            )
                    );
                }
                break;
        }
    }

    public abstract void onInterviewType(InterviewApiDao interview);

    public abstract void onTestType(InterviewApiDao interview);

    public abstract void onSectionType(InterviewApiDao interview);

}