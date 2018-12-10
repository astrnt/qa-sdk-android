package co.astrnt.qasdk.utils;

import com.orhanobut.hawk.Hawk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import co.astrnt.qasdk.dao.LogDao;

public class LogUtil {

    public static void addNewLog(String key, LogDao itemLog) {
        List<LogDao> logDaos = Hawk.get(key, new ArrayList<LogDao>());
        logDaos.add(itemLog);
        Hawk.put(key, logDaos);
    }

    public static List<LogDao> getLog(String key) {
        return Hawk.get(key);
    }

    public static String getTimeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
        String timeZone = new SimpleDateFormat("Z").format(calendar.getTime());
        return timeZone.substring(0, 3) + ":" + timeZone.substring(3, 5);
    }
}
