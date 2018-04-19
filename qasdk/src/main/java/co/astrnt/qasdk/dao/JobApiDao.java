package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class JobApiDao implements Parcelable {
    public static final Creator<JobApiDao> CREATOR = new Creator<JobApiDao>() {
        @Override
        public JobApiDao createFromParcel(Parcel source) {
            return new JobApiDao(source);
        }

        @Override
        public JobApiDao[] newArray(int size) {
            return new JobApiDao[size];
        }
    };
    private long id;
    private String title;
    private int requiredCv;
    private String description;
    private String location;
    private String requirement;
    private String responsibility;
    private String type;

    public JobApiDao() {
    }

    protected JobApiDao(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.requiredCv = in.readInt();
        this.description = in.readString();
        this.location = in.readString();
        this.requirement = in.readString();
        this.responsibility = in.readString();
        this.type = in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRequiredCv() {
        return requiredCv != 0;
    }

    public void setRequiredCv(int requiredCv) {
        this.requiredCv = requiredCv;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getResponsibility() {
        return responsibility;
    }

    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeInt(this.requiredCv);
        dest.writeString(this.description);
        dest.writeString(this.location);
        dest.writeString(this.requirement);
        dest.writeString(this.responsibility);
        dest.writeString(this.type);
    }
}
