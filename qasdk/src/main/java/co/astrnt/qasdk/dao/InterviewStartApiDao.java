package co.astrnt.qasdk.dao;

/**
 * Created by deni rohimat on 27/04/18.
 */
public class InterviewStartApiDao extends BaseApiDao {

    private int finished;

    public boolean isFinished() {
        return finished != 0;
    }

    public void setFinished(boolean finished) {
        if (finished) {
            this.finished = 1;
        } else {
            this.finished = 0;
        }
    }
}
