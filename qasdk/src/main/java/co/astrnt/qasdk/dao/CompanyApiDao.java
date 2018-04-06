package co.astrnt.qasdk.dao;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class CompanyApiDao extends BaseApiDao {
    private String coverPicture;
    private String id;
    private String logo;
    private String requirement;
    private String nda;

    public String getCoverPicture() {
        return coverPicture;
    }

    public void setCoverPicture(String coverPicture) {
        this.coverPicture = coverPicture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getNda() {
        return nda;
    }

    public void setNda(String nda) {
        this.nda = nda;
    }
}
