package models;

import java.util.List;

public class PlanningUnit {

    private String name;
    private String tenant;
    private String id;
    private List<String> onDayPlanners = null;
    private List<String> onCallPlanners = null;
    private Boolean activated;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getOnDayPlanners() {
        return onDayPlanners;
    }

    public void setOnDayPlanners(List<String> onDayPlanners) {
        this.onDayPlanners = onDayPlanners;
    }

    public List<String> getOnCallPlanners() {
        return onCallPlanners;
    }

    public void setOnCallPlanners(List<String> onCallPlanners) {
        this.onCallPlanners = onCallPlanners;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

}