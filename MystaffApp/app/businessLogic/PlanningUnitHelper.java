package businessLogic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.AxiansController;
import enumerations.PlanningUnits;
import models.Member;
import models.PlanningUnit;
import models.planning.Planning;
import models.planning.PlanningLong;
import models.planning.PlanningMember;
import play.mvc.Controller;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class PlanningUnitHelper{

    private AxiansController ac;

    @Inject
    public PlanningUnitHelper(AxiansController ac){
        this.ac = ac;
    }


    /**
     * This function returns the ID's of all planning units an employee is part of.
     *
     * @param employeeID: the id of the employee for which we want to know their planning units.
     * @param plannings: list of plannings. This is done to save some calls to Axians.
     *
     * @return
     * [
     *      id,
     *      ...,
     *      id
     * ]
     */
    public static List<String> getAllPlanningUnitIDsInWhichEmployeeIDIsIn(String employeeID, Planning[] plannings){
        ArrayList<String> list = new ArrayList<>();
        for(Planning planning: plannings){
            boolean found = false;
            int i = 0;
            while(found == false && i < planning.getPlanningMembers().size()){
                if(planning.getPlanningMembers().get(i).getMember().equals(employeeID)){
                    list.add(planning.getId());
                }
                i++;
            }
        }
        return list;
    }


    /**
     * This function returns the ID's of all planning units an employee is part of.
     *
     * @param employeeID: the id of the employee for which we want to know their planning units.
     * @param token: the identification token of the Axians web API.
     *
     * @return
     * [
     *      id,
     *      ...,
     *      id
     * ]
     */
    public List<String> getAllPlanningUnitIDsInWhichEmployeeIDIsIn(String token, String employeeID) throws Exception{
        ArrayList<String> list = new ArrayList<>();
        Planning[] plannings = ac.getOnCallPlanningsArray(token);
        for(Planning planning: plannings){
            int i = 0;
            while(i < planning.getPlanningMembers().size()){
                if(planning.getPlanningMembers().get(i).getMember().equals(employeeID)){
                    list.add(planning.getId());
                }
                i++;
            }
        }
        return list;
    }

    /**
     * This function returns the ID's of all planning units a planner is planner of.
     *
     * @param token: the identification token of the Axians web API.
     * @param employeeID: the id of the planner for which we want to know their planning units.
     *
     * @return
     * [
     *      id,
     *      ...,
     *      id
     * ]
     */
    public  List<String> getAllPlanningUnitIDsOfWhichEmployeeIDIsPlanner(String token, String employeeID) throws Exception {
        List<String> list = new ArrayList<>();
        PlanningUnits.getInstance().reload(ac, token);
        for(String id: PlanningUnits.getInstance().planningUnits.keySet()){
            PlanningUnit unit = ac.getPlanningUnitObject(token, id);
            if(unit.getOnCallPlanners().contains(employeeID)){
                list.add(unit.getId());
            }
        }
        return list;
    }

    /**
     * This function returns all employee ids that are in a certain planning units
     *
     * @param id: planning unit of which we want to know the employees of.
     * @return 200 OK
     *          [
     *                  id,
     *                  ...,
     *                  id
     *          ]
     */
    public List<PlanningMember> getAllEmployeesOfPlanningUnitWithId(String token, String id) throws Exception{
        List<PlanningMember> members = new ArrayList<>();
        PlanningLong planning = ac.getOnCallPlanning(token, id);
        for(PlanningMember member: planning.getMembers()){
            members.add(member);
        }
        return members;
    }

    /**
     * This function returns all employee ids that are in a certain list of planning units.
     *
     * @param ids: list of ids of planning units of which we want to know the employees of.
     * @return 200 OK
     *          [
     *                  id,
     *                  ...,
     *                  id
     *          ]
     */
    public List<PlanningMember> getAllEmployeesOfPlanningUnitsWithIds(String token, List<String> ids) throws Exception{
        List<PlanningMember> plmembers = new ArrayList<>();
        for(String unit: ids){
            for(PlanningMember member: getAllEmployeesOfPlanningUnitWithId(token, unit)){
                boolean erin = false;
                for(PlanningMember plmember: plmembers){
                    if(plmember.equals(member))
                        erin = true;
                }
                if(!erin){
                    plmembers.add(member);
                }
            }
        }
        return plmembers;
    }

    public List<PlanningMember> getAllPlannersOfPlanningUnitsWithIds(String token, List<String> ids) throws Exception{
        Set<PlanningMember> members = new HashSet<>();
        Set<PlanningMember> allMembers = new HashSet<>();
        for(String id: ids) {
            PlanningLong planning = ac.getOnCallPlanning(token, id);
            for (PlanningMember member : planning.getMembers()) {
                allMembers.add(member);
            }
        }
        for(String id: ids) {
            PlanningUnit unit = ac.getPlanningUnitObject(token, id);
            for(String memberid: unit.getOnCallPlanners()){
                for(PlanningMember member: allMembers){
                    if(member.getMember().equals(memberid)){
                        boolean found = false;
                        for(PlanningMember finalMember: members){
                            if(finalMember.getMember().equals(member.getMember()))
                                found = true;
                        }
                        if(!found) {
                            members.add(member);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(members);
    }
}
