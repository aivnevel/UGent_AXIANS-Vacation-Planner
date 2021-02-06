package models.planning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import models.Member;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanningLong {

    private String tenant;
    private String id;
    private String name;
    private List<Category> categories = null;
    private List<Object> categoryShiftDemandRates = null;
    private List<Location> locations = null;
    private List<Skill> skills = null;
    private List<Shift> shifts = null;
    private List<Goal> goals = null;
    private List<PlanningMember> members = null;
    private List<Object> shiftRules = null;
    private List<Object> poolShiftRules = null;
    private List<Object> shiftSkillDemands = null;
    private List<SimultaneousShift> simultaneousShifts = null;
    private List<Object> shiftGroups = null;
    private List<ShiftSequence> shiftSequences = null;
    private List<UnwantedShiftPattern> unwantedShiftPatterns = null;
    private String historyDate;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Object> getCategoryShiftDemandRates() {
        return categoryShiftDemandRates;
    }

    public void setCategoryShiftDemandRates(List<Object> categoryShiftDemandRates) {
        this.categoryShiftDemandRates = categoryShiftDemandRates;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(List<Shift> shifts) {
        this.shifts = shifts;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }

    public List<PlanningMember> getMembers() {
        return members;
    }

    public void setMembers(List<PlanningMember> members) {
        this.members = members;
    }

    public List<Object> getShiftRules() {
        return shiftRules;
    }

    public void setShiftRules(List<Object> shiftRules) {
        this.shiftRules = shiftRules;
    }

    public List<Object> getPoolShiftRules() {
        return poolShiftRules;
    }

    public void setPoolShiftRules(List<Object> poolShiftRules) {
        this.poolShiftRules = poolShiftRules;
    }

    public List<Object> getShiftSkillDemands() {
        return shiftSkillDemands;
    }

    public void setShiftSkillDemands(List<Object> shiftSkillDemands) {
        this.shiftSkillDemands = shiftSkillDemands;
    }

    public List<SimultaneousShift> getSimultaneousShifts() {
        return simultaneousShifts;
    }

    public void setSimultaneousShifts(List<SimultaneousShift> simultaneousShifts) {
        this.simultaneousShifts = simultaneousShifts;
    }

    public List<Object> getShiftGroups() {
        return shiftGroups;
    }

    public void setShiftGroups(List<Object> shiftGroups) {
        this.shiftGroups = shiftGroups;
    }

    public List<ShiftSequence> getShiftSequences() {
        return shiftSequences;
    }

    public void setShiftSequences(List<ShiftSequence> shiftSequences) {
        this.shiftSequences = shiftSequences;
    }

    public List<UnwantedShiftPattern> getUnwantedShiftPatterns() {
        return unwantedShiftPatterns;
    }

    public void setUnwantedShiftPatterns(List<UnwantedShiftPattern> unwantedShiftPatterns) {
        this.unwantedShiftPatterns = unwantedShiftPatterns;
    }

    public String getHistoryDate() {
        return historyDate;
    }

    public void setHistoryDate(String historyDate) {
        this.historyDate = historyDate;
    }

    @Override
    public String toString() {
        return "PlanningLong{" +
                "tenant='" + tenant + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", categories=" + categories +
                ", categoryShiftDemandRates=" + categoryShiftDemandRates +
                ", locations=" + locations +
                ", skills=" + skills +
                ", shifts=" + shifts +
                ", goals=" + goals +
                ", members=" + members +
                ", shiftRules=" + shiftRules +
                ", poolShiftRules=" + poolShiftRules +
                ", shiftSkillDemands=" + shiftSkillDemands +
                ", simultaneousShifts=" + simultaneousShifts +
                ", shiftGroups=" + shiftGroups +
                ", shiftSequences=" + shiftSequences +
                ", unwantedShiftPatterns=" + unwantedShiftPatterns +
                ", historyDate='" + historyDate + '\'' +
                '}';
    }
}
