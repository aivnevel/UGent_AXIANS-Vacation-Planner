package models.planning;

public class UnwantedShiftPattern {
    private String id;
    private String shift1;
    private String shift2;

    public UnwantedShiftPattern() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShift1() {
        return shift1;
    }

    public void setShift1(String shift1) {
        this.shift1 = shift1;
    }

    public String getShift2() {
        return shift2;
    }

    public void setShift2(String shift2) {
        this.shift2 = shift2;
    }

    @Override
    public String toString() {
        return "UnwantedShiftPattern{" +
                "id='" + id + '\'' +
                ", shift1='" + shift1 + '\'' +
                ", shift2='" + shift2 + '\'' +
                '}';
    }
}
