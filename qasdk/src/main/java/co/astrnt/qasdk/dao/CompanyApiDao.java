package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class CompanyApiDao implements Parcelable {
    public static final Creator<CompanyApiDao> CREATOR = new Creator<CompanyApiDao>() {
        @Override
        public CompanyApiDao createFromParcel(Parcel source) {
            return new CompanyApiDao(source);
        }

        @Override
        public CompanyApiDao[] newArray(int size) {
            return new CompanyApiDao[size];
        }
    };
    private String coverPicture;
    private long id;
    private String logo;
    private String requirement;
    private String nda;

    public CompanyApiDao() {
    }

    protected CompanyApiDao(Parcel in) {
        this.coverPicture = in.readString();
        this.id = in.readLong();
        this.logo = in.readString();
        this.requirement = in.readString();
        this.nda = in.readString();
    }

    public String getCoverPicture() {
        return coverPicture;
    }

    public void setCoverPicture(String coverPicture) {
        this.coverPicture = coverPicture;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.coverPicture);
        dest.writeLong(this.id);
        dest.writeString(this.logo);
        dest.writeString(this.requirement);
        dest.writeString(this.nda);
    }
}
