package co.astrnt.qasdk.core;

import com.google.gson.Gson;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import co.astrnt.qasdk.dao.BaseApiDao;
import okhttp3.ResponseBody;
import rx.Observer;

/**
 * Created by deni rohimat on 06/04/18.
 */
public abstract class MyObserver<T extends BaseApiDao> implements Observer<T> {

    @Override
    public final void onCompleted() {
        onApiResultCompleted();
    }

    @Override
    public final void onError(Throwable e) {
        if (e instanceof retrofit2.HttpException) {
            try {
                ResponseBody body = ((retrofit2.HttpException) e).response().errorBody();
                BaseApiDao apiDao = new Gson().fromJson(body.string(), BaseApiDao.class);
                onApiResultError("", apiDao.getStatus());
            } catch (Exception e2) {
                e2.printStackTrace();
                onApiResultError("Terjadi kesalahan, silakan hubungi customer service", "exception");
            }
            onApiResultCompleted();
        } else if (e instanceof UnknownHostException) {
            onApiResultError("Koneksi terputus, silahkan coba lagi", "exception");
        } else if (e instanceof SocketTimeoutException) {
            onApiResultError("Koneksi terputus, silahkan coba lagi", "exception");
        } else {
            System.err.println(e.getMessage());
            e.printStackTrace();
            onApiResultError("Terjadi kesalahan, silakan hubungi customer service", "exception");
        }
    }

    @Override
    public final void onNext(T t) {
        if (t.getStatus() != null && t.getStatus().contains("error")) {
            onApiResultError("", "error");
        } else {
            onApiResultOk(t);
        }
    }

    public abstract void onApiResultCompleted();

    public abstract void onApiResultError(String message, String code);

    public abstract void onApiResultOk(T t);
}