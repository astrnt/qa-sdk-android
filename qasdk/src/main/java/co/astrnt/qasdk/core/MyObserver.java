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
import retrofit2.HttpException;

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
        String message;
        if (e instanceof retrofit2.HttpException) {
            try {
                HttpException httpException = (HttpException) e;
                if (httpException.code() == 429) {
                    message = httpException.getMessage();
                    onApiResultError("", message, "error");
                } else {
                    ResponseBody body = ((retrofit2.HttpException) e).response().errorBody();
                    if (body != null) {
                        BaseApiDao apiDao = new Gson().fromJson(body.string(), BaseApiDao.class);
                        message = apiDao.getMessage();
                        if (apiDao.getTitle() != null) {
                            onApiResultError(apiDao.getTitle(), message, "error");
                        } else {
                            onApiResultError("", message, "error");
                        }
                    } else {
                        message = e.getMessage();
                        onApiResultError("", message, "error");
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                message = e2.getMessage();
                onApiResultError("", message, "exception");
            }
        } else if (e instanceof UnknownHostException) {
            message = e.getMessage();
            onApiResultError("", message, "exception");
        } else if (e instanceof SocketTimeoutException) {
            message = e.getMessage();
            onApiResultError("", message, "exception");
        } else {
            message = e.getMessage();
            System.err.println(message);
            e.printStackTrace();
            onApiResultError("", message, "exception");
        }

        String interviewCode = astrntSDK.getInterviewCode();
        LogUtil.addNewLog(interviewCode,
                new LogDao("Response API",
                        "Response Error : " + message
                )
        );
    }

    @Override
    public final void onNext(T t) {
        onComplete();
        String interviewCode = astrntSDK.getInterviewCode();
        if (t == null) {
            onApiResultError("", "Failed connect to server", "error");

            LogUtil.addNewLog(interviewCode,
                    new LogDao("Response API",
                            "Error : Failed connect to server"
                    )
            );

            return;
        }
        if (t.getStatus() != null && t.getStatus().contains("error")) {
            if (t instanceof InterviewResultApiDao) {
                InterviewResultApiDao data = (InterviewResultApiDao) t;
                InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
                if (interviewApiDao != null) {
                    if (data.getToken() != null && !data.getToken().isEmpty()) {
                        LogUtil.addNewLog(interviewCode,
                                new LogDao("Token Changed",
                                        "New Token : " + data.getToken()
                                )
                        );
                        astrntSDK.saveInterview(interviewApiDao, data.getToken(), data.getInterview_code());
                    }

                    if (interviewCode != null) {
                        String message = "";
                        if (t.getMessage() != null) {
                            message = t.getMessage();
                        }
                        LogUtil.addNewLog(interviewCode,
                                new LogDao("Response API",
                                        "Error : " + message
                                )
                        );
                    }
                }
            }
            String message = "";
            if (t.getMessage() != null) {
                message = t.getMessage();
            }
            if (t.getTitle() != null) {
                onApiResultError(t.getTitle(), message, "error");
            } else {
                onApiResultError("", message, "error");
            }

            if (interviewCode != null) {
                if (t.getMessage() != null) {
                    message = t.getMessage();
                }
                LogUtil.addNewLog(interviewCode,
                        new LogDao("Response API",
                                "Error : " + message
                        )
                );
            }

        } else {
            String message = "";
            if (t.getMessage() != null) {
                message = t.getMessage();
            }
            if (interviewCode != null) {
                LogUtil.addNewLog(interviewCode,
                        new LogDao("Response API",
                                "Success : " + message
                        )
                );
            }

            onApiResultOk(t);
        }
    }

    public abstract void onApiResultCompleted();

    public abstract void onApiResultError(String title, String message, String code);

    public abstract void onApiResultOk(T t);
}