package businessLogic;

import models.planning.PlanningMember;
import models.planning.Shift;
import utilityClasses.ExactDate;

import java.util.Objects;

/*
 * A SchedulingPerson is used by the algorithms that check if a planning can be made in ScheduleHelper.
 * To do make a planning, a list of PlanningMembers has to be kept up together with the shift they are planned in and
 * the day on which they are planned.
 * A SchedulingPerson is a collection of these variables.
 */

public class SchedulingPerson{
    private Shift shift;
    private ExactDate ed;
    private PlanningMember pl;

    public SchedulingPerson(Shift shift, ExactDate ed, PlanningMember pl){
        this.shift = shift;
        this.ed = ed;
        this.pl = pl;
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

    public PlanningMember getPl() {
        return pl;
    }

    public void setPl(PlanningMember pl) {
        this.pl = pl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingPerson that = (SchedulingPerson) o;
        return Objects.equals(getShift(), that.getShift()) &&
                Objects.equals(getEd(), that.getEd()) &&
                Objects.equals(getPl(), that.getPl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getShift(), getEd(), getPl());
    }

    @Override
    public String toString() {
        return "SchedulingPerson{" +
                "shift=" + shift +
                ", ed=" + ed +
                ", pl=" + pl +
                '}';
    }
}