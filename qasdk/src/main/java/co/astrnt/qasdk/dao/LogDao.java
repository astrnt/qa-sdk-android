package co.astrnt.qasdk.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by deni rohimat on 10/12/18.
 */
public class LogDao {

    private String event;
    private String log_time;
    private String message;

    public LogDao(String event, String message) {
        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date resultdate = new Date(yourmilliseconds);
        String logTime = sdf.format(resultdate);

        this.event = event;
        this.log_time = logTime;
        this.message = message;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getLog_time() {
        return log_time;
    }

    public void setLog_time(String log_time) {
        this.log_time = log_time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
