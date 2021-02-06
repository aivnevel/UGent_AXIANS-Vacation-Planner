package utilityClasses;

import enumerations.SchedulingState;

/*
 * This object is used to communicate a decision of one planner.
 *
 * An absence can only be globally approved if one planner of every planning unit a person is in has approved the absence.
 * This object contains the decision of one planner in a compact way:
 *      - id: the id of the HolidayMessage that is linked to this object.
 *      - comment: the comment of the planner on this absence. Should only be filled in if the absence is rejected.
 *      - state: the decision the planner made: approved, rejected or in consideration.
 *      - plannerID: the memberID of the planner that made this decision.
 * When this message is send to the backend, the back end should check of which planning units the planner is planner of and set
 * the PlanningUnitStates of the HolidayMessages accordingly.
 * e.g. When the planner is planner of two planning units, the planningUnitStates of those planning units will be filled
 * in with this decision for the according HolidayMessage.
 *
 */

public class HolidayPlanningsMessage {
    public long id;
    private String comment;
    private SchedulingState state;
    private String plannerID;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public SchedulingState getState() {
        return state;
    }

    public void setState(SchedulingState state) {
        this.state = state;
    }

    public String getPlannerID() {
        return plannerID;
    }

    public void setPlannerID(String plannerID) {
        this.plannerID = plannerID;
    }

    @Override
    public String toString() {
        return "HolidayPlanningsMessage{" +
                "id=" + id +
                ", comment='" + comment + '\'' +
                ", state=" + state +
                ", plannerID='" + plannerID + '\'' +
                '}';
    }
}
