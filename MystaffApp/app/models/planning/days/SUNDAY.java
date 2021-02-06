package models.planning.days;

import java.util.List;

public class SUNDAY implements WeekDay  {

    private Integer number;
    private Integer freePeriodDays;
    private List<String> requiredSkills = null;
    private List<String> alternativeSkills = null;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getFreePeriodDays() {
        return freePeriodDays;
    }

    public void setFreePeriodDays(Integer freePeriodDays) {
        this.freePeriodDays = freePeriodDays;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<String> getAlternativeSkills() {
        return alternativeSkills;
    }

    public void setAlternativeSkills(List<String> alternativeSkills) {
        this.alternativeSkills = alternativeSkills;
    }

    @Override
    public String toString() {
        return "SUNDAY{" +
                "number=" + number +
                ", freePeriodDays=" + freePeriodDays +
                ", requiredSkills=" + requiredSkills +
                ", alternativeSkills=" + alternativeSkills +
                '}';
    }
}