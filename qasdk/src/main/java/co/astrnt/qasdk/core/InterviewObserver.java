package co.astrnt.qasdk.core;

import co.astrnt.qasdk.dao.InformationApiDao;
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
            if (data.getType().contains(OPEN)) {

                if (data.getInterviewCode() != null) {
                    LogUtil.addNewLog(data.getInterviewCode(),
                            new LogDao("Response API",
                                    "Success, will move to Register"
                            )
                    );
                }
                onNeedToRegister(data);
            } else {
                astrntSDK.saveInterview(data, resultApiDao.getToken(), data.getInterviewCode());
                InformationApiDao information = resultApiDao.getInformation();

                if (information != null && information.isFinished()) {
                    onApiResultError("", information.getMessage(), "error");
                } else {
                    String interviewCode = astrntSDK.getInterviewCode();
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
                        case CLOSE_APTITUDE:
                            if (interviewCode != null) {
                                LogUtil.addNewLog(interviewCode,
                                        new LogDao("Response API",
                                                "Success, will move to Close Aptitude Rating Scale"
                                        )
                                );
                            }
                            onAptitudeType(data);
                            break;
                        default:
                            if (resultApiDao.getTitle() != null) {
                                onApiResultError(resultApiDao.getTitle(), resultApiDao.getMessage(), "error");
                            } else {
                                onApiResultError("", resultApiDao.getMessage(), "error");
                            }

                            if (interviewCode != null) {
                                LogUtil.addNewLog(interviewCode,
                                        new LogDao("Enter Code Response API",
                                                "Error : " + resultApiDao.getMessage()
                                        )
                                );
                            }
                            break;
                    }
                }
            }
        }
    }

    public abstract void onNeedToRegister(InterviewApiDao interview);

    public abstract void onInterviewType(InterviewApiDao interview);

    public abstract void onTestType(InterviewApiDao interview);

    public abstract void onSectionType(InterviewApiDao interview);

    public abstract void onAstronautProfileType(InterviewApiDao interview);

    public abstract void onAptitudeType(InterviewApiDao interview);

}