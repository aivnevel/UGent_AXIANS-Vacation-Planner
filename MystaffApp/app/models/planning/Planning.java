package models.planning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Planning {
    @JsonProperty("tenant")
    private String tenant;
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("categories")
    private List<Category> categories = null;
    @JsonProperty("categoryShiftDemandRates")
    private List<Object> categoryShiftDemandRates = null;
    @JsonProperty("locations")
    private List<Location> locations = null;
    @JsonProperty("skills")
    private List<Skill> skills = null;
    @JsonProperty("shifts")
    private List<Shift> shifts = null;
    @JsonProperty("goals")
    private List<Goal> goals = null;
    @JsonProperty("members")
    private List<PlanningMember> members = null;
    @JsonProperty("shiftRules")
    private List<ShiftRules> shiftRules = null;
    @JsonProperty("poolShiftRules")
    private List<Object> poolShiftRules = null;
    @JsonProperty("shiftSkillDemands")
    private List<Object> shiftSkillDemands = null;
    @JsonProperty("simultaneousShifts")
    private List<SimultaneousShift> simultaneousShifts = null;
    @JsonProperty("shiftGroups")
    private List<Object> shiftGroups = null;
    @JsonProperty("shiftSequences")
    private List<ShiftSequence> shiftSequences = null;
    @JsonProperty("unwantedShiftPatterns")
    private List<UnwantedShiftPattern> unwantedShiftPatterns = null;
    @JsonProperty("historyDate")
    private String historyDate;

    @JsonProperty("tenant")
    public String getTenant() {
        return tenant;
    }

    @JsonProperty("tenant")
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("categories")
    public List<Category> getCategories() {
        return categories;
    }

    @JsonProperty("categories")
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    @JsonProperty("categoryShiftDemandRates")
    public List<Object> getCategoryShiftDemandRates() {
        return categoryShiftDemandRates;
    }

    @JsonProperty("categoryShiftDemandRates")
    public void setCategoryShiftDemandRates(List<Object> categoryShiftDemandRates) {
        this.categoryShiftDemandRates = categoryShiftDemandRates;
    }

    @JsonProperty("locations")
    public List<Location> getLocations() {
        return locations;
    }

    @JsonProperty("locations")
    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @JsonProperty("skills")
    public List<Skill> getSkills() {
        return skills;
    }

    @JsonProperty("skills")
    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    @JsonProperty("shifts")
    public List<Shift> getShifts() {
        return shifts;
    }

    @JsonProperty("shifts")
    public void setShifts(List<Shift> shifts) {
        this.shifts = shifts;
    }

    @JsonProperty("goals")
    public List<Goal> getGoals() {
        return goals;
    }

    @JsonProperty("goals")
    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }

    @JsonProperty("members")
    public List<PlanningMember> getPlanningMembers() {
        return members;
    }

    @JsonProperty("members")
    public void setPlanningMembers(List<PlanningMember> members) {
        this.members = members;
    }

    @JsonProperty("shiftRules")
    public List<ShiftRules> getShiftRules() {
        return shiftRules;
    }

    @JsonProperty("shiftRules")
    public void setShiftRules(List<ShiftRules> shiftRules) {
        this.shiftRules = shiftRules;
    }

    @JsonProperty("poolShiftRules")
    public List<Object> getPoolShiftRules() {
        return poolShiftRules;
    }

    @JsonProperty("poolShiftRules")
    public void setPoolShiftRules(List<Object> poolShiftRules) {
        this.poolShiftRules = poolShiftRules;
    }

    @JsonProperty("shiftSkillDemands")
    public List<Object> getShiftSkillDemands() {
        return shiftSkillDemands;
    }

    @JsonProperty("shiftSkillDemands")
    public void setShiftSkillDemands(List<Object> shiftSkillDemands) {
        this.shiftSkillDemands = shiftSkillDemands;
    }

    @JsonProperty("simultaneousShifts")
    public List<SimultaneousShift> getSimultaneousShifts() {
        return simultaneousShifts;
    }

    @JsonProperty("simultaneousShifts")
    public void setSimultaneousShifts(List<SimultaneousShift> simultaneousShifts) {
        this.simultaneousShifts = simultaneousShifts;
    }

    @JsonProperty("shiftGroups")
    public List<Object> getShiftGroups() {
        return shiftGroups;
    }

    @JsonProperty("shiftGroups")
    public void setShiftGroups(List<Object> shiftGroups) {
        this.shiftGroups = shiftGroups;
    }

    @JsonProperty("shiftSequences")
    public List<ShiftSequence> getShiftSequences() {
        return shiftSequences;
    }

    @JsonProperty("shiftSequences")
    public void setShiftSequences(List<ShiftSequence> shiftSequences) {
        this.shiftSequences = shiftSequences;
    }

    @JsonProperty("unwantedShiftPatterns")
    public List<UnwantedShiftPattern> getUnwantedShiftPatterns() {
        return unwantedShiftPatterns;
    }

    @JsonProperty("unwantedShiftPatterns")
    public void setUnwantedShiftPatterns(List<UnwantedShiftPattern> unwantedShiftPatterns) {
        this.unwantedShiftPatterns = unwantedShiftPatterns;
    }

    @JsonProperty("historyDate")
    public String getHistoryDate() {
        return historyDate;
    }

    @JsonProperty("historyDate")
    public void setHistoryDate(String historyDate) {
        this.historyDate = historyDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tenant", tenant).append("id", id).append("name", name).append("categories", categories).append("categoryShiftDemandRates", categoryShiftDemandRates).append("locations", locations).append("skills", skills).append("shifts", shifts).append("goals", goals).append("members", members).append("shiftRules", shiftRules).append("poolShiftRules", poolShiftRules).append("shiftSkillDemands", shiftSkillDemands).append("simultaneousShifts", simultaneousShifts).append("shiftGroups", shiftGroups).append("shiftSequences", shiftSequences).append("unwantedShiftPatterns", unwantedShiftPatterns).append("historyDate", historyDate).toString();
    }

}

