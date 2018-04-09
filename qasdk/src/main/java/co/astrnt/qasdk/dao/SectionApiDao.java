package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deni rohimat on 06/04/18.
 */
public class SectionApiDao implements Parcelable {
    public static final Creator<SectionApiDao> CREATOR = new Creator<SectionApiDao>() {
        @Override
        public SectionApiDao createFromParcel(Parcel source) {
            return new SectionApiDao(source);
        }

        @Override
        public SectionApiDao[] newArray(int size) {
            return new SectionApiDao[size];
        }
    };
    private long id;
    private String title;
    private String instruction;
    private String type; //SectionType
    private int duration;
    private int preparation_time;
    private boolean randomize;
    private String image;
    private String parent_id;

    public SectionApiDao() {
    }

    protected SectionApiDao(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.instruction = in.readString();
        this.type = in.readString();
        this.duration = in.readInt();
        this.preparation_time = in.readInt();
        this.randomize = in.readByte() != 0;
        this.image = in.readString();
        this.parent_id = in.readString();
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

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPreparation_time() {
        return preparation_time;
    }

    public void setPreparation_time(int preparation_time) {
        this.preparation_time = preparation_time;
    }

    public boolean isRandomize() {
        return randomize;
    }

    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.instruction);
        dest.writeString(this.type);
        dest.writeInt(this.duration);
        dest.writeInt(this.preparation_time);
        dest.writeByte(this.randomize ? (byte) 1 : (byte) 0);
        dest.writeString(this.image);
        dest.writeString(this.parent_id);
    }
}
