package businessLogic;

import io.ebean.Finder;
import io.ebean.Model;
import utilityClasses.HolidayMessage;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/*
 * The AbsenceCounter object is used to store the maximum number of days a person can be absent one year in the database.
 * This way, it's possible to adapt the value, for example when a the planner wants to transfer the days left for a person
 * from one year into another.
 *
 * employeeID: the ID of the employee that own's this
 *
 */

@Entity
public class AbsenceCounter extends Model {
    @Id
    private String employeeID;
    private double maxDaysThisYear;
    private Date lastUpdate;
    private String lastComment;

    public AbsenceCounter(String employeeID, double maxDaysThisYear){
        this.employeeID = employeeID;
        this.maxDaysThisYear = maxDaysThisYear;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public double getMaxDaysThisYear() {
        return maxDaysThisYear;
    }

    public void setMaxDaysThisYear(double maxDaysThisYear) {
        this.maxDaysThisYear = maxDaysThisYear;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLastComment() {
        return lastComment;
    }

    public void setLastComment(String lastComment) {
        this.lastComment = lastComment;
    }

    public static final Finder<String, AbsenceCounter> find = new Finder<>(AbsenceCounter.class);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsenceCounter that = (AbsenceCounter) o;
        return Double.compare(that.getMaxDaysThisYear(), getMaxDaysThisYear()) == 0 &&
                Objects.equals(getEmployeeID(), that.getEmployeeID()) &&
                Objects.equals(getLastUpdate(), that.getLastUpdate()) &&
                Objects.equals(getLastComment(), that.getLastComment());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmployeeID(), getMaxDaysThisYear(), getLastUpdate(), getLastComment());
    }

    @Override
    public String toString() {
        return "AbsenceCounter{" +
                "employeeID='" + employeeID + '\'' +
                ", maxDaysThisYear=" + maxDaysThisYear +
                ", lastUpdate=" + lastUpdate +
                ", lastComment='" + lastComment + '\'' +
                '}';
    }
}
