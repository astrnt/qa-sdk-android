package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class CandidateApiDao implements Parcelable {
    public static final Creator<CandidateApiDao> CREATOR = new Creator<CandidateApiDao>() {
        @Override
        public CandidateApiDao createFromParcel(Parcel source) {
            return new CandidateApiDao(source);
        }

        @Override
        public CandidateApiDao[] newArray(int size) {
            return new CandidateApiDao[size];
        }
    };
    private int id;
    private String email;
    private String fullname;

    public CandidateApiDao() {
    }

    protected CandidateApiDao(Parcel in) {
        this.id = in.readInt();
        this.email = in.readString();
        this.fullname = in.readString();
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.email);
        dest.writeString(this.fullname);
    }
}
