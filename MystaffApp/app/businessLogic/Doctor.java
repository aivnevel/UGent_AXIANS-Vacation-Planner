package businessLogic;

import enumerations.HolidayType;
import enumerations.SchedulingState;
import interfaces.Employee;
import persistence.HolidayMessageController;
import utilityClasses.ExactDate;
import utilityClasses.HolidayMessage;

import java.awt.*;
import java.util.*;
import java.util.List;

/*
 * Doctors are used in an EmployeeGroup, they contain information about Member and PlanningMember objects.
 * See EmployeeGroup for more information.
 */

public class Doctor implements Employee {

    private String name;
    private String firstName;
    private String lastName;
    private String ID;
    private List<String> skills;
    private List<String> locations;
    private List<String> favorites;


    private Map<ExactDate, HolidayType> holidays;
    private Color color;
    private String email;
    private List<HolidayMessage> holidaysRejected;
    private List<HolidayMessage> holidaysApproved;
    private List<HolidayMessage> holidaysInConsidering;
    private List<HolidayMessage> holidaysNew;
    private int daysLeftNow;
    private int daysLeftEverythingAccepted;

    public Doctor(){
        skills = new ArrayList<>();
        locations = new ArrayList<>();
        holidays = new HashMap<>();
        holidaysRejected = new ArrayList<>();
        holidaysApproved = new ArrayList<>();
        holidaysInConsidering= new ArrayList<>();
        holidaysNew = new ArrayList<>();
    }

    public void loadHolidayMessages(){
        HolidayMessageController cont = new HolidayMessageController();
        holidaysRejected = cont.getHolidayMessagesRejectedOfDoctorWithID(ID);
        holidaysInConsidering = cont.getHolidayMessagesInConsiderationOfDoctorWithID(ID);
        holidaysApproved = cont.getHolidayMessagesApprovedOfDoctorWithID(ID);
        for(HolidayMessage hm: holidaysApproved){
            System.out.println(hm);
        }
        holidaysNew = cont.getHolidayMessagesNewOfDoctorWithID(ID);
    }

    @Override
    public HolidayType getHolidayType(ExactDate exactDate, HolidayType type) {
        return null;
    }

    @Override
    public boolean isAvailable(ExactDate exactDate) {
        return false;
    }

    @Override
    public boolean canGoOnHolidayOn(Date date) {
        return false;
    }

    @Override
    public boolean canGoOnHolidayOn(ExactDate exactDate) {
        return false;
    }

    @Override
    public boolean canGoOnHolidayDuring(ExactDate exactDateStart, ExactDate exactDateEnd) {
        return false;
    }

    @Override
    public boolean canGoOnHolidayDuring(Date DateStart, Date DateEnd) {
        return false;
    }

    @Override
    public List<String> getSkills() {
        return skills;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public Map<ExactDate, HolidayType> getHolidays() {
        return holidays;
    }

    public void setHolidays(Map<ExactDate, HolidayType> holidays) {
        this.holidays = holidays;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<HolidayMessage> getHolidaysRejected() {
        return holidaysRejected;
    }

    public void setHolidaysRejected(List<HolidayMessage> holidaysRejected) {
        this.holidaysRejected = holidaysRejected;
    }

    public List<HolidayMessage> getHolidaysApproved() {
        return holidaysApproved;
    }

    public void setHolidaysApproved(List<HolidayMessage> holidaysApproved) {
        this.holidaysApproved = holidaysApproved;
    }

    public List<HolidayMessage> getHolidaysInConsidering() {
        return holidaysInConsidering;
    }

    public void setHolidaysInConsidering(List<HolidayMessage> holidaysInConsidering) {
        this.holidaysInConsidering = holidaysInConsidering;
    }

    public List<HolidayMessage> getHolidaysNew() {
        return holidaysNew;
    }

    public void setHolidaysNew(List<HolidayMessage> holidaysNew) {
        this.holidaysNew = holidaysNew;
    }

    public int getDaysLeftNow() {
        return daysLeftNow;
    }

    @Override
    public int getDaysLeftIfEverythingAccepted() {
        return daysLeftEverythingAccepted;
    }

    public void setDaysLeftNow(int daysLeftNow) {
        this.daysLeftNow = daysLeftNow;
    }

    @Override
    public void setDaysLeftIfEverythingAccepted(int days) {
        daysLeftEverythingAccepted = days;
    }

    public int getDaysLeftEverythingAccepted() {
        return daysLeftEverythingAccepted;
    }

    public void setDaysLeftEverythingAccepted(int daysLeftEverythingAccepted) {
        this.daysLeftEverythingAccepted = daysLeftEverythingAccepted;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    @Override
    public String toString() {
        return
                "ID: " + ID + "\n" +
                "name: " + name + "\n" +
                "skills: " + skills.toString() + "\n" +
                "locations: " + locations.toString() + "\n" +
                "holidays: " + holidays.toString() + "\n" +
                "color: " + color + "\n" +
                "email: " + email + "\n" +
                "holidaysRejected: " + holidaysRejected.toString() + "\n" +
                "holidaysApproved: " + holidaysApproved.toString() + "\n" +
                "holidaysInConsidering: " + holidaysInConsidering.toString() + "\n" +
                "holidaysNew: " + holidaysNew.toString() + "\n" +
                "daysLefNow: " + daysLeftNow + "\n" +
                "daysLeftEverythingAccepted: " + daysLeftEverythingAccepted + "\n";
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }

    public void addHolidayMessage(HolidayMessage hm){
        if(hm.getState() == SchedulingState.Approved){
            getHolidaysApproved().add(hm);
        }
        else if(hm.getState() == SchedulingState.Rejected){
            getHolidaysRejected().add(hm);
        }
        else if(hm.getState() == SchedulingState.InConsideration){
            getHolidaysInConsidering().add(hm);
        }
        else if(hm.getState() == SchedulingState.New){
            getHolidaysNew().add(hm);
        }
    }

    public void deleteHolidayMessage(HolidayMessage hm){
        if(hm.getState() == SchedulingState.Approved){
            getHolidaysApproved().remove(hm);
        }
        else if(hm.getState() == SchedulingState.Rejected){
            getHolidaysRejected().remove(hm);
        }
        else if(hm.getState() == SchedulingState.InConsideration){
            getHolidaysInConsidering().remove(hm);
        }
        else if(hm.getState() == SchedulingState.New){
            getHolidaysNew().remove(hm);
        }
    }


}
