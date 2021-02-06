package models.planning;

public class ShiftSequence {
    private String id;
    private String shift;
    private String fromDay;
    private String tillDay;

    public ShiftSequence() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getFromDay() {
        return fromDay;
    }

    public void setFromDay(String fromDay) {
        this.fromDay = fromDay;
    }

    public String getTillDay() {
        return tillDay;
    }

    public void setTillDay(String tillDay) {
        this.tillDay = tillDay;
    }

    @Override
    public String toString() {
        return "ShiftSequence{" +
                "id='" + id + '\'' +
                ", shift='" + shift + '\'' +
                ", fromDay='" + fromDay + '\'' +
                ", tillDay='" + tillDay + '\'' +
                '}';
    }
}
