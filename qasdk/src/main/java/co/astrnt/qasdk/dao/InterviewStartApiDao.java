package co.astrnt.qasdk.dao;

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
