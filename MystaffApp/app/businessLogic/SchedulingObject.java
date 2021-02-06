package businessLogic;

import models.planning.Shift;
import utilityClasses.ExactDate;

import java.util.Objects;

/*
 * A SchedulingObject is used in the same places as SchedulingPerson.
 * To make a planning, certain constraints have to be tested. To test if a certain person can be planned in, we need
 * the information about in which shifts that person is planned in on which days. This is what this object presents.
 * Because the constraints only apply to one person at a time, the PlanningMember object does not need to be stocked
 * in here.
 */
public class SchedulingObject {
    private Shift shift;
    private ExactDate ed;

    public SchedulingObject(Shift shift, ExactDate ed){
        this.shift = shift;
        this.ed = ed;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public ExactDate getEd() {
        return ed;
    }

    public void setEd(ExactDate ed) {
        this.ed = ed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingObject that = (SchedulingObject) o;
        return Objects.equals(getShift(), that.getShift()) &&
                Objects.equals(getEd(), that.getEd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getShift(), getEd());
    }

    @Override
    public String toString() {
        return "SchedulingObject{" +
                "shift=" + shift +
                ", ed=" + ed +
                '}';
    }
}
