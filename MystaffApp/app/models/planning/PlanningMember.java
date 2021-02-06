package models.planning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import models.Absence;

import java.util.Arrays;
import java.util.Map;

//@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanningMember {
    private String shortName;
    private String colour;
    private String category;
    private String[] skills;
    private String[] locations;
    //TODO: periods - Not used in current API
    private Object periods;
    private Map<String, Integer> absenceRights;
    private Absence[] absences;
    private WorkAvailability workAvailability;
    private boolean usesFixedRoster;
    //TODO: roster - Is always null according to API
    private Object roster;
    private String member;
    private boolean active;

    public PlanningMember() {}

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String[] getSkills() {
        return skills;
    }

    public void setSkills(String[] skills) {
        this.skills = skills;
    }

    public String[] getLocations() {
        return locations;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public Map<String, Integer> getAbsenceRights() {
        return absenceRights;
    }

    public void setAbsenceRights(Map<String, Integer> absenceRights) {
        this.absenceRights = absenceRights;
    }

    public Absence[] getAbsences() {
        return absences;
    }

    public void setAbsences(Absence[] absences) {
        this.absences = absences;
    }

    public WorkAvailability getWorkAvailability() {
        return workAvailability;
    }

    public void setWorkAvailability(WorkAvailability workAvailability) {
        this.workAvailability = workAvailability;
    }

    public boolean isUsesFixedRoster() {
        return usesFixedRoster;
    }

    public void setUsesFixedRoster(boolean usesFixedRoster) {
        this.usesFixedRoster = usesFixedRoster;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Object getPeriods() {
        return periods;
    }

    public void setPeriods(Object periods) {
        this.periods = periods;
    }

    public Object getRoster() {
        return roster;
    }

    public void setRoster(Object roster) {
        this.roster = roster;
    }

    @Override
    public String toString() {
        return "PlanningMember{" +
                "shortName='" + shortName + '\'' +
                ", colour='" + colour + '\'' +
                ", category='" + category + '\'' +
                ", skills=" + Arrays.toString(skills) +
                ", locations=" + Arrays.toString(locations) +
                ", periods=" + periods +
                ", absenceRights=" + absenceRights +
                ", absences=" + Arrays.toString(absences) +
                ", workAvailability=" + workAvailability +
                ", usesFixedRoster=" + usesFixedRoster +
                ", roster=" + roster +
                ", member='" + member + '\'' +
                ", active=" + active +
                '}';
    }
}
