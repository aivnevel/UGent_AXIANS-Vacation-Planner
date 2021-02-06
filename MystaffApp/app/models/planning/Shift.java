package models.planning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;

public class Shift {

    private String id;
    private String name;
    private String location;
    private Demands demands;
    private Boolean standby;
    private Boolean manuallyAssigned;
    private String type;
    private String prefix;
    private Integer idx;
    private List<Object> assignmentCounters = null;
    private Boolean useFteCountAsDefault;
    private Boolean requiresMember;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Demands getDemands() {
        return demands;
    }

    public void setDemands(Demands demands) {
        this.demands = demands;
    }

    public Boolean getStandby() {
        return standby;
    }

    public void setStandby(Boolean standby) {
        this.standby = standby;
    }

    public Boolean getManuallyAssigned() {
        return manuallyAssigned;
    }

    public void setManuallyAssigned(Boolean manuallyAssigned) {
        this.manuallyAssigned = manuallyAssigned;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public List<Object> getAssignmentCounters() {
        return assignmentCounters;
    }

    public void setAssignmentCounters(List<Object> assignmentCounters) {
        this.assignmentCounters = assignmentCounters;
    }

    public Boolean getUseFteCountAsDefault() {
        return useFteCountAsDefault;
    }

    public void setUseFteCountAsDefault(Boolean useFteCountAsDefault) {
        this.useFteCountAsDefault = useFteCountAsDefault;
    }

    public Boolean getRequiresMember() {
        return requiresMember;
    }

    public void setRequiresMember(Boolean requiresMember) {
        this.requiresMember = requiresMember;
    }

    @Override
    public String toString() {
        return "Shift{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", demands=" + demands +
                ", standby=" + standby +
                ", manuallyAssigned=" + manuallyAssigned +
                ", type='" + type + '\'' +
                ", prefix='" + prefix + '\'' +
                ", idx=" + idx +
                ", assignmentCounters=" + assignmentCounters +
                ", useFteCountAsDefault=" + useFteCountAsDefault +
                ", requiresMember=" + requiresMember +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shift shift = (Shift) o;
        return getId().equals(shift.getId()) &&
                Objects.equals(getName(), shift.getName()) &&
                Objects.equals(getLocation(), shift.getLocation()) &&
                Objects.equals(getDemands(), shift.getDemands()) &&
                Objects.equals(getStandby(), shift.getStandby()) &&
                Objects.equals(getManuallyAssigned(), shift.getManuallyAssigned()) &&
                Objects.equals(getType(), shift.getType()) &&
                Objects.equals(getPrefix(), shift.getPrefix()) &&
                Objects.equals(getIdx(), shift.getIdx()) &&
                Objects.equals(getAssignmentCounters(), shift.getAssignmentCounters()) &&
                Objects.equals(getUseFteCountAsDefault(), shift.getUseFteCountAsDefault()) &&
                getRequiresMember().equals(shift.getRequiresMember());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getLocation(), getDemands(), getStandby(), getManuallyAssigned(), getType(), getPrefix(), getIdx(), getAssignmentCounters(), getUseFteCountAsDefault(), getRequiresMember());
    }
}
