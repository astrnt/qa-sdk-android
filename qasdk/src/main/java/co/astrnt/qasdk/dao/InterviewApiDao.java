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
    private boolean is_allowed_preview;
    private boolean first_time;
    private int duration_left;
    private JobApiDao job;
    private CompanyApiDao company;
    private CandidateApiDao candidate;
    private CustomFieldResultApiDao custom_fields;
    private List<SectionApiDao> sections;
    private String lang;

    public InterviewApiDao() {
    }

    protected InterviewApiDao(Parcel in) {
        this.type = in.readString();
        this.invite_id = in.readString();
        this.is_allowed_preview = in.readByte() != 0;
        this.first_time = in.readByte() != 0;
        this.duration_left = in.readInt();
        this.job = in.readParcelable(JobApiDao.class.getClassLoader());
        this.company = in.readParcelable(CompanyApiDao.class.getClassLoader());
        this.candidate = in.readParcelable(CandidateApiDao.class.getClassLoader());
        this.custom_fields = in.readParcelable(CustomFieldResultApiDao.class.getClassLoader());
        this.sections = in.createTypedArrayList(SectionApiDao.CREATOR);
        this.lang = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.invite_id);
        dest.writeByte(this.is_allowed_preview ? (byte) 1 : (byte) 0);
        dest.writeByte(this.first_time ? (byte) 1 : (byte) 0);
        dest.writeInt(this.duration_left);
        dest.writeParcelable(this.job, flags);
        dest.writeParcelable(this.company, flags);
        dest.writeParcelable(this.candidate, flags);
        dest.writeParcelable(this.custom_fields, flags);
        dest.writeTypedList(this.sections);
        dest.writeString(this.lang);
    }
}
