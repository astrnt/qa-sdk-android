package co.astrnt.qasdk.dao.post;

import java.util.List;

public class RegisterPost {

    private long job_id;
    private long company_id;
    private String interview_code;
    private String fullname;
    private String preferred_name;
    private String email;
    private String phone;
    private String device;
    private int version;
    private int is_profile;
    private List<CustomFieldsPost> custom_fields;

    public long getJob_id() {
        return job_id;
    }

    public void setJob_id(long job_id) {
        this.job_id = job_id;
    }

    public long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(long company_id) {
        this.company_id = company_id;
    }

    public String getInterviewCode() {
        return interview_code;
    }

    public void setInterviewCode(String interviewTempCode) {
        this.interview_code = interviewTempCode;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPreferred_name() {
        return preferred_name;
    }

    public void setPreferred_name(String preferred_name) {
        this.preferred_name = preferred_name;
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

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getIs_profile() {
        return is_profile;
    }

    public void setIs_profile(int is_profile) {
        this.is_profile = is_profile;
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
