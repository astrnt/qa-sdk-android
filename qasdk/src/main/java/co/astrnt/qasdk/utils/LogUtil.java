package co.astrnt.qasdk.utils;

import com.orhanobut.hawk.Hawk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import co.astrnt.qasdk.dao.LogDao;

public class LogUtil {

    public static void addNewLog(String interviewCode, LogDao itemLog) {
        List<LogDao> logDaoList = getLog(interviewCode);
        logDaoList.add(itemLog);
        Set<LogDao> set = new LinkedHashSet<>(logDaoList);
        List<LogDao> logWithoutDuplicates = new ArrayList<>(set);
        Hawk.put(interviewCode, logWithoutDuplicates);
    }

    public static List<LogDao> getLog(String interviewCode) {
        List<LogDao> logDaoList = Hawk.get(interviewCode, new ArrayList<>());
        Set<LogDao> set = new LinkedHashSet<>(logDaoList);
        List<LogDao> logWithoutDuplicates = new ArrayList<>(set);
        Hawk.put(interviewCode, logWithoutDuplicates);
        return logWithoutDuplicates;
    }

    public static String getTimeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
        String timeZone = new SimpleDateFormat("Z").format(calendar.getTime());
        return timeZone.substring(0, 3) + ":" + timeZone.substring(3, 5);
    }

    public static void clearSentLog(String interviewCode, List<LogDao> sentLog) {
        List<LogDao> logDaoList = getLog(interviewCode);
        logDaoList.removeAll(sentLog);
        Hawk.put(interviewCode, logDaoList);
    }

}
