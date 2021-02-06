package utilityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * This object is a presentation of a previous state a HolidayMessage was in.
 * When an employee adapts an absence in his calendar, the old state will be put into a HolidayMessageHistory object and
 *      the new state will now be in the holidayMessage that has a link to the newly created HMH object.
 * For more info about the flow, see HolidayMessage.
 */

@Entity
@Table(name = "holiday_message_history")
public class HolidayMessageHistory extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    @OneToMany(mappedBy ="holidayMessage", cascade = CascadeType.REMOVE)
    private List<ExactDateHMH> exactDates;
    @Constraints.Required
    private HolidayType type;
    @Constraints.Required
    private SchedulingState state;
    @Constraints.Required
    private Date requestDate;
    @Constraints.Required
    private String requestByID;
    private String comment;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="holiday_message_id", nullable = false)
    private HolidayMessage holidayMessage;

    public List<ExactDateHMH> getExactDates() {
        return exactDates;
    }

    public void setExactDates(List<ExactDateHMH> exactDates) {
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

    public static final Finder<Long, HolidayMessageHistory> find = new Finder<>(HolidayMessageHistory.class);

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "HolidayMessage{" +
                "id=" + id +
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

    public HolidayMessage getHolidayMessage() {
        return holidayMessage;
    }

    public void setHolidayMessage(HolidayMessage holidayMessage) {
        this.holidayMessage = holidayMessage;
    }
}
