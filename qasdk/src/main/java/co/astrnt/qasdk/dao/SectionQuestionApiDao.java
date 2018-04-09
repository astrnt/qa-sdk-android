package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class SectionQuestionApiDao implements Parcelable {
    public static final Creator<SectionQuestionApiDao> CREATOR = new Creator<SectionQuestionApiDao>() {
        @Override
        public SectionQuestionApiDao createFromParcel(Parcel source) {
            return new SectionQuestionApiDao(source);
        }

        @Override
        public SectionQuestionApiDao[] newArray(int size) {
            return new SectionQuestionApiDao[size];
        }
    };
    private long id;
    private long section_id;

    public SectionQuestionApiDao() {
    }

    protected SectionQuestionApiDao(Parcel in) {
        this.id = in.readLong();
        this.section_id = in.readLong();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSection_id() {
        return section_id;
    }

    public void setSection_id(long section_id) {
        this.section_id = section_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.section_id);
    }
}
