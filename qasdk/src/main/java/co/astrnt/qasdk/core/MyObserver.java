package co.astrnt.qasdk.core;

import com.google.gson.Gson;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.InterviewResultApiDao;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * Created by deni rohimat on 06/04/18.
 */
public abstract class MyObserver<T extends BaseApiDao> implements Observer<T> {

    protected AstrntSDK astrntSDK = new AstrntSDK();

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public final void onComplete() {
        onApiResultCompleted();
    }

    @Override
    public final void onError(Throwable e) {
        onComplete();
        if (e instanceof retrofit2.HttpException) {
            try {
                ResponseBody body = ((retrofit2.HttpException) e).response().errorBody();
                assert body != null;
                BaseApiDao apiDao = new Gson().fromJson(body.string(), BaseApiDao.class);
                onApiResultError(apiDao.getMessage(), apiDao.getStatus());
            } catch (Exception e2) {
                e2.printStackTrace();
                onApiResultError("Terjadi kesalahan, silakan hubungi help@astrnt.co", "exception");
            }
        } else if (e instanceof UnknownHostException) {
            onApiResultError("Koneksi terputus, silahkan coba lagi", "exception");
        } else if (e instanceof SocketTimeoutException) {
            onApiResultError("Koneksi terputus, silahkan coba lagi", "exception");
        } else {
            System.err.println(e.getMessage());
            e.printStackTrace();
            onApiResultError("Terjadi kesalahan, silakan hubungi help@astrnt.co", "exception");
        }
    }

    @Override
    public final void onNext(T t) {
        if (t.getStatus() != null && t.getStatus().contains("error")) {
            if (t instanceof InterviewResultApiDao) {
                InterviewResultApiDao data = (InterviewResultApiDao) t;
                InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
                if (data.getToken() != null && !data.getToken().isEmpty()) {
                    astrntSDK.saveInterview(interviewApiDao, data.getToken(), data.getInterview_code());
                }
            }
            onApiResultError(t.getMessage(), t.getStatus());
        } else {
            onApiResultOk(t);
        }
    }

    public abstract void onApiResultCompleted();

    public abstract void onApiResultError(String message, String code);

    public abstract void onApiResultOk(T t);
}