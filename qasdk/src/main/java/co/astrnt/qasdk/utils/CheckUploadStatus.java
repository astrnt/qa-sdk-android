package co.astrnt.qasdk.utils;


import android.content.Context;

import java.util.List;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.type.UploadStatusType;

public class CheckUploadStatus {

    public static boolean checkAvailability(AstrntSDK astrntSDK) {
        boolean isAvailable = false;
        List<QuestionApiDao> pendingVideo = astrntSDK.getPending(UploadStatusType.PENDING);
        List<QuestionApiDao> compressedVideo = astrntSDK.getPending(UploadStatusType.COMPRESSED);
        List<QuestionApiDao> uploadingVideo = astrntSDK.getPending(UploadStatusType.UPLOADING);

        for (QuestionApiDao item : pendingVideo) {
            isAvailable = true;
        }

        for (QuestionApiDao item : compressedVideo) {
            isAvailable = true;
        }
        for (QuestionApiDao item : uploadingVideo) {
            isAvailable = true;
        }
        return isAvailable;
    }
}
