package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class JobApiDao implements Parcelable {
    private long id;
    private String title;
    private int required;
    private String description;
    private String location;
    private String requirement;
    private String responsibility;

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

    public int getRequired() {
        return required;
    }

    public void setRequired(int required) {
        this.required = required;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeInt(this.required);
        dest.writeString(this.description);
        dest.writeString(this.location);
        dest.writeString(this.requirement);
        dest.writeString(this.responsibility);
    }

    public JobApiDao() {
    }

    protected JobApiDao(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.required = in.readInt();
        this.description = in.readString();
        this.location = in.readString();
        this.requirement = in.readString();
        this.responsibility = in.readString();
    }

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
}
