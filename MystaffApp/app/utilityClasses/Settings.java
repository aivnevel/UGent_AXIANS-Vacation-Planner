package utilityClasses;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/*
 * The Settings object is used at the front end to save the applied filters and options a person has done.
 * It has the following fields:
 *      employeeID: the ID of the employee that's owner of these settings.
 *      requestFilters: a boolean that says whether the filters should be applied to the requests or not.
 *      satOn: a string that says whether the planner wants planner mode or not
 *             (had to be sortOn, was a typo, and is then used in an other way)
 *      period: a list containing all selected periods in the filters of the planner tool.
 *      function: a list containing all selected functions (skills) in the filters of the planner tool.
 *      dayPart: the selected dayPart in the filter of the planner tool.
 *      location: a list containing all selected locations in the filters of the planner tool.
 *      favorites: a list containing all selected favorite people in the filters of the planner tool.
 *      comments: a list that is used to sort by requests that have comments.
 *      absenceType: a list containing all selected absence types in the filters of the planner tool.
 */

@Entity
@Table(name = "settings")
public class Settings extends Model {
    @Id
    public String employeeID;
    private boolean requestFilters;
    private String satOn;
    @ElementCollection
    @Column(name="period")
    private List<String> period;
    @ElementCollection
    @Column(name="function")
    private List<String> function;
    private String dayPart;
    @ElementCollection
    @Column(name="location")
    private List<String> location;
    @ElementCollection
    @Column(name="favorites")
    private List<String> favorites;
    @ElementCollection
    @Column(name="comments")
    private List<String> comments;
    @ElementCollection
    @Column(name="absenceType")
    private List<String> absenceType;

    public boolean isRequestFilters() {
        return requestFilters;
    }

    public void setRequestFilters(boolean requestFilters) {
        this.requestFilters = requestFilters;
    }

    public String getSatOn() {
        return satOn;
    }

    public void setSatOn(String satOn) {
        this.satOn = satOn;
    }

    public List<String> getPeriod() {
        return period;
    }

    public void setPeriod(List<String> period) {
        this.period = period;
    }

    public List<String> getFunction() {
        return function;
    }

    public void setFunction(List<String> function) {
        this.function = function;
    }

    public String getDayPart() {
        return dayPart;
    }

    public void setDayPart(String dayPart) {
        this.dayPart = dayPart;
    }

    public List<String> getLocation() {
        return location;
    }

    public void setLocation(List<String> location) {
        this.location = location;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public List<String> getAbsenceType() {
        return absenceType;
    }

    public void setAbsenceType(List<String> absenceType) {
        this.absenceType = absenceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return isRequestFilters() == settings.isRequestFilters() &&
                Objects.equals(employeeID, settings.employeeID) &&
                Objects.equals(getSatOn(), settings.getSatOn()) &&
                Objects.equals(getPeriod(), settings.getPeriod()) &&
                Objects.equals(getFunction(), settings.getFunction()) &&
                Objects.equals(getDayPart(), settings.getDayPart()) &&
                Objects.equals(getLocation(), settings.getLocation()) &&
                Objects.equals(getFavorites(), settings.getFavorites()) &&
                Objects.equals(getComments(), settings.getComments()) &&
                Objects.equals(getAbsenceType(), settings.getAbsenceType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeID, isRequestFilters(), getSatOn(), getPeriod(), getFunction(), getDayPart(), getLocation(), getFavorites(), getComments(), getAbsenceType());
    }

    @Override
    public String toString() {
        return "Settings{" +
                "employeeID='" + employeeID + '\'' +
                ", requestFilters=" + requestFilters +
                ", satOn='" + satOn + '\'' +
                ", period=" + period +
                ", function=" + function +
                ", dayPart='" + dayPart + '\'' +
                ", location=" + location +
                ", favorites=" + favorites +
                ", comments=" + comments +
                ", absenceType=" + absenceType +
                '}';
    }

    public static final Finder<String, Settings> find = new Finder<>(Settings.class);
}
