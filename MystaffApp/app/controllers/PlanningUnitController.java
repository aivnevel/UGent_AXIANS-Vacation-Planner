package controllers;

import businessLogic.PlanningUnitHelper;
import businessLogic.ScheduleHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import exceptions.NoSuchIDException;
import exceptions.UnauthorizedException;
import models.planning.PlanningLong;
import org.springframework.jmx.export.UnableToRegisterMBeanException;
import persistence.HolidayMessageController;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilityClasses.HolidaysBelgium;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/*
 * PlanningUnitController returns data that is related to Planning Units.
 * This controller has two functions for each call.
 * One returns a Result to the caller. The other returns a JsonNode. This node can be used elsewhere in the back end.
 * Only the Result returner will be commented.
 */

public class PlanningUnitController extends Controller {
    @Inject
    private AxiansController ac;
    @Inject
    private PlanningUnitHelper planningUnitHelper;
    @Inject
    private HolidayMessageController hmController;
    private ScheduleHelper sh;

    private final boolean doLog;

    @Inject
    public PlanningUnitController(ScheduleHelper sh, Config config){
        this.sh = sh;
        doLog = config.getBoolean("mystaff.doLogRequests");
    }

    /**
     * This function returns the ID's of all planning units an employee is part of, with a flag that says whether the
     * employee is a planner of that plannerUnit.
     *
     * @param employeeID: the id of the employee for which we want to know their planning units.
     *
     * @return 200 OK
     * [
     *      {
     *          unitID: id of the unit
     *          isPlanner: true/false
     *      }
     * ]
     *          401 Unauthorized when person is not authorized.
     *          404 Not found when the planner id does not exist
     *          406 When another error occurs
     */
    public Result getAllPlanningUnitIDsInWhichEmployeeIDIsInWithPlannerFlag(String employeeID){
        try{
            if(doLog)
                System.out.println("sending 'GET' request to URL : /getAllPlanningUnitIDsInWhichEmployeeIDIsIn/" + employeeID);
            JsonNode json = getAllPlanningUnitIDsInWhichEmployeeIDIsInWithPlannerFlagRequest(getToken(request()), employeeID);
            return sendJSON(json);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    public JsonNode getAllPlanningUnitIDsInWhichEmployeeIDIsInWithPlannerFlagRequest(String token, String employeeID) throws Exception{
        List<String> listEmployee = planningUnitHelper.getAllPlanningUnitIDsInWhichEmployeeIDIsIn(token, employeeID);
        List<String> listPlanner = planningUnitHelper.getAllPlanningUnitIDsOfWhichEmployeeIDIsPlanner(token, employeeID);
        ArrayNode array = Json.newArray();
        for(String emp: listEmployee){
            ObjectNode node = array.addObject();
            node.put("unitId", emp);
            if(listPlanner.contains(emp)){
                node.put("isPlanner", true);
            } else {
                node.put("isPlanner", false);
            }
        }
        return array;
    }

    /**
     * This function returns the ID's of all planning units an employee is part of.
     *
     * @param employeeID: the id of the employee for which we want to know their planning units.
     *
     * @return 200 OK
     * [
     *      id,
     *      ...,
     *      id
     * ]
     *          401 Unauthorized when person is not authorized.
     *          404 Not found when the id does not exist
     *          406 When another error occurs
     */
    public Result getAllPlanningUnitIDsInWhichEmployeeIDIsIn(String employeeID){
        try{
            if(doLog)
                System.out.println("sending 'GET' request to URL : /getAllPlanningUnitIDsInWhichEmployeeIDIsIn/" + employeeID);
            JsonNode json = getAllPlanningUnitIDsInWhichEmployeeIDIsInRequest(getToken(request()), employeeID);
            return sendJSON(json);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    public JsonNode getAllPlanningUnitIDsInWhichEmployeeIDIsInRequest(String token, String employeeID) throws Exception{
        List<String> list = planningUnitHelper.getAllPlanningUnitIDsInWhichEmployeeIDIsIn(token, employeeID);
        JsonNode json = Json.toJson(list);
        return json;
    }

    /**
     * This function returns the ID's of all planning units a planner is planner of.
     *
     * @param employeeID: the id of the planner for which we want to know their planning units.
     *
     * @return 200 OK
     * [
     *      id,
     *      ...,
     *      id
     * ]
     *          401 Unauthorized when person is not authorized.
     *          404 Not found when the id does not exist
     *          406 When another error occurs
     */
    public Result getAllPlanningUnitIDsOfWhichEmployeeIDIsPlanner(String employeeID) {
        try{
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /getAllPlanningUnitIDsOfWhichEmployeeIDIsPlanner/" + employeeID);
            return sendJSON(getAllPlanningUnitIDsOfWhichEmployeeIDIsPlannerRequest(getToken(request()), employeeID));
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    public JsonNode getAllPlanningUnitIDsOfWhichEmployeeIDIsPlannerRequest(String token, String employeeID) throws Exception{
        List<String> list = planningUnitHelper.getAllPlanningUnitIDsOfWhichEmployeeIDIsPlanner(token, employeeID);
        JsonNode json = Json.toJson(list);
        return json;
    }

    /**
     * Returns the employees that COULD be scheduled in a certain period.
     *     The start and end of the period are given in "yyyy-mm-dd" format.
     *     It looks which skills are needed and which skills are possible for each day part.
     *     Then it adds the people that are not absent that have these skills.
     *     Next it checks all constraints like simultaneous shifts and days with recup with the help of a backtracker.
     *     This function returns the
     *         -number of needed people
     *         -minimum number of people that can be given an absence
     *         -maximum number of people that can be given an absence
     *     for each shift separately as well as in total.
     *     It also returns the for each day, for every shift the specific people id's that could be planned in and that are
     *     planned in in this case.
     *     It may seem weird that two separate numbers are given, we would want only one number, THE number of people that could
     *     be given an absence. This is impossible.
     *         e.g. There are two shifts.
     *             In one shift, there is only one person needed, but he needs a complex skillset.
     *             In the second shift, there is als one person needed, but with a simple skillset.
     *             In the first shift, there is one person with the correct skillset
     *             In the second shift, there are 4 people with the correct skillset.
     *             Now, the person in the first shift can not be given an absence, he is crucial to that shift.
     *             But 3 people in the second shift can be given an absence.
     *             Thus we say: there are 2 people needed and 0-3 can be given an absence.
     *
     * @param start: the start of the period in "yyyy-mm-dd"
     * @param end: the end of the period in "yyyy-mm-dd"
     * @param unitid: list of unitids we want to test on.
     * @return 200 OK if everything is ok.
     *      [
     *           {
     *               date: "yyyy-mm-dd",
     *               dayPart: "AM" or "PM",
     *               totalMaxReserve: int,
     *               totalMinReserve: int,
     *               totalNumberNeeded: int,
     *               shift 'shiftname':
     *               {
     *                   numberOfPeopleNeeded: int,
     *                   reserve: int,
     *                   requiredPeople:    String[],
     *                   alternativePeople: String[],
     *                   finalPeople:       String[],
     *               }
     *           },
     *           ...
     *       ]
     *         406 Not accepted if start or end isn't "yyyy-mm-dd"
     *         401 Unauthorized when person is not authorized.
     *         404 Not found when the id does not exist
     *         406 When another error occurs
     */
    public Result getAvailablePeoplePerDayPartForPlanningUnits(String start, String end, List<String> unitid) {
        try {
            if (doLog) {
                System.out.print("Sending 'GET' request to URL : /getAvailablePeoplePerDayPartForPlanningUnits/" + start + "/" + end + "/?");
                for (String id : unitid) {
                    System.out.print("unitid=" + id + "&");
                }
                System.out.println();
            }
            ScheduleHelper newsh = new ScheduleHelper(planningUnitHelper);
            newsh.ac = ac;
            newsh.hmController = hmController;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, PlanningLong> plannings = new HashMap<>();
            for (String unit : unitid) {
                plannings.put(unit, ac.getOnCallPlanning(getToken(request()), unit));
            }
            Date startdate;
            Date enddate;
            try {
                startdate = dateFormat.parse(start);
                enddate = dateFormat.parse(end);
                LocalDate lstartdate = startdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate lenddate = enddate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return sendJSON(newsh.getAvailablePeoplePerDayPartForPlanningUnits(getToken(request()), unitid, lstartdate, lenddate, plannings, true));
            } catch (Exception E) {
                E.printStackTrace();
                return notAcceptable("Dates should be given in the format \"yyyy-MM-dd\"");
            }
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
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
     *          401 Unauthorized when person is not authorized.
     *          404 Not found when the id does not exist
     *          406 When another error occurs
     */
    public Result getAllEmployeesOfPlanningUnitsWithIds(List<String> ids) {
        try{
            if(doLog) {
                System.out.print("Sending 'GET' request to URL : /getAllEmployeesOfPlanningUnitsWithIds/?");
                for(String id: ids){
                    System.out.print("ids="+id+"&");
                }
                System.out.println();
            }
            JsonNode node = Json.toJson(planningUnitHelper.getAllEmployeesOfPlanningUnitsWithIds(getToken(request()), ids));
            return ok(node).as("application/json");
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Simple auxiliary function to send a JsonNode.
     */
    private Result sendJSON(JsonNode jsonNode) {
        try {
            return ok(jsonNode).as("application/json");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return notFound();
    }

    /**
     * Simple auxiliary function to get token out of request.
     */
    private String getToken(Http.Request request) {
        return "Bearer " + request.getHeaders().toMap().get("token").get(0);
    }

}
