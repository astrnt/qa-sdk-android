package co.astrnt.qasdk.dao;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class BaseApiDao {

    private String status;
    private String title;
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
