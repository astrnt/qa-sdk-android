package co.astrnt.astrntqasdk.listener;

public interface PreviewListener {
    void onVideoFinished();

    void onVideoPlay();

    void onVideoPause();

    void onVideoRetake();

    void onVideoDone();
}