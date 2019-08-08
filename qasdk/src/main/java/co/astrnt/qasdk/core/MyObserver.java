package co.astrnt.qasdk.core;

import com.google.gson.Gson;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.utils.LogUtil;
import io.reactivex.Observer;
import okhttp3.ResponseBody;

/**
 * Created by deni rohimat on 06/04/18.
 */
public abstract class MyObserver<T extends BaseApiDao> implements Observer<T> {

    protected AstrntSDK astrntSDK = new AstrntSDK();

    @Override
    public final void onComplete() {
        onApiResultCompleted();
    }

    @Override
    public final void onError(Throwable e) {
        onComplete();
        String message = e.getMessage();
        if (e instanceof retrofit2.HttpException) {
            try {
                ResponseBody body = ((retrofit2.HttpException) e).response().errorBody();
                assert body != null;
                BaseApiDao apiDao = new Gson().fromJson(body.string(), BaseApiDao.class);
                message = apiDao.getMessage();
                if (apiDao.getTitle() != null) {
                    onApiResultError(apiDao.getTitle(), apiDao.getMessage(), "error");
                } else {
                    onApiResultError("", apiDao.getMessage(), "error");
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                onApiResultError("", e.getMessage(), "exception");
            }
        } else if (e instanceof UnknownHostException) {
            onApiResultError("", e.getMessage(), "exception");
        } else if (e instanceof SocketTimeoutException) {
            onApiResultError("", e.getMessage(), "exception");
        } else {
            System.err.println(e.getMessage());
            e.printStackTrace();
            onApiResultError("", e.getMessage(), "exception");
        }

        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        if (interviewApiDao != null && interviewApiDao.getInterviewCode() != null) {
            LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                    new LogDao("Hit API",
                            "Response Error : " + message
                    )
            );
        }
    }

    @Override
    public final void onNext(T t) {
        if (t == null) {
            onApiResultError("", "Failed connect to server", "error");
            return;
        }
        if (t.getStatus() != null && t.getStatus().contains("error")) {
            if (t instanceof InterviewResultApiDao) {
                InterviewResultApiDao data = (InterviewResultApiDao) t;
                InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
                if (interviewApiDao != null) {
                    if (data.getToken() != null && !data.getToken().isEmpty()) {
                        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                                new LogDao("Token Changed",
                                        "New Token : " + data.getToken()
                                )
                        );
                        astrntSDK.saveInterview(interviewApiDao, data.getToken(), data.getInterview_code());
                    }
                }

                if (interviewApiDao != null && interviewApiDao.getInterviewCode() != null) {
                    LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                            new LogDao("Hit API",
                                    "Response Error : " + t.getMessage()
                            )
                    );
                }

            }
            if (t.getTitle() != null) {
                onApiResultError(t.getTitle(), t.getMessage(), "error");
            } else {
                onApiResultError("", t.getMessage(), "error");
            }
        } else {
            onApiResultOk(t);
        }
    }

    public abstract void onApiResultCompleted();

    public abstract void onApiResultError(String title, String message, String code);

    public abstract void onApiResultOk(T t);
}