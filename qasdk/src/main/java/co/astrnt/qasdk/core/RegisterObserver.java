package co.astrnt.qasdk.core;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.utils.LogUtil;

import static co.astrnt.qasdk.type.InterviewType.ASTRONAUT_PROFILE;
import static co.astrnt.qasdk.type.InterviewType.CLOSE_APTITUDE;
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
        String interviewCode = data.getInterviewCode();
        astrntSDK.saveInterviewResult(resultApiDao, data, false);

        switch (data.getType()) {
            case CLOSE_INTERVIEW:
            case CLOSE_INTERVIEW_PROFILE:
                if (interviewCode != null) {
                    LogUtil.addNewLog(interviewCode,
                            new LogDao("Response API",
                                    "Success, will move to Video Interview"
                            )
                    );
                }
                onInterviewType(data);
                break;
            case CLOSE_TEST:
                if (interviewCode != null) {
                    LogUtil.addNewLog(interviewCode,
                            new LogDao("Response API",
                                    "Success, will move to MCQ Interview"
                            )
                    );
                }
                onTestType(data);
                break;
            case CLOSE_SECTION:
                if (interviewCode != null) {
                    LogUtil.addNewLog(interviewCode,
                            new LogDao("Response API",
                                    "Success, will move to Section Interview"
                            )
                    );
                }
                onSectionType(data);
                break;
            case CLOSE_APTITUDE:
                if (interviewCode != null) {
                    LogUtil.addNewLog(interviewCode,
                            new LogDao("Response API",
                                    "Success, will move to Close Aptitude Rating Scale"
                            )
                    );
                }
                onAptitudeType(data);
            case ASTRONAUT_PROFILE:
                if (interviewCode != null) {
                    LogUtil.addNewLog(interviewCode,
                            new LogDao("Response API",
                                    "Success, will move to Astronaut Profile"
                            )
                    );
                }
                onAstronautProfileType(data);
                break;
            default:
                String message = resultApiDao.getMessage();
                if (resultApiDao.getTitle() != null) {
                    onApiResultError(resultApiDao.getTitle(), message, "error");
                } else {
                    onApiResultError("", message, "error");
                }
                LogUtil.addNewLog(interviewCode,
                        new LogDao("Register Response API",
                                "Error : " + message
                        )
                );
                break;
        }
    }

    public abstract void onInterviewType(InterviewApiDao interview);

    public abstract void onTestType(InterviewApiDao interview);

    public abstract void onSectionType(InterviewApiDao interview);

    public abstract void onAstronautProfileType(InterviewApiDao interview);

    public abstract void onAptitudeType(InterviewApiDao interview);

}