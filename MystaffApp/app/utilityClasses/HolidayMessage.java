package utilityClasses;

import enumerations.DayPart;
import enumerations.HolidayType;
import enumerations.SchedulingState;
import io.ebean.Finder;
import io.ebean.Model;
import models.Holiday;
import org.springframework.beans.factory.annotation.Autowired;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

/*
HolidayMessages are the most important objects of this backend.
They are the communicationObject between back and front end.

The flow goes like this:
An employee asks for an absence at the frontend.
An employee is part of multiple planning units.
The planners can now check if it is possible to approve the absence.
The state of the absence stays "New" until one planner of each planning unit has approved it.
If a planner would reject it, the global state changes to rejected too.
If the absence reaches a final state (approved or rejected), the employee gets an email.

The employee can also adapt his original absencerequest at the frontend.
He can take a day extra etc.
When he does this, a HolidayMessageHistory (HMH) object is added to the HolidayMessage.
The HMH contains the old information of the absencerequest.
The states of the planners are now reset and they have to approve everything again.

They store all absences of every doctor and every aspect they must contain.
They store the employeeID of the employee that wants an absence
           the dates the employee wants to be absent
           the type of absence (European, Yearly, Sickness, Other, ...)
           the state of the absence (Approved, Rejected, In consideration, new)
                It is calculated like this:
                    If at least one planner rejected the absence, the global state is rejected
                    If not: if at least one planner has it new: the state is new
                    If not: if at least one planner has it in consideration: that is the state
                    IF not: state is approved
           the request date (the date which the employee asked for his absence)
           the date of the last update by a planner
           the requestByID, normally this is the employeeID of the employee that wants an absence
                but, an absence can also be requested by a planner, par example. In that case, the requestByID
                is the plannerID.
           the plannerOfLastUpdate is the ID of the planner that did the lastUpdate.
                is default null.
           the planningUnitStates are tricky. When the employee first requests his absence, this field is empty.
                Then, de backend checks which planning units this employee is part of. It then makes a planningUnitState
                for each planning unit with the correct ID, empty state, plannerID en comment.
                When a planner of specific planning unit (note: can be more than one!) gives a new state, the correct
                objects here are filled with the new state, the plannerID and the comment.
           the comment is the comment of the employee himself.
           the history contains a list of old versions of this holidayMessage, Applicable when employee changes his mind
*/

@Entity
@Table(name = "holiday_message")
public class HolidayMessage extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    @Constraints.Required
    private String employeeID;
    @OneToMany(mappedBy ="holidayMessage", cascade = CascadeType.REMOVE)
    private List<ExactDate> exactDates;
    @Constraints.Required
    private HolidayType type;
    @Constraints.Required
    private SchedulingState state;
    @Constraints.Required
    private Date requestDate;
    @Constraints.Required
    private Date lastUpdate;
    @Constraints.Required
    private String requestByID;
    private String plannerOfLastUpdate;
    @Constraints.Required
    @OneToMany(mappedBy ="holidayMessage", cascade = CascadeType.REMOVE)
    private List<PlanningUnitState> planningUnitStates;
    private String comment;
    @Constraints.Required
    @OneToMany(mappedBy ="holidayMessage", cascade = CascadeType.REMOVE)
    private List<HolidayMessageHistory> history = new ArrayList<>();

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public List<ExactDate> getExactDates() {
        return exactDates;
    }

    public void setExactDates(List<ExactDate> exactDates) {
        this.exactDates = exactDates;
    }

    public HolidayType getType() {
        return type;
    }

    public void setType(HolidayType type) {
        this.type = type;
    }

    public SchedulingState getState() {
        return state;
    }

    public void setState(SchedulingState state) {
        this.state = state;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getRequestByID() {
        return requestByID;
    }

    public void setRequestByID(String requestByID) {
        this.requestByID = requestByID;
    }

    public static final Finder<Long, HolidayMessage> find = new Finder<>(HolidayMessage.class);

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "HolidayMessage{" +
                "id=" + id +
                ", employeeID='" + employeeID + '\'' +
                ", exactDates=" + exactDates +
                ", type=" + type +
                ", state=" + state +
                ", requestDate=" + requestDate +
                ", requestByID='" + requestByID + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HolidayMessage that = (HolidayMessage) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
        System.out.println("-------"+this.lastUpdate);
    }

    public String getPlannerOfLastUpdate() {
        return plannerOfLastUpdate;
    }

    public void setPlannerOfLastUpdate(String plannerOfLastUpdate) {
        this.plannerOfLastUpdate = plannerOfLastUpdate;
    }

    public List<PlanningUnitState> getPlanningUnitStates() {
        return planningUnitStates;
    }

    public void setPlanningUnitStates(List<PlanningUnitState> planningUnitStates) {
        this.planningUnitStates = planningUnitStates;
    }

    public List<HolidayMessageHistory> getHistory() {
        return history;
    }

    public void setHistory(List<HolidayMessageHistory> history) {
        this.history = history;
    }
}
