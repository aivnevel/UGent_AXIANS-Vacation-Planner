package models;

import java.util.HashMap;
import java.util.Map;

public class PlanningUnitShort {

    private String name;
    private String id;
    private Boolean activated;
    private Integer numberOfOnCallPlanners;
    private Integer numberOfOnDayPlanners;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Integer getNumberOfOnCallPlanners() {
        return numberOfOnCallPlanners;
    }

    public void setNumberOfOnCallPlanners(Integer numberOfOnCallPlanners) {
        this.numberOfOnCallPlanners = numberOfOnCallPlanners;
    }

    public Integer getNumberOfOnDayPlanners() {
        return numberOfOnDayPlanners;
    }

    public void setNumberOfOnDayPlanners(Integer numberOfOnDayPlanners) {
        this.numberOfOnDayPlanners = numberOfOnDayPlanners;
    }

}