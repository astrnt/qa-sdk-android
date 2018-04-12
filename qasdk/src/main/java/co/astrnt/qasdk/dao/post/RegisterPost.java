package co.astrnt.qasdk.dao.post;

import java.util.List;

public class RegisterPost {

    private long jobId;
    private long companyId;
    private String interviewTempCode;
    private String fullname;
    private String preferredName;
    private String email;
    private String phone;
    private List<CustomFieldsPost> custom_fields;

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public String getInterviewTempCode() {
        return interviewTempCode;
    }

    public void setInterviewTempCode(String interviewTempCode) {
        this.interviewTempCode = interviewTempCode;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<CustomFieldsPost> getCustom_fields() {
        return custom_fields;
    }

    public void setCustom_fields(List<CustomFieldsPost> custom_fields) {
        this.custom_fields = custom_fields;
    }

    public static class CustomFieldsPost {

        private long id;
        private String value;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
