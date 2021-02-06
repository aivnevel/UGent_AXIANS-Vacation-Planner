package models.planning;

import java.util.Arrays;

public class WorkAvailability {
    private int ftePercentage;
    private String[] workingDays;

    public WorkAvailability() {}

    public int getFtePercentage() {
        return ftePercentage;
    }

    public void setFtePercentage(int ftePercentage) {
        this.ftePercentage = ftePercentage;
    }

    public String[] getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String[] workingDays) {
        this.workingDays = workingDays;
    }

    @Override
    public String toString() {
        return "WorkAvailability{" +
                "ftePercentage=" + ftePercentage +
                ", workingDays=" + Arrays.toString(workingDays) +
                '}';
    }
}
