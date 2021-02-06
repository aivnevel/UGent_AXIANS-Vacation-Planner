package models.planning.days;

import java.util.List;

public interface WeekDay {

    public Integer getNumber();

    public void setNumber(Integer number);

    public Integer getFreePeriodDays();

    public void setFreePeriodDays(Integer freePeriodDays);

    public List<String> getRequiredSkills();

    public void setRequiredSkills(List<String> requiredSkills);

    public List<String> getAlternativeSkills();

    public void setAlternativeSkills(List<String> alternativeSkills);

}