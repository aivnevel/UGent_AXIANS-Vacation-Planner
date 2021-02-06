package interfaces;

import enumerations.HolidayType;
import enumerations.Skills;
import models.Holiday;
import utilityClasses.ExactDate;
import utilityClasses.HolidayMessage;

import java.awt.*;
import java.util.Date;
import java.util.Map;
import java.util.List;

public interface Employee {
    HolidayType getHolidayType(ExactDate exactDate, HolidayType type);
    boolean isAvailable(ExactDate exactDate);
    boolean canGoOnHolidayOn(Date date);
    boolean canGoOnHolidayOn(ExactDate exactDate);
    boolean canGoOnHolidayDuring(ExactDate exactDateStart, ExactDate exactDateEnd);
    boolean canGoOnHolidayDuring(Date DateStart, Date DateEnd);
    String getName();
    String getFirstName();
    String getLastName();
    String getID();
    List<String> getSkills();
    List<String> getLocations();
    Map<ExactDate, HolidayType> getHolidays();
    Color getColor();
    String getEmail();
    List<HolidayMessage> getHolidaysRejected();
    List<HolidayMessage> getHolidaysApproved();
    List<HolidayMessage> getHolidaysInConsidering();
    List<HolidayMessage> getHolidaysNew();
    int getDaysLeftNow();
    int getDaysLeftIfEverythingAccepted();

    void setName(String name);
    void setFirstName(String firstName);
    void setLastName(String lastName);
    void setID(String id);
    void setSkills(List<String> skills);
    void setLocations(List<String> locations);
    void setHolidays(Map<ExactDate, HolidayType> holidays);
    void setColor(Color color);
    void setEmail(String email);
    void setHolidaysRejected(List<HolidayMessage> messages);
    void setHolidaysApproved(List<HolidayMessage> messages);
    void setHolidaysInConsidering(List<HolidayMessage> messages);
    void setHolidaysNew(List<HolidayMessage> messages);
    void setDaysLeftNow(int days);
    void setDaysLeftIfEverythingAccepted(int days);
    void addHolidayMessage(HolidayMessage hm);
    void deleteHolidayMessage(HolidayMessage hm);
}
