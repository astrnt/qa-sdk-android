package co.astrnt.astrntqasdk.listener;

public interface RecordListener {
    void onPreRecord();

    void onCountdown();

    void onRecording();

    void onFinished();
}