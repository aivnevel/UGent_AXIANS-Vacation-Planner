package models;

import java.util.Arrays;

public class Permissions {

    private boolean organizationAdmin;
    private String[] onDayPlannerFor;
    private String[] onCallPlannerFor;
    private String[] onDayTeamMemberIn;
    private String[] onCallTeamMemberIn;

    public Permissions() {}

    public boolean isOrganizationAdmin() {
        return organizationAdmin;
    }

    public void setOrganizationAdmin(boolean organizationAdmin) {
        this.organizationAdmin = organizationAdmin;
    }

    public String[] getOnDayPlannerFor() {
        return onDayPlannerFor;
    }

    public void setOnDayPlannerFor(String[] onDayPlannerFor) {
        this.onDayPlannerFor = onDayPlannerFor;
    }

    public String[] getOnCallPlannerFor() {
        return onCallPlannerFor;
    }

    public void setOnCallPlannerFor(String[] onCallPlannerFor) {
        this.onCallPlannerFor = onCallPlannerFor;
    }

    public String[] getOnDayTeamMemberIn() {
        return onDayTeamMemberIn;
    }

    public void setOnDayTeamMemberIn(String[] onDayTeamMemberIn) {
        this.onDayTeamMemberIn = onDayTeamMemberIn;
    }

    public String[] getOnCallTeamMemberIn() {
        return onCallTeamMemberIn;
    }

    public void setOnCallTeamMemberIn(String[] onCallTeamMemberIn) {
        this.onCallTeamMemberIn = onCallTeamMemberIn;
    }

    @Override
    public String toString() {
        return "Permissions{" +
                "organizationAdmin=" + organizationAdmin +
                ", onDayPlannerFor=" + Arrays.toString(onDayPlannerFor) +
                ", onCallPlannerFor=" + Arrays.toString(onCallPlannerFor) +
                ", onDayTeamMemberIn=" + Arrays.toString(onDayTeamMemberIn) +
                ", onCallTeamMemberIn=" + Arrays.toString(onCallTeamMemberIn) +
                '}';
    }
}
