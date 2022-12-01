package co.astrnt.qasdk.event;

public class Compress {

    private String path;
    private long questionId;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public Compress(String path, long questionId) {
        this.path = path;
        this.questionId = questionId;
    }

    public Compress() {
    }

    public Compress(String path) {
        this.path = path;
    }
}

