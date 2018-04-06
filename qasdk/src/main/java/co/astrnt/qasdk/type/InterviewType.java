package co.astrnt.qasdk.type;

public enum InterviewType {
    CLOSE_INTERVIEW("close interview"),
    CLOSE_TEST("close test"),
    CLOSE_SECTION("close section"),
    OPEN_INTERVIEW("open interview"),
    OPEN_TEST("open test"),
    OPEN_SECTION("open section");

    private String type;

    InterviewType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}