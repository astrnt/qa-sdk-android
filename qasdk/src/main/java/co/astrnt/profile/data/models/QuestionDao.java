package co.astrnt.profile.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class QuestionDao extends RealmObject implements Parcelable {

    @PrimaryKey
    @Expose
    @SerializedName("id")
    private int id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("answered")
    private boolean answered;

    public QuestionDao() {
    }

    public QuestionDao(int id, String title, boolean answered) {
        this.id = id;
        this.title = title;
        this.answered = answered;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    protected QuestionDao(Parcel in) {
        title = in.readString();
        id = in.readInt();
        answered = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(id);
        dest.writeByte((byte) (answered ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QuestionDao> CREATOR = new Creator<QuestionDao>() {
        @Override
        public QuestionDao createFromParcel(Parcel in) {
            return new QuestionDao(in);
        }

        @Override
        public QuestionDao[] newArray(int size) {
            return new QuestionDao[size];
        }
    };
}
