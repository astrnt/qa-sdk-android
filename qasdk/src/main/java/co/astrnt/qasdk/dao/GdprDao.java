package co.astrnt.qasdk.dao;

public class GdprDao {

    private int gdpr_complied;
    private String gdpr_text;
    private String gdpr_aggrement_text;
    private String compliance;

    public GdprDao(int gdpr_complied, String gdpr_text, String gdpr_aggrement_text, String compliance) {
        this.gdpr_complied = gdpr_complied;
        this.gdpr_text = gdpr_text;
        this.gdpr_aggrement_text = gdpr_aggrement_text;
        this.compliance = compliance;
    }

    public boolean isGdprComplied() {
        return gdpr_complied == 1;
    }

    public void setGdpr_complied(int gdpr_complied) {
        this.gdpr_complied = gdpr_complied;
    }

    public String getGdpr_text() {
        return gdpr_text;
    }

    public void setGdpr_text(String gdpr_text) {
        this.gdpr_text = gdpr_text;
    }

    public String getGdpr_aggrement_text() {
        return gdpr_aggrement_text;
    }

    public void setGdpr_aggrement_text(String gdpr_aggrement_text) {
        this.gdpr_aggrement_text = gdpr_aggrement_text;
    }

    public String getCompliance() {
        return compliance;
    }

    public void setCompliance(String compliance) {
        this.compliance = compliance;
    }
}
