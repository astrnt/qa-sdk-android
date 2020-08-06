package co.astrnt.qasdk.core;

import co.astrnt.qasdk.dao.InformationApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.utils.LogUtil;

import static co.astrnt.qasdk.type.InterviewType.ASTRONAUT_PROFILE;
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

        String interviewCode = astrntSDK.getInterviewCode();
        InterviewApiDao newInterview = resultApiDao.getInterview();

        InformationApiDao information = resultApiDao.getInformation();

        if (information.isFinished()) {
            astrntSDK.setInterviewFinished();
            astrntSDK.setFinishInterview(false);
            astrntSDK.setShowUpload(true);
        }

        switch (newInterview.getType()) {
            case CLOSE_INTERVIEW:
            case CLOSE_SECTION:
            case CLOSE_INTERVIEW_PROFILE:
            case CLOSE_TEST:
                LogUtil.addNewLog(interviewCode,
                        new LogDao("Response API",
                                "Success, will move to Info"
                        )
                );

                if (interviewCode.equals(resultApiDao.getInterview_code())) {
                    astrntSDK.updateInterviewData(currentInterview, newInterview);
                    currentInterview = astrntSDK.getCurrentInterview();
                    astrntSDK.saveInterviewResult(resultApiDao, currentInterview, true);
                } else {
                    astrntSDK.saveInterviewResult(resultApiDao, newInterview, true);
                }

                onContinueInterview();
                break;
            case ASTRONAUT_PROFILE:
                if (interviewCode != null) {
                    LogUtil.addNewLog(interviewCode,
                            new LogDao("Response API",
                                    "Success, will move to Astronaut Profile"
                            )
                    );
                }
                onAstronautProfileType(newInterview);
                break;
            default:
                String message = resultApiDao.getMessage();
                if (resultApiDao.getTitle() != null) {
                    onApiResultError(resultApiDao.getTitle(), message, "error");
                } else {
                    onApiResultError("", message, "error");
                }
                LogUtil.addNewLog(interviewCode,
                        new LogDao("Continue Response API",
                                "Error : " + message
                        )
                );
                break;
        }
    }

    public abstract void onContinueInterview();

    public abstract void onAstronautProfileType(InterviewApiDao interview);
}