package utilityClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enumerations.DayPart;
import io.ebean.Finder;
import org.springframework.beans.factory.annotation.Autowired;
import play.data.validation.Constraints;

import io.ebean.Model;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/*
 An ExactDate contains a date and a daypart
 The date is actually a LocalDate and contains only day, month and year, not minutes and hours.
 It is linked only to HolidayMessages.
 See the ExactDateHMH wich is only linked to HolidayMessageHistory objects.
 The fact that it is a seperate object, is because of the limitations of the ORM.
 We tried to make an interface "ExactDateHolder" with only a getExactDates, to link it here as a holidayMessage
 but Ebean did not like that.
*/

@Entity
@Table(name = "exact_date")
public class ExactDate extends Model implements Comparable<ExactDate> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    @Constraints.Required
    private LocalDate date;
    @Constraints.Required
    private DayPart daypart;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="holiday_message_id", nullable = false)
    private HolidayMessage holidayMessage;


    public HolidayMessage getHolidayMessage() {
        return holidayMessage;
    }

    public void setHolidayMessage(HolidayMessage holidayMessage) {
        this.holidayMessage = holidayMessage;
    }

    public ExactDate(LocalDate date, DayPart daypart){
        this.date = date;
        this.daypart = daypart;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public DayPart getDaypart() {
        return daypart;
    }

    public void setDaypart(DayPart daypart) {
        this.daypart = daypart;
    }

    public long getId() {
        return id;
    }

    public static final Finder<Long, ExactDate> find = new Finder<>(ExactDate.class);

    public boolean isBefore(ExactDate other){
        if(this.getDate().isBefore(other.getDate())){
            return true;
        }
        if(this.getDate().isEqual(other.getDate())){
            if(this.getDaypart().equals(DayPart.AM) && other.getDaypart().equals(DayPart.PM)){
                return true;
            }
        }
        return false;
    }

    public boolean isAfter(ExactDate other){
        if(this.getDate().isAfter(other.getDate())){
            return true;
        }
        if(this.getDate().isEqual(other.getDate())){
            if(this.getDaypart().equals(DayPart.PM) && other.getDaypart().equals(DayPart.AM)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExactDate exactDate = (ExactDate) o;
        return  getDate().equals(exactDate.getDate()) &&
                getDaypart() == exactDate.getDaypart();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getDaypart());
    }

    @Override
    public String toString() {
        return "ExactDate{" +
                "id=" + id +
                ", date=" + date +
                ", daypart=" + daypart +
                '}';
    }

    @Override
    public int compareTo(ExactDate other){
        if(other.isBefore(this)) return 1;
        if(other.isAfter(this)) return -1;
        return 0;
    }
}
