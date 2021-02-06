package businessLogic;

import utilityClasses.ExactDate;

import java.util.List;
import java.util.Objects;

/*
 * A Person Date object contains a person and a date.
 * It is used to represent a HolidayMessage with the information that is needed to make a valid planning. This includes
 * only the ID of the employee and the dates this person wants to take a leave.
 */
public class PersonDate implements Comparable<PersonDate>{
    private long id;
    private String employeeID;
    private List<ExactDate> dates;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public List<ExactDate> getDates() {
        return dates;
    }

    public void setDates(List<ExactDate> dates) {
        this.dates = dates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonDate that = (PersonDate) o;
        return Objects.equals(getEmployeeID(), that.getEmployeeID()) &&
                Objects.equals(getDates(), that.getDates()) &&
                Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmployeeID(), getDates());
    }

    @Override
    public String toString() {
        return "PersonDate{" +
                "id=" + id + '\'' +
                "employeeID=" + employeeID + '\'' +
                ", date=" + dates +
                '}';
    }

    @Override
    public int compareTo(PersonDate other){
        if(other.getDates().get(0).isBefore(getDates().get(0))) return 1;
        if(other.getDates().get(0).isAfter(getDates().get(0))) return -1;
        return 0;
    }
}
