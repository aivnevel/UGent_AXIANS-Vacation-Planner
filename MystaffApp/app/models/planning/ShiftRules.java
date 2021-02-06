package models.planning;

public class ShiftRules {
    private String id;
    private String ifShift;
    private String ifOnDay;
    private boolean not;
    private String thenShift;
    private String thenOnDay;

    public ShiftRules() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIfShift() {
        return ifShift;
    }

    public void setIfShift(String ifShift) {
        this.ifShift = ifShift;
    }

    public String getIfOnDay() {
        return ifOnDay;
    }

    public void setIfOnDay(String ifOnDay) {
        this.ifOnDay = ifOnDay;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public String getThenShift() {
        return thenShift;
    }

    public void setThenShift(String thenShift) {
        this.thenShift = thenShift;
    }

    public String getThenOnDay() {
        return thenOnDay;
    }

    public void setThenOnDay(String thenOnDay) {
        this.thenOnDay = thenOnDay;
    }

    @Override
    public String toString() {
        return "ShiftRules{" +
                "id='" + id + '\'' +
                ", ifShift='" + ifShift + '\'' +
                ", ifOnDay='" + ifOnDay + '\'' +
                ", not=" + not +
                ", thenShift='" + thenShift + '\'' +
                ", thenOnDay='" + thenOnDay + '\'' +
                '}';
    }
}
