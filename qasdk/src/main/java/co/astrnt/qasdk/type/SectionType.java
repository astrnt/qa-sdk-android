package co.astrnt.qasdk.type;

public enum SectionType {
    INTERVIEW("interview"),
    TEST("test"),;

    private String type;

    SectionType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}