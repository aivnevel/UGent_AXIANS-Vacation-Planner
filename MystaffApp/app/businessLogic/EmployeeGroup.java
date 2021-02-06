package businessLogic;


import controllers.AxiansController;
import enumerations.Locations;
import enumerations.PlanningUnits;
import enumerations.Skills;
import interfaces.Employee;
import models.Member;
import models.planning.Planning;
import models.planning.PlanningMember;

import java.awt.*;

import java.util.ArrayList;
import java.util.List;

/*
 * There are two important calls provided by Axians in connection with users.
 * There is a function that returns Member objects that contain a firstName, lastName, email; general
 *      information about the person.
 * There is another function that returns PlanningMember objects, those are linked to a planning unit. If a person is
 *      linked to multiple planning units, there are multiple PlanningMember objects for the same person.
 * An EmployeeGroup contains a list of Employees. Each Employee contains the accumulative information of both Member
 *      and PlanningMember objects for each person.
 */

public class EmployeeGroup {

    private List<Employee> allEmployees;
    private Skills allSkills;
    private Locations allLocations;
    private AxiansController ac;

    /**
     * The constructor loads the Employees based on a call to Axians.
     *
     * @param ac: the AxiansController singleton.
     * @param token: the token provided by Axians
     */
    public EmployeeGroup(AxiansController ac, String token) throws Exception{
        allEmployees = new ArrayList<>();
        allSkills = Skills.getInstance();
        allLocations = Locations.getInstance();
        this.ac = ac;
        getMembersFromServer(token);
    }

    public Employee findEmployeeByID(String id) {
        boolean found = false;
        int i = -1;
        while (i < allEmployees.size() - 1 && !found) {
            i++;
            if (allEmployees.get(i).getID().equals(id)) {
                found = true;
            }
        }
        if(!found){
            return null;
        }
        return allEmployees.get(i);
    }

    public Employee findEmployeeByFullName(String fullName) {
        boolean found = false;
        int i = -1;
        while (i < allEmployees.size() - 1 && found == false) {
            i++;
            if (allEmployees.get(i).getName().equals(fullName)) {
                found = true;
            }
        }
        if (i != -1)
            return allEmployees.get(i);
        else return null;
    }

    public List<Employee> getAllEmployees() {
        return allEmployees;
    }

    public void setAllEmployees(List<Employee> allEmployees) {
        this.allEmployees = allEmployees;
    }

    public Skills getAllSkills() {
        return allSkills;
    }

    public void setAllSkills(Skills allSkills) {
        this.allSkills = allSkills;
    }

    public Locations getAllLocations() {
        return allLocations;
    }

    public void setAllLocations(Locations allLocations) {
        this.allLocations = allLocations;
    }

    /**
     * Loads the Employees based on a call to Axians.
     */
     private void getMembersFromServer(String token) throws Exception{
        for (Member member : ac.getMembersArray(token)) {
            Doctor d = new Doctor();
            d.setFirstName(member.getFirstName());
            d.setLastName(member.getLastName());
            d.setName(member.getFirstName() + " " + d.getLastName());
            d.setID(member.getId());
            d.setEmail(member.getEmail());
            allEmployees.add(d);
        }

        Planning[] plannings = ac.getOnCallPlanningsArray(token);
        PlanningUnits.getInstance().reload(ac, token);
        for (Planning planning : plannings) {
            for (PlanningMember member : planning.getPlanningMembers()) {
                Employee emp = findEmployeeByID(member.getMember());
                emp.setColor(Color.decode(member.getColour()));
                for (String skill : member.getSkills()) {
                    emp.getSkills().add(skill);
                }
                for (String location : member.getLocations())
                    emp.getLocations().add(location);
            }
        }
    }
}
