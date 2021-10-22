package co.astrnt.qasdk.repository;

import android.content.Context;

import co.astrnt.qasdk.AstrntSDK;
import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.utils.ServiceUtils;
import co.astrnt.qasdk.utils.services.SendLogService;

public class BaseRepository {

    protected final AstronautApi mAstronautApi;
    protected AstrntSDK astrntSDK = new AstrntSDK();

    public BaseRepository(AstronautApi astronautApi) {
        mAstronautApi = astronautApi;
    }

    public void sendLog(Context context) {
        if (!ServiceUtils.isMyServiceRunning(context, SendLogService.class)) {
            SendLogService.start(context);
        }
    }

}
