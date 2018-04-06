package co.astrnt.qasdk.dao;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class JobApiDao extends BaseApiDao {
    private String description;
    private String id;
    private String location;
    private String requirement;
    private String title;
    private int requireCv;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRequireCv() {
        return requireCv;
    }

    public void setRequireCv(int requireCv) {
        this.requireCv = requireCv;
    }
}
