package co.astrnt.qasdk.dao;

import java.util.List;

import co.astrnt.qasdk.type.InterviewType;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class InterviewApiDao extends BaseApiDao {

    private String type;
    private String invite_id;
    private boolean is_allowed_preview;
    private boolean first_time;
    private int duration_left;
    private JobApiDao job;
    private CompanyApiDao company;
    private CandidateApiDao candidate;
    private List<SectionApiDao> sections;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInvite_id() {
        return invite_id;
    }

    public void setInvite_id(String invite_id) {
        this.invite_id = invite_id;
    }

    public boolean isIs_allowed_preview() {
        return is_allowed_preview;
    }

    public void setIs_allowed_preview(boolean is_allowed_preview) {
        this.is_allowed_preview = is_allowed_preview;
    }

    public boolean isFirst_time() {
        return first_time;
    }

    public void setFirst_time(boolean first_time) {
        this.first_time = first_time;
    }

    public int getDuration_left() {
        return duration_left;
    }

    public void setDuration_left(int duration_left) {
        this.duration_left = duration_left;
    }

    public JobApiDao getJob() {
        return job;
    }

    public void setJob(JobApiDao job) {
        this.job = job;
    }

    public CompanyApiDao getCompany() {
        return company;
    }

    public void setCompany(CompanyApiDao company) {
        this.company = company;
    }

    public CandidateApiDao getCandidate() {
        return candidate;
    }

    public void setCandidate(CandidateApiDao candidate) {
        this.candidate = candidate;
    }

    public List<SectionApiDao> getSections() {
        return sections;
    }

    public void setSections(List<SectionApiDao> sections) {
        this.sections = sections;
    }
}
