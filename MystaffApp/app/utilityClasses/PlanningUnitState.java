package utilityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enumerations.SchedulingState;
import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name = "planning_unit_state")
/*
 * A HolidayMessage can only be approved if it is approved by a planner of every planning unit the employee is part of.
 * Therefore, the state for every planning unit should be saved in a HolidayMessage.
 * That is what this object does.
 * It is linked to a certain HM and contains it's own ID, the ID of the planning unit, a comment that the planner can give
 *      when he rejects a message, the state (approved, rejected, ...) and the ID of the planner that gave this decision.
 *
 */
public class PlanningUnitState extends Model {
    @JsonIgnore
    @ManyToOne//(cascade = CascadeType.ALL)
    @JoinColumn(name="holiday_message_id", nullable = false)//(name="holiday_ message_id")
    private HolidayMessage holidayMessage;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String unitId;
    private String comment;
    private SchedulingState state;
    private String plannerID;


    public HolidayMessage getHolidayMessage() {
        return holidayMessage;
    }

    public void setHolidayMessage(HolidayMessage holidayMessage) {
        this.holidayMessage = holidayMessage;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
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

    public static final Finder<Long, PlanningUnitState> find = new Finder<>(PlanningUnitState.class);


    @Override
    public String toString() {
        return "PlanningUnitState{" +
                "holidayMessage=" + holidayMessage +
                ", id='" + id + '\'' +
                ", comment='" + comment + '\'' +
                ", state=" + state +
                ", planner='" + plannerID + '\'' +
                '}';
    }
}
