package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deni rohimat on 09/04/18.
 */
public class CustomFieldApiDao implements Parcelable {
    public static final Creator<CustomFieldApiDao> CREATOR = new Creator<CustomFieldApiDao>() {
        @Override
        public CustomFieldApiDao createFromParcel(Parcel source) {
            return new CustomFieldApiDao(source);
        }

        @Override
        public CustomFieldApiDao[] newArray(int size) {
            return new CustomFieldApiDao[size];
        }
    };
    private long id;
    private String label;
    private int is_mandatory;

    public CustomFieldApiDao() {
    }

    protected CustomFieldApiDao(Parcel in) {
        this.id = in.readLong();
        this.label = in.readString();
        this.is_mandatory = in.readInt();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isMandatory() {
        return is_mandatory != 0;
    }

    public void setIs_mandatory(int is_mandatory) {
        this.is_mandatory = is_mandatory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.label);
        dest.writeInt(this.is_mandatory);
    }
}
