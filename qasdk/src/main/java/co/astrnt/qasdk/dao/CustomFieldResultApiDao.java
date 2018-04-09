package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by deni rohimat on 09/04/18.
 */
public class CustomFieldResultApiDao implements Parcelable {
    public static final Creator<CustomFieldResultApiDao> CREATOR = new Creator<CustomFieldResultApiDao>() {
        @Override
        public CustomFieldResultApiDao createFromParcel(Parcel source) {
            return new CustomFieldResultApiDao(source);
        }

        @Override
        public CustomFieldResultApiDao[] newArray(int size) {
            return new CustomFieldResultApiDao[size];
        }
    };
    private List<CustomFieldApiDao> fields;

    public CustomFieldResultApiDao() {
    }

    protected CustomFieldResultApiDao(Parcel in) {
        this.fields = in.createTypedArrayList(CustomFieldApiDao.CREATOR);
    }

    public List<CustomFieldApiDao> getFields() {
        return fields;
    }

    public void setFields(List<CustomFieldApiDao> fields) {
        this.fields = fields;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.fields);
    }
}
