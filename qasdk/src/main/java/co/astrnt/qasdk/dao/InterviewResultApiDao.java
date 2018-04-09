package co.astrnt.qasdk.dao;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class InterviewResultApiDao extends BaseApiDao {

    private InterviewApiDao interview;

    public InterviewApiDao getInterview() {
        return interview;
    }

    public void setInterview(InterviewApiDao interview) {
        this.interview = interview;
    }
}
