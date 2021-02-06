package models.planning;

import java.util.Arrays;

public class SimultaneousShift {
    private String id;
    private String[] shifts;

    public SimultaneousShift() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getShifts() {
        return shifts;
    }

    public void setShifts(String[] shifts) {
        this.shifts = shifts;
    }

    @Override
    public String toString() {
        return "SimultaneousShift{" +
                "id='" + id + '\'' +
                ", shifts=" + Arrays.toString(shifts) +
                '}';
    }
}
