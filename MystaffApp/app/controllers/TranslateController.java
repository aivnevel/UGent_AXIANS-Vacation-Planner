package controllers;

import businessLogic.EmployeeGroup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import enumerations.Locations;
import enumerations.PlanningUnits;
import enumerations.Skills;
import exceptions.NoSuchIDException;
import exceptions.UnauthorizedException;
import interfaces.Employee;
import models.planning.Location;
import models.planning.Planning;
import models.planning.PlanningMember;
import models.planning.Skill;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.awt.*;

import static play.mvc.Results.*;

/*
 * TranslateController translates ID's to human readable names for the front end.
 * It does this for skills, locations, people and planning units.
 * Every get also includes a refresh from the Axians server. Therefore, the user doesn't need to worry about refreshing.
 */

public class TranslateController extends Controller {
    @Inject
    AxiansController ac;

    private final boolean doLog;

    @Inject
    public TranslateController(Config config){
        doLog = config.getBoolean("mystaff.doLogRequests");
    }

    /**
     * Refresh the maps stocked in Skills, Locations and PlanningUnits object.
     * This function is not needed elsewhere in this controller, because every get implies a refresh.
     * It can be used in other controllers.
     *
     * @return 200 OK when everything is okay
     *         500 Internal server error when communication with the Axians server is impossible.
     */
    public Result refreshData(){
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /refreshData");
            String token = ac.getToken(request());
            Planning[] plannings = ac.getOnCallPlanningsArray(token);
            refreshSkills(plannings);
            refreshLocations(plannings);
            refreshPlanningUnits(plannings);
            // Refreshes data for ShortMemberData
            try {
                EmployeeGroup eg = new EmployeeGroup(ac, getToken(request()));
            } catch(UnauthorizedException e){
                return unauthorized();
            } catch(NoSuchIDException e){
                return notFound(e.getMessage());
            } catch(Exception e){
                return notAcceptable();
            }
            return ok();
        } catch (Exception e){
            return internalServerError("Could not contact Axians servers");
        }
    }

    /**
     * Returns the id's and names of all skills that are in the current tenant.
     *
     *
     * @return 200 OK with when everything is okay
     *         [
     *              {
     *                  id: ID of a skill
     *                  value: name of the skill
     *              },
     *              ...
     *              {
     *                  ...
     *              }
     *         ]
     *         500 Internal server error when there are no Skills loaded.
     */
    public Result getAllSkills() {
        try{
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /getAllSkills");
            ArrayNode json = Json.newArray();
            refreshSkills();
            Skills allSkills = Skills.getInstance();
            if(allSkills.skills.keySet().isEmpty()){
                System.err.println("No skills are loaded.");
                return internalServerError("No skills are loaded.");
            }
            for (String key : allSkills.skills.keySet()) {
                ObjectNode node = json.addObject();
                node.put("id", key);
                node.put("value", allSkills.skills.get(key));
            }
            return sendJSON(json);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Returns one id value pair of a skill of the tenant based on the provided ID
     *
     *
     * @return 200 OK with when everything is okay
     *         {
     *              id: ID of a skill
     *              value: name of the skill
     *         }
     *         404 Not Found when ID is not in the database
     */
    public Result getSkillById(String id) {
        try{
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /getSkillById/" + id);
            ObjectNode json = Json.newObject();
            refreshSkills();
            Skills skills = Skills.getInstance();
            if(!skills.skills.containsKey(id)){
                System.err.println("Skill with id " + id + " not found.");
                return notFound();
            }
            json.put("id", id);
            json.put("value", skills.skills.get(id));
            return sendJSON(json);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     *  Refresh the skills with a new call to Axians.
     */
    private void refreshSkills() throws Exception {
        Skills.getInstance().reload(ac, getToken(request()));
    }

    /**
     *  Refresh the skills based on the plannings object of a previous call.
     */
    private void refreshSkills(Planning[] plannings){
        Skills.getInstance().reload(plannings);
    }

    /**
     * Returns the id's and names of all locations that are in the current tenant.
     *
     *
     * @return 200 OK with when everything is okay
     *         [
     *              {
     *                  id: ID of a location
     *                  value: name of the location
     *              },
     *              ...
     *              {
     *                  ...
     *              }
     *         ]
     *         500 Internal server error when there are no Locations loaded.
     */
    public Result getAllLocations() {
        try{
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /getAllLocations");
            ArrayNode json = Json.newArray();
            refreshLocations();
            Locations loc = Locations.getInstance();
            if(loc.locations.keySet().isEmpty()){
                System.err.println("No locations are loaded.");
                return internalServerError("No locations are loaded.");
            }
            for (String key : loc.locations.keySet()) {
                ObjectNode node = json.addObject();
                node.put("id", key);
                node.put("value", loc.locations.get(key));
            }
            return sendJSON(json);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Returns one id value pair of a location of the tenant based on the provided ID
     *
     *
     * @return 200 OK with when everything is okay
     *         {
     *              id: ID of a location
     *              value: name of the location
     *         }
     *         404 Not Found when ID is not in the database
     */
    public Result getLocationById(String id) {
        try{
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /getLocationById/" + id);
            ObjectNode json = Json.newObject();
            refreshLocations();
            Locations locations = Locations.getInstance();
            if(!locations.locations.containsKey(id)){
                System.err.println("Location with id " + id + " not found.");
                return notFound();
            }
            json.put("id", id);
            json.put("value", locations.locations.get(id));
            return sendJSON(json);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     *  Refresh the locations with a new call to Axians.
     */
    private void refreshLocations() throws Exception {
        Locations.getInstance().reload(ac, getToken(request()));
    }

    /**
     *  Refresh the locations based on the plannings object of a previous call.
     */
    private void refreshLocations(Planning[] plannings){
        Locations.getInstance().reload(plannings);
    }

    /**
     * Returns the first and last names of all people that are in the current tenant.
     *
     *
     * @return 200 OK with when everything is okay
     *         [
     *              {
     *                  id: ID of a person
     *                  firstName: first name of that person
     *                  lastName: last name of that person
     *              },
     *              ...
     *              {
     *                  ...
     *              }
     *         ]
     *         500 Internal server error when there are no people loaded.
     */
    public Result getAllMemberNamesData() {
        try{
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /getAllMemberNamesData");
            ArrayNode arr = Json.newArray();
            EmployeeGroup eg = new EmployeeGroup(ac, getToken(request()));
            if(eg.getAllEmployees().isEmpty()){
                System.err.println("No people are loaded.");
                return internalServerError("No people are loaded.");
            }
            for (Employee emp : eg.getAllEmployees()) {
                ObjectNode json = arr.addObject();
                json.put("id", emp.getID());
                json.put("firstName", emp.getFirstName());
                json.put("lastName", emp.getLastName());
            }
            return sendJSON(arr);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Returns one id with a first and a last name of a person of the tenant based on the provided ID
     *
     *
     * @return 200 OK with when everything is okay
     *         {
     *              id: ID of a person
     *              firstName: first name of the person
     *              lastName: last name of the person
     *         }
     *         404 Not Found when ID is not in the database
     */
    public Result getShortMemberNameDataOfId(String id) {
        try{
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /getShortMemberNameDataOfId/" + id);
            ObjectNode json = Json.newObject();
            EmployeeGroup eg = new EmployeeGroup(ac, getToken(request()));
            Employee emp = eg.findEmployeeByID(id);
            if(emp == null){
                System.err.println("Employee with id " + id + " not found.");
                return notFound();
            }
            json.put("id", id);
            json.put("firstName", emp.getFirstName());
            json.put("lastName", emp.getLastName());
            return sendJSON(json);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Returns the id's and names of all planning units that are in the current tenant.
     *
     *
     * @return 200 OK with when everything is okay
     *         [
     *              {
     *                  id: ID of a planning unit
     *                  value: name of the planning unit
     *              },
     *              ...
     *              {
     *                  ...
     *              }
     *         ]
     *         500 Internal server error when there are no PlanningUnits loaded.
     */
    public Result getAllPlanningUnits() {
        try{
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /getAllPlanningUnits");
            PlanningUnits units = PlanningUnits.getInstance();
            refreshPlanningUnits();
            ArrayNode json = Json.newArray();
            if(units.planningUnits.keySet().isEmpty()){
                System.err.println("No planning units are loaded.");
                return internalServerError("No planning units are loaded.");
            }
            for (String key : units.planningUnits.keySet()) {
                ObjectNode node = json.addObject();
                node.put("id", key);
                node.put("value", units.planningUnits.get(key));
            }
            return sendJSON(json);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Returns one id value pair of a planning unit of the tenant based on the provided ID
     *
     *
     * @return 200 OK with when everything is okay
     *         {
     *              id: ID of a planning unit
     *              value: name of the planning unit
     *         }
     *         404 Not Found when ID is not in the database
     */
    public Result getPlanningUnitById(String id) {
        try{
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /getPlanningUnitById/" + id);
            ObjectNode json = Json.newObject();
            PlanningUnits units = PlanningUnits.getInstance();
            refreshPlanningUnits();
            if(!units.planningUnits.containsKey(id)){
                System.err.println("Planning unit with id " + id + " not found.");
                return notFound();
            }
            json.put("id", id);
            json.put("value", units.planningUnits.get(id));
            return sendJSON(json);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }


    /**
     *  Refresh the planning units with a new call to Axians.
     */
    private void refreshPlanningUnits() throws Exception {
        PlanningUnits.getInstance().reload(ac, getToken(request()));
    }

    /**
     *  Refresh the planning units based on the plannings object of a previous call.
     */
    private void refreshPlanningUnits(Planning[] plannings){
        PlanningUnits.getInstance().reload(plannings);
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
