package co.astrnt.astrntqasdk;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.repository.InterviewRepository;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private InterviewRepository mInterviewRepository;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mInterviewRepository = new InterviewRepository(AstronautApp.getApi());

        mInterviewRepository.enterCode("mimpi")
                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
                .subscribe(new MyObserver<InterviewApiDao>() {
                    @Override
                    public void onApiResultCompleted() {
                    }

                    @Override
                    public void onApiResultError(String message, String code) {
                        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onApiResultOk(InterviewApiDao interviewApiDao) {
                        Toast.makeText(mContext, "success", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
