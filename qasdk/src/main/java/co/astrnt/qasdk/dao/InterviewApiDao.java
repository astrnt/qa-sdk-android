package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class InterviewApiDao extends BaseApiDao implements Parcelable {

    public static final Creator<InterviewApiDao> CREATOR = new Creator<InterviewApiDao>() {
        @Override
        public InterviewApiDao createFromParcel(Parcel source) {
            return new InterviewApiDao(source);
        }

        @Override
        public InterviewApiDao[] newArray(int size) {
            return new InterviewApiDao[size];
        }
    };
    private String type;
    private String invite_id;
    private int is_allowed_preview;
    private String first_time;
    private int duration_left;
    private JobApiDao job;
    private CompanyApiDao company;
    private CandidateApiDao candidate;
    private CustomFieldResultApiDao custom_fields;
    private List<SectionApiDao> sections;
    private List<QuestionApiDao> questions;
    private String lang;

    public InterviewApiDao() {
    }

    protected InterviewApiDao(Parcel in) {
        this.type = in.readString();
        this.invite_id = in.readString();
        this.is_allowed_preview = in.readInt();
        this.first_time = in.readString();
        this.duration_left = in.readInt();
        this.job = in.readParcelable(JobApiDao.class.getClassLoader());
        this.company = in.readParcelable(CompanyApiDao.class.getClassLoader());
        this.candidate = in.readParcelable(CandidateApiDao.class.getClassLoader());
        this.custom_fields = in.readParcelable(CustomFieldResultApiDao.class.getClassLoader());
        this.sections = in.createTypedArrayList(SectionApiDao.CREATOR);
        this.questions = in.createTypedArrayList(QuestionApiDao.CREATOR);
        this.lang = in.readString();
    }

    public List<QuestionApiDao> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionApiDao> questions) {
        this.questions = questions;
    }

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

    public boolean getIs_allowed_preview() {
        return is_allowed_preview != 0;
    }

    public void setIs_allowed_preview(int is_allowed_preview) {
        this.is_allowed_preview = is_allowed_preview;
    }

    public boolean isFirst_time() {
        return first_time.equals("yes");
    }

    public void setFirst_time(String first_time) {
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

    public CustomFieldResultApiDao getCustom_fields() {
        return custom_fields;
    }

    public void setCustom_fields(CustomFieldResultApiDao custom_fields) {
        this.custom_fields = custom_fields;
    }

    public List<SectionApiDao> getSections() {
        return sections;
    }

    public void setSections(List<SectionApiDao> sections) {
        this.sections = sections;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public int getTotalQuestion() {
        return getQuestions().size();
    }

    public int getTotalAttempt() {
        int totalAttempt = 0;

        for (QuestionApiDao item : getQuestions()) {
            totalAttempt += item.getTakesCount();
        }
        return totalAttempt;
    }

    public int getTotalUpload() {
        return getTotalQuestion() * 5;
    }

    public int getEstimatedTime() {
        return getTotalAttempt() * getTotalQuestion() * 2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.invite_id);
        dest.writeInt(this.is_allowed_preview);
        dest.writeString(this.first_time);
        dest.writeInt(this.duration_left);
        dest.writeParcelable(this.job, flags);
        dest.writeParcelable(this.company, flags);
        dest.writeParcelable(this.candidate, flags);
        dest.writeParcelable(this.custom_fields, flags);
        dest.writeTypedList(this.sections);
        dest.writeTypedList(this.questions);
        dest.writeString(this.lang);
    }
}
