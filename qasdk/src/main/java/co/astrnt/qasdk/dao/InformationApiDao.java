package co.astrnt.qasdk.dao;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 19/04/18.
 */
public class InformationApiDao extends RealmObject implements Parcelable {

    public static final Creator<InformationApiDao> CREATOR = new Creator<InformationApiDao>() {
        @Override
        public InformationApiDao createFromParcel(Parcel source) {
            return new InformationApiDao(source);
        }

        @Override
        public InformationApiDao[] newArray(int size) {
            return new InformationApiDao[size];
        }
    };
    @PrimaryKey
    private long id;
    private boolean finished;
    private int interviewIndex;
    private int interviewAttempt;
    private String status;
    //    private List<?> prevQuestStates;
    private String message;

    public InformationApiDao() {
    }

    protected InformationApiDao(Parcel in) {
        this.id = in.readLong();
        this.finished = in.readByte() != 0;
        this.interviewIndex = in.readInt();
        this.interviewAttempt = in.readInt();
        this.status = in.readString();
        this.message = in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getInterviewIndex() {
        return interviewIndex;
    }

    public void setInterviewIndex(int interviewIndex) {
        this.interviewIndex = interviewIndex;
    }

    public int getInterviewAttempt() {
        return interviewAttempt;
    }

//    public List<?> getPrevQuestStates() {
//        return prevQuestStates;
//    }
//
//    public void setPrevQuestStates(List<?> prevQuestStates) {
//        this.prevQuestStates = prevQuestStates;
//    }

    public void setInterviewAttempt(int interviewAttempt) {
        this.interviewAttempt = interviewAttempt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeByte(this.finished ? (byte) 1 : (byte) 0);
        dest.writeInt(this.interviewIndex);
        dest.writeInt(this.interviewAttempt);
        dest.writeString(this.status);
        dest.writeString(this.message);
    }
}
