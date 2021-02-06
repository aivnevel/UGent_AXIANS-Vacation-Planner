package controllers;

import businessLogic.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import enumerations.*;
import exceptions.NoSuchIDException;
import exceptions.UnauthorizedException;
import interfaces.Employee;
import models.Member;
import models.planning.*;
import persistence.HolidayMessageController;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilityClasses.ExactDate;
import utilityClasses.HolidayMessage;
import utilityClasses.HolidaysBelgium;
import utilityClasses.PlanningUnitState;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.sql.Timestamp;
import java.util.*;

/*
    AxiansController takes data from Axians and gives it back as it came in.
    FormatController will heavily adapt the data. Mostly, it will do this to have less network traffic or to let the
    front end handle the data easier.
    When an application at the frontend only needs the name of the person, it is totally unnecessary to send all
    the other data with it.
 */

public class FormatController extends Controller {

    private EmployeeGroup eg;
    @Inject
    private AxiansController ac;
    @Inject
    private HolidayMessageController hmController;

    private PlanningUnitHelper planningUnitHelper;
    private ScheduleHelper sh;

    private final boolean doLogRequests;
    private final boolean doLogBacktracker = false;

    private static final ObjectMapper _mapper = new ObjectMapper();

    @Inject
    public FormatController(PlanningUnitHelper planningUnitHelper, ScheduleHelper sh, Config config){
        this.planningUnitHelper = planningUnitHelper;
        this.sh = sh;
        doLogRequests = config.getBoolean("mystaff.doLogRequests");
    }

    /**
     * Returns the absence types defined by this back end.
     * e.g Yearly, Sickness, ....
     *
     * @return 200 OK
     *         [
     *              type,
     *              ...,
     *              type
     *         ]
     *         401 Unauthorized when person is not authorized.
     */
    public Result getOwnAbsenceTypes() {
        if (ac.isAuthorised(getToken(request()))) {
            if (doLogRequests)
                System.out.println("Sending 'GET' request to URL : /getOwnAbsenceTypes");
            ArrayNode absences = Json.newArray();
            for (HolidayType key : HolidayType.values()) {
                absences.add(key.toString());
            }
            return sendJSON(absences);
        }
        return unauthorized();
    }

    /**
     * Builds an EmployeeGroup. Used for updated information about members.
     *
     * @param token the token provided by Axians.
     */
    public void buildEmployeeGroup(String token) throws Exception {
        eg = new EmployeeGroup(ac,token);
        for (Employee d : eg.getAllEmployees()) {
            ((Doctor) d).loadHolidayMessages();
        }
    }

    /**
     * Returns all people and their skills, locations, ... and holidayMessages in a detailed JSON-format
     * See the comment above jsonIfyPeople for the format
     * @param id: the id of the planner
     * @return 200 OK
     *         401 Unauthorized when person is not authorized.
     *         404 Not found when the planner id does not exist
     *         406 When another error occurs
     */
    public Result getAllHolidayMessagesUnderPlannerWithId(String id){
        if (ac.isAuthorised(getToken(request()))) {
            if(doLogRequests)
                System.out.println("Sending 'GET' request to URL : /getAllHolidayMessagesUnderPlannerWithId/" + id);
            try {
                return sendJSON(getAllHolidayMessagesUnderPlannerWithIdRequest(getToken(request()), id));
            } catch(UnauthorizedException e){
                return unauthorized();
            } catch(NoSuchIDException e){
                return notFound(e.getMessage());
            } catch(Exception e){
                return notAcceptable();
            }
        }
        return unauthorized();
    }

    /**
     * Auxiliary function for getAllHolidayMessagesUnderPlannerWithId
     * Calls getAllHolidayMessagesForPlanningUnitsRequest with all unit ids the given planner is planner of.
     *
     * @param token: token provided by Axians
     * @param id: ID of the planner
     */

    public JsonNode getAllHolidayMessagesUnderPlannerWithIdRequest(String token, String id) throws Exception {
        List<String> units = planningUnitHelper.getAllPlanningUnitIDsOfWhichEmployeeIDIsPlanner(token, id);
        return getAllHolidayMessagesForPlanningUnitsRequest(token, units);
    }

    /**
     * Returns a JSON in which all members and planning units are listed for each given planning unit on it's own.
     * The format is basically that of jsonIfyPeople but pasted into an array, one time for every planning unit.
     *
     * @param ids: a list of planning unit id"s
     */
    public Result getAllHolidayMessagesPerPlanningUnit(List<String> ids) throws Exception{
        if(doLogRequests) {
            System.out.print("Sending 'GET' request to URL : /getAllHolidayMessagesPerPlanningUnit/?");
            for(String id: ids){
                System.out.print("ids="+id+"&");
            }
            System.out.println();
        }
        ObjectNode json = Json.newObject();
        for(String id: ids){
            List<String> list = new ArrayList<>();
            list.add(id);
            try {
                json.put(id, getAllHolidayMessagesForPlanningUnitsRequest(getToken(request()), list));
            } catch(UnauthorizedException e){
                return unauthorized();
            } catch(NoSuchIDException e){
                return notFound(e.getMessage());
            } catch(Exception e){
                return notAcceptable();
            }
        }
        return sendJSON(json);
    }

    /**
     * Given a token and planning unit id's, this function calculates the membersPerPlanningsUnitID-, members- and hm-
     * object jsonIfyPeople needs.
     *
     * @param token: token provided by Axians
     * @param units: a list of all unit id's we want the people and holiday messages of.
     *
     * @throws Exception
     */
    public JsonNode getAllHolidayMessagesForPlanningUnitsRequest(String token, List<String> units) throws Exception {
        Map<String,List<PlanningMember>> membersPerPlanningsUnitId = new HashMap<>();
        for(String unit: units){
            membersPerPlanningsUnitId.put(unit, planningUnitHelper.getAllEmployeesOfPlanningUnitWithId(token, unit));
        }
        Member[] members = ac.getMembersArray(token);
        List<HolidayMessage> hms = new ArrayList<>();
        for(String unit: units) {
            for (PlanningMember plmember : membersPerPlanningsUnitId.get(unit)) {
                Member me = null;
                for (Member member : members) {
                    if (member.getId().equals(plmember.getMember())) {
                        me = member;
                    }
                }
                if (me == null) {
                    throw new Exception();
                }
                try {
                    hms.addAll(hmController.getHolidayMessageWithEmployeeID(me.getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(hms == null){
            throw new Exception("HolidayMSGs is empty");
        }
        return jsonIfyPeople(membersPerPlanningsUnitId, members, hms);
    }

    /**
     * Returns a JSON with detailed information about the given members and their holiday messages.
     *
     * @param membersPerPlanningsUnitId: A map that has planning unit id's as key and a list of PlanningMembers as value
     * @param members: an array of member objects. There has to be a member object in this array for every PlanningMember.
     * @param hms: a list of HolidayMessages. Is variable to allow user to add only approved hm's, or only new, ...
     *
     * @return JsonNode
     *         [
     *              {
     *                  PlanningUnits: a list of locations and skills the person has per planning unit
     *                      [
     *                          {
     *                              'id': id of a planningUnit
     *                                  {
     *                                      locations: all the locations a person can work in that planning unit
     *                                      skills: all skills a person has in that planning unit
     *                                  }
     *                          },
     *                          ...
     *                      ]
     *                  holidayMessages: a list of holiday messages
     *                      [
     *                          {
     *                              comment: the comment the employee gave about the absence
     *                              employeeID: the ID of the employee
     *                              exactDates
     *                                  [
     *                                      {
     *                                              date
     *                                                  [
     *                                                      'year': int yyyy
     *                                                      'month': int m or mm
     *                                                       'day': int d or dd
     *                                                   ]
     *                                              daypart: "AM" or "PM"
     *                                              id: id of the exactDate
     *                                      },
     *                                      ...
     *                                  ]
     *                              history
     *                                  [
     *                                      {
     *                                          comment: the comment the employee gave about the absence
     *                                          exactDates
     *                                              [
     *                                                  {
     *                                                      date
     *                                                          [
     *                                                              'year': int yyyy
     *                                                              'month': int m or mm
     *                                                              'day': int d or dd
     *                                                         ]
     *                                                       daypart: "AM" or "PM"
     *                                                       id: id of the exactDate
     *                                                  },
     *                                                  ...
     *                                              ]
     *                                          id: id of the historyObject
     *                                          requestByID: ID of the employee that requested this absence, mostly this person, can also be a planner
     *                                          requestDate: Timestamp of when the absence was requested, int xxxxxxxxxxxxx
     *                                          state: state of the request: APPROVED, NEW, IN CONSIDERATION or REJECTED
     *                                          type: type of the absence: Educative, Yearly, Sickness, ...
     *                                         },
     *                                         ...
     *                                      },
     *                                      ...
     *                                  ]
     *                              id: id of the holidayMessage
     *                              lastUpdate: last update by the employee or a planner, int xxxxxxxxxxxxx
     *                              plannerOfLastUpdate: the plannerID that did the last update
     *                              planningUnitStates
     *                                  [
     *                                      {
     *                                          comment: comment of the planner about this absence
     *                                          plannerID: id of the planner
     *                                          state: the decision that the planner made about this absence (APPROVED, ...)
     *                                          unitId: id of the planning unit for which the planner did this decision
     *                                      },
     *                                      ...
     *                                  ]
     *                              requestByID: ID of the employee that requested this absence, mostly this person, can also be a planner
     *                              requestDate: Timestamp of when the absence was requested, int xxxxxxxxxxxxx
     *                              state: state of the request: APPROVED, NEW, IN CONSIDERATION or REJECTED
     *                              type: type of the absence: Educative, Yearly, Sickness, ...
     *                          }
     *                      ]
     *                  id: id of the employee
     *                  lastUpdate: date of the last update by the employee or a planner for this person in javascript format. e.g. "Wed May 01 17:06:31 CEST 2019"
     *                  locations: all id's of locations the person has under the requested planning units
     *                      [
     *                          'id1',
     *                          'id2',
     *                          ...
     *                      ]
     *                  planningUnits: all id's of planning units a person is part of.
     *                      [
     *                          'id1',
     *                          'id2',
     *                           ...
     *                      ]
     *                  skills: all id's of skills the person has under the requested planning units
     *                     [
     *                          'id1',
     *                          'id2',
     *                           ...
     *                      ]
     *              },
     *              ...
     *         ]
     *
     * @throws Exception when no member is found for every planningMember
     */
    private ArrayNode jsonIfyPeople(Map<String,List<PlanningMember>> membersPerPlanningsUnitId, Member[] members, List<HolidayMessage> hms) throws Exception {
        ArrayNode json = Json.newArray();
        // Because several planningMember-objects are saved for several planningunits, we have to keep track of who is
        // already in the endJSON.
        Map<String, Set<String>> skillsPerMember;
        Map<String, Set<String>> locationsPerMember;
        Map<String, Set<String>> planningUnitsPerMember;
        skillsPerMember = new HashMap<>();
        locationsPerMember = new HashMap<>();
        planningUnitsPerMember = new HashMap<>();
        HashMap<String, ObjectNode> PlanningUnitNodePerPerson = new HashMap<>();
        HashMap<String, ObjectNode> nodePerPerson = new HashMap<>();
        for(String planningsUnitId: membersPerPlanningsUnitId.keySet()) {
            for (PlanningMember plmember : membersPerPlanningsUnitId.get(planningsUnitId)) {
                Member me = null;
                for (Member member : members) {
                    if (member.getId().equals(plmember.getMember())) {
                        me = member;
                    }
                }
                if (me == null) {
                    throw new Exception();
                }
                if (!PlanningUnitNodePerPerson.containsKey(me.getId())) {
                    skillsPerMember.put(me.getId(), new HashSet<>());
                    locationsPerMember.put(me.getId(), new HashSet<>());
                    planningUnitsPerMember.put(me.getId(), new HashSet<>());
                    ObjectNode node = json.addObject();
                    nodePerPerson.put(me.getId(), node);
                    node.put("id", me.getId());
                    //node.putArray("skills");
                    for (String skill : plmember.getSkills()) {
                        //skillArr.add(skill);
                        skillsPerMember.get(me.getId()).add(skill);
                    }

                    ObjectNode planningUnits = node.putObject("PlanningUnits");
                    PlanningUnitNodePerPerson.put(me.getId(), planningUnits);
                    ObjectNode unitArr = planningUnits.putObject(planningsUnitId);
                    ArrayNode skills = unitArr.putArray("skills");
                    for (String skill : plmember.getSkills()) {
                        skills.add(skill);
                    }
                    ArrayNode locations = unitArr.putArray("locations");
                    for (String location : plmember.getLocations()) {
                        locations.add(location);
                    }

                    for (String location : plmember.getLocations()) {
                        locationsPerMember.get(me.getId()).add(location);
                    }

                    planningUnitsPerMember.get(me.getId()).add(planningsUnitId);
                    try {
                        if (!hms.isEmpty()) {
                            Date lastUpdate = null;
                            for (HolidayMessage hm : hms) {
                                if (lastUpdate == null)
                                    lastUpdate = hm.getLastUpdate();
                                else if (lastUpdate.before(hm.getLastUpdate())) {
                                    lastUpdate = hm.getLastUpdate();
                                }
                            }
                            node.put("lastUpdate", lastUpdate.toString());
                            ArrayNode hmgs = node.putArray("holidayMessages");
                            List<HolidayMessage> usedHMs = new ArrayList<>();
                            for (HolidayMessage hm : hms) {
                                if (hm.getEmployeeID().equals(me.getId()) && !usedHMs.contains(hm)) {
                                    usedHMs.add(hm);
                                    hmgs.add(Json.toJson(hm));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ObjectNode planningUnits = ((ObjectNode) PlanningUnitNodePerPerson.get(me.getId()));
                    ObjectNode unitArr = planningUnits.putObject(planningsUnitId);
                    ArrayNode skills = unitArr.putArray("skills");
                    for (String skill : plmember.getSkills()) {
                        skills.add(skill);
                        skillsPerMember.get(me.getId()).add(skill);
                    }
                    ArrayNode locations = unitArr.putArray("locations");
                    for (String location : plmember.getLocations()) {
                        locations.add(location);
                        locationsPerMember.get(me.getId()).add(location);
                    }
                    planningUnitsPerMember.get(me.getId()).add(planningsUnitId);
                }
            }
        }
        for(String key: PlanningUnitNodePerPerson.keySet()){
            ArrayNode skillArr = nodePerPerson.get(key).putArray("skills");
            for(String skill: skillsPerMember.get(key)){
                skillArr.add(skill);
            }
            ArrayNode locationArr = nodePerPerson.get(key).putArray("locations");
            for(String location: locationsPerMember.get(key)){
                locationArr.add(location);
            }
            ArrayNode planningUnitArr = nodePerPerson.get(key).putArray("planningUnits");
            for(String planningUnit: planningUnitsPerMember.get(key)){
                planningUnitArr.add(planningUnit);
            }

        }
        return json;
    }

    /**
     * Return all collision states and collision groups for all new or in consideration holiday messages under a certain
     * planning unit
     *
     * @param id: the id of the planning unit
     * @param start: the start of the period to evaluate in "yyyy-MM-dd" format.
     * @param end: the end of the period to evaluate in "yyyy-MM-dd" format.
     *
     * @return 200 OK
     *         Json format of "getAllCollisionStatesAndGroupsForPlanningUnits"
     *         401 Unauthorized when person is not authorized.
     *         406 Not acceptable when the strings are in the wrong format or another error occurs
     *         404 Not found when the planning unit id does not exist
     */
    public Result getAllCollisionStatesUnderPlanningUnitBetween(String id,String start, String end){
        try {
            if (doLogRequests) {
                System.out.println("Sending 'GET' request to URL : /getAllCollisionStatesUnderPlanningUnitBetween/" + id + "/" + start + "/" + end);
            }
            LocalDate lstartdate = stringToDate(start);
            LocalDate lenddate = stringToDate(end);
            try {
                if (lstartdate == null || lenddate == null) {
                    return notAcceptable();
                } else {
                    lstartdate = stringToDate(start).minusDays(5);
                    lenddate = stringToDate(end).plusDays(5);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return notAcceptable("Dates should be in format \"yyyy-MM-dd\"");
            }
            return sendJSON(getAllCollisionStatesUnderPlanningUnitBetweenRequest(getToken(request()), id, lstartdate, lenddate));
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Auxiliary function for getAllCollisionStatesUnderPlanningUnitBetween
     */
    public JsonNode getAllCollisionStatesUnderPlanningUnitBetweenRequest(String token, String unitid, LocalDate lstartdate, LocalDate lenddate) throws Exception
    {
        List<String> units = new ArrayList<>();
        units.add(unitid);
        return getAllCollisionStatesAndGroupsForPlanningUnits(token, units, lstartdate, lenddate);
    }

    /**
     * Return all collision states and collision groups for all new or in consideration holiday messages under a certain planner
     *
     * @param id: the id of the planner
     * @param start: the start of the period to evaluate in "yyyy-MM-dd" format.
     * @param end: the end of the period to evaluate in "yyyy-MM-dd" format.
     *
     * @return 200 OK
     *         Json format of "getAllCollisionStatesAndGroupsForPlanningUnits"
     *         401 Unauthorized when person is not authorized.
     *         406 Not acceptable when the strings are in the wrong format or another error occurs
     *         404 Not found when the planner id does not exist
     */
    public Result getAllCollisionStatesUnderPlannerBetween(String id,String start, String end) {
        if(doLogRequests)
            System.out.println("Sending 'GET' request to URL : /getAllHolidayMessagesUnderPlannerWithId/" + id + "/" + start + "/" + end);
        LocalDate lstartdate = stringToDate(start);
        LocalDate lenddate = stringToDate(end);
        try {
            if (lstartdate == null || lenddate == null) {
                return notAcceptable();
            } else {
                lstartdate = stringToDate(start).minusDays(5);
                lenddate = stringToDate(end).plusDays(5);
            }
        } catch( Exception e){
            e.printStackTrace();
            return notAcceptable("Dates should be given in the format \"yyyy-MM-dd\"");
        }
        try {
            return sendJSON(getAllCollisionStatesUnderPlannnerBetweenRequest(getToken(request()), id, lstartdate, lenddate));
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Auxiliary function for getAllCollisionStatesUnderPlannerBetween
     */
    public JsonNode getAllCollisionStatesUnderPlannnerBetweenRequest(String token, String plannerID, LocalDate lstartdate, LocalDate lenddate) throws Exception
    {
        List<String> units = planningUnitHelper.getAllPlanningUnitIDsOfWhichEmployeeIDIsPlanner(token, plannerID);
        return getAllCollisionStatesAndGroupsForPlanningUnits(token, units, lstartdate, lenddate);
    }

    /**
     * Return a list of all holiday messages that are new or in consideration for a certain list of PlanningMember objects.
     *
     * @param plmembers: list of PlanningMember objects for each person we want to get the holiday messages of.
     * @param lenddate: LocalDate object for the end date of the period
     * @param lstartdate: LocalDate object for the start date of the period
     * @return a list of HolidayMessages.
     */
    private List<HolidayMessage> getAllHolidayMessagesNewAndInConsideration(List<PlanningMember> plmembers, LocalDate lenddate, LocalDate lstartdate){
        List<HolidayMessage> allhms = new ArrayList<>();
        List<String> foundpeople = new ArrayList<>();

        for (PlanningMember plmember : plmembers) {
            // Each person can have multiple planningmember objects.
            if(!foundpeople.contains(plmember.getMember())) {
                allhms.addAll(hmController.getHolidayMessagesInConsiderationOfDoctorWithID(plmember.getMember()));
                allhms.addAll(hmController.getHolidayMessagesNewOfDoctorWithID(plmember.getMember()));
                foundpeople.add(plmember.getMember());
            }
        }

        int initialSize = allhms.size();
        List<HolidayMessage> usedHms = new ArrayList<>();
        for(int i = initialSize -1; i >= 0; i--){
            if(!usedHms.contains(allhms.get(i))) {
                usedHms.add(allhms.get(i));
                if (!(allhms.get(i).getExactDates().get(0).getDate().isBefore(lenddate) &&
                        allhms.get(i).getExactDates().get(allhms.get(i).getExactDates().size() - 1).getDate().isAfter(lstartdate))) {
                    allhms.remove(i);
                }
            } else {
                allhms.remove(i);
            }
        }
        return allhms;
    }

    /**
     * Simple auxiliary function that creates an array of PersonDates, given a list of holiday messages.
     * PersonDate objects are used because they are smaller.
     *
     * @param allhms: a list of hm's to convert to PersonDates
     * @return a list of PersonDates
     */
    private PersonDate[] getAllPersonDatesFromHms(List<HolidayMessage> allhms){
        PersonDate[] dates = new PersonDate[allhms.size()];
        for(int i = 0; i < allhms.size(); i++){
            PersonDate pd = new PersonDate();
            HolidayMessage hm = allhms.get(i);
            pd.setDates(hm.getExactDates());
            pd.setEmployeeID(hm.getEmployeeID());
            pd.setId(hm.getId());
            dates[i] = pd;
        }
        return dates;
    }

    /**
     * Calculates all collision states in a quick, but insufficient manner for a given list of HolidayMessages.
     *
     * First calculate the whole group. If that doesn't crash, we can return that all hm's can be approved. => "NEVER"
     * If not, calculate each element on its own. If that crashes, that element will always collide and it can not
     * be approved. => "ALWAYS"
     * Now test the group without those crashing elements.
     * If the group doesn't crash, those elements are also good, they can be approved. => "NEVER"
     * If the group still crashes, there is a group with a size greater than one. We simply don't know where the groups
     * are. The whole remaining group gets the state "UNKNOWN.
     *
     *
     * @param token: token provided by Axians
     * @param functionCounter: a counter to debug how many times "isPlanning..." gets called
     * @param plannings: a map of PlanningLong objects with keys their id's. One key-value pair is needed for every planning unit to test.
     * @param dates: an array of all PersonDates that have to be checked (they represent HolidayMessages)
     * @param sh: a ScheduleHelper class to calculate the collisions.
     * @param units: a list of all planning unit id's to test..
     * @return
     */
    private CollisionState[] calculateCollisions(String token,int functionCounter, Map<String, PlanningLong> plannings, PersonDate[] dates,
                                                 ScheduleHelper sh, List<String> units){
        ScheduleHelper newsh = new ScheduleHelper(planningUnitHelper);
        newsh.ac = ac;
        newsh.hmController = hmController;
        CollisionState[] collisions = new CollisionState[dates.length];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        if(doLogBacktracker)
            System.out.println("start " + sdf.format(new Timestamp(System.currentTimeMillis())));

        if(newsh.isPlanningStillPossibleWithoutCertainPeopleGroups(token, units, Arrays.asList(dates), plannings,true)){
            if(doLogBacktracker)
                System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
            for(int i = 0; i < collisions.length; i++) {
                collisions[i] = CollisionState.NEVER;
            }
        }
        else {
            if(doLogBacktracker)
                System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
            for (int i = 0; i < dates.length; i++) {
                if (!newsh.isPlanningStillPossibleWithoutCertainPeopleGroups(token, units, Arrays.asList(dates[i]), plannings,true)) {
                    collisions[i] = CollisionState.ALWAYS;
                }
                if(doLogBacktracker)
                    System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
            }

            Map<PersonDate, Integer> possiblePeople = new HashMap<>();
            Map<PersonDate, Integer> allPeople = new HashMap<>();
            for (int i = 0; i < dates.length; i++) {
                if (collisions[i] == null || !collisions[i].equals(CollisionState.ALWAYS)) {
                    possiblePeople.put(dates[i], i);
                }
                allPeople.put(dates[i], i);
            }

            PersonDate[] peopleLeft = possiblePeople.keySet().toArray(new PersonDate[possiblePeople.size()]);

            if (newsh.isPlanningStillPossibleWithoutCertainPeopleGroups(token, units, Arrays.asList(peopleLeft), plannings,true)) {
                if(doLogBacktracker)
                    System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
                for (PersonDate pd : possiblePeople.keySet()) {
                    if (collisions[possiblePeople.get(pd)] == null) {
                        collisions[possiblePeople.get(pd)] = CollisionState.NEVER;
                    }
                }
            } else {
                if(doLogBacktracker)
                    System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
                for (PersonDate pd : possiblePeople.keySet()) {
                    if (collisions[possiblePeople.get(pd)] == null) {
                        collisions[possiblePeople.get(pd)] = CollisionState.UNKNOWN;
                    }
                }
            }
        }
        if(doLogBacktracker)
            System.out.println("einde " + sdf.format(new Timestamp(System.currentTimeMillis())));
        return collisions;
    }

    /**
     * This function calculates for a group of holiday messages which ones can be approved without a problem and which
     * ones can absolutely never be approved.
     * It also gives a handy list of the hm's that can not be approved together. One hm of the given lists can not be approved.
     *
     * It uses a rather complicated, but efficiënt algorithm. Each part has it's own comments inside the function.
     *
     * In principle we want to test every possible combination, but timewise, that is impossible. Therefore we need to
     * find all crashing groups in a clever manner.
     *
     * First test every hm together, if that doesn't crash, no combination will crash. You can stop here then.
     * Then test every hm on it's own, if it crashes on it's own, we know don't need to include them in further tests.
     * Now test every hm that doesn't crash on it's own together, if this doesn't crash, we can stop here.
     * At this point, there is a combination of multiple hm's that crashes.
     * To find them, we test the first two hm's together, if those doesn't crash, test the first three.
     * Keep expanding until the group crashes.
     * Of course, not every element in the group is responsible for the crash, we have to find those elements that form our first group.
     * To do that, we test the group without one element. We do this test for every but the last element.
     * If the group still crashes, we know that the element that was let out, does not cause the crash, it is not part of the group.
     * Every element that is let out, and the group does not crash anymore, is part of the group.
     * The last element is not tested, because that crashed the group in the first place, it is without a doubt in the group.
     *
     * Now we let the first element out of the group and expand further. Without the first element, the group doesn't crash
     * anymore because of the first group.
     *
     * Keep expanding, finding groups and letting the first element out, until the whole list has gone through.
     *
     * If there is no element that is part of multiple groups, every group is found.
     * If there is overlap, things get more complicated.
     *
     * If there is a group that consists of the first elements of other groups, they can never be found, because they
     * were always left out.
     * The idea now is simple. Let each element of each group out in turn.
     * First let the second groups second one out, then let the first his second and the second his first out ...
     *
     * When a new group is found, start over with the alternations, now with the extra group.
     * When all alternations are finished, all groups are found.
     *
     * @param token: the token provided by Axians
     * @param units: a list of all planning unit id's
     * @param lstartdate: the start date of the period to be investigated
     * @param lenddate: the end date of the period to be investigated
     * @return JsonNode
     *         {
     *             holidayMessages: the id's of hm's and their collision state.
     *                  [
     *                      {
     *                          id: id of the holidayMessage
     *                          CollisionState: "When does this hm crash the whole planning?"
     *                              ALWAYS: it crashes always, it can NOT be planned in
     *                              NEVER: it never crashes, it can be planned in without a problem
     *                              SOMETIMES: it only crashes when approved together with other hm's, see collisionGroups
     *                      },
     *                      ...
     *                  ]
     *             collisionGroups: groups of holiday messages that can not be approved together. All but one hm's in a group can be approved.
     *                  [
     *                      [
     *                          'id1',
     *                          'id2',
     *                          ...
     *                      ],
     *                      ...
     *                  ]
     *         }
     */
    private JsonNode getAllCollisionStatesAndGroupsForPlanningUnits(String token, List<String> units, LocalDate lstartdate, LocalDate lenddate) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        ObjectNode returnJson = Json.newObject();
        ArrayNode hmarray = returnJson.putArray("holidayMessages");
        ArrayNode collisionGroups = returnJson.putArray("collisionGroups");
        Map<String, PlanningLong> plannings = new HashMap<>();
        Map<Long, Integer> pdIdToIndex = new HashMap<>();
        Map<Integer, PersonDate> indexToPersonDate = new HashMap<>();
        for(String unit: units) {
            plannings.put(unit, ac.getOnCallPlanning(token, unit));
        }
        List<PlanningMember> plmembers;
        //Member[] members = ac.getMembersArray(token);
        Map<Long, PersonDate> IdToPd = new HashMap<>();
        plmembers = planningUnitHelper.getAllEmployeesOfPlanningUnitsWithIds(token, units);
        ScheduleHelper newsh = new ScheduleHelper(planningUnitHelper);
        newsh.ac = ac;
        newsh.hmController = hmController;

        List<HolidayMessage> allhms = getAllHolidayMessagesNewAndInConsideration(plmembers, lenddate, lstartdate);

        PersonDate[] dates = getAllPersonDatesFromHms(allhms);

        for(PersonDate pd: dates){
            IdToPd.put(pd.getId(), pd);
        }
        int functionCounter = 0;

        // If all absences together don't give a problem => everything can be planned in => everything status NEVER
        // If an absence crashes on itself, it will get the status ALWAYS
        // Now if we take the rest of the absences, without those that crash on their own,
        // if the group crashes, we can't know which combination crashes, they get the state UNKNOWN
        // else, they get the status NEVER
        CollisionState[] collisions = calculateCollisions(token, functionCounter, plannings, dates, sh, units);

        //All people that are NOT red => NOT state ALWAYS
        List<PersonDate> unknownPeople = new ArrayList<>();
        List<List<PersonDate>> foundGroups = new ArrayList<>();

        for(int i = 0; i < dates.length; i++){
            PersonDate pd = dates[i];
            indexToPersonDate.put(i, pd);
            pdIdToIndex.put(pd.getId(), i);
        }

        for(int i = 0; i < collisions.length; i++){
            if(collisions[i].equals(CollisionState.UNKNOWN)){
                unknownPeople.add(indexToPersonDate.get(i));
            }
        }

        Map<Integer, PersonDate> unknownIndexToPersonDate = new HashMap<>();
        for(int i = 0; i < unknownPeople.size(); i++){
            unknownIndexToPersonDate.put(i, unknownPeople.get(i));
        }

        // Now we take the smallest possible group (size 2) and let it expand until it crashes. When it does, we know
        // that we have a crashing subgroup in the total group.
        // We know let each element out of the subgroup. When it still crashes when that element is out of the group =>
        //      we know that that element is not part of the crashing subgroup.
        // So the crashing subgroup is formed by all elements that DON'T crash the group when they are taken out of it.
        // We don't have to test the last added element, since that crashed the group.
        // The above is true for the first run.
        // Now the second run, anything we add would crash the group, because the first found group is in it.
        // So for every new group, we let the first element that lets the group crash out.
        // Now we won't find every group, but we will find most.
        for(int i = 1; i < unknownPeople.size(); i++){
            // All people in the unknownGroup we are testing => the first i+1 hm's minus the first ones of each crashing group
            List<PersonDate> newFullGroup = new ArrayList<>();
            for(int j = 0; j <= i; j++){
                newFullGroup.add(unknownIndexToPersonDate.get(j));
            }
            for(List<PersonDate> pdlist: foundGroups){
                newFullGroup.remove(pdlist.get(0));
            }
            Map<Integer, PersonDate> indexToPersonDateFullGroup = new HashMap<>();
            for(int j = 0; j < newFullGroup.size(); j++){
                indexToPersonDateFullGroup.put(j, newFullGroup.get(j));
            }
            //If planning does not crash, just expand further, else test every element smaller than i;
            if(!newsh.isPlanningStillPossibleWithoutCertainPeopleGroups(token, units, newFullGroup, plannings, true)){
                if(doLogBacktracker)
                    System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
                List<PersonDate> crashingGroupFound = new ArrayList<>();
                for(int j = 0; j < newFullGroup.size() -1; j++){
                    List<PersonDate> subGroup = new ArrayList<>();
                    for(PersonDate pd: newFullGroup){
                        if(pd.getId() != indexToPersonDateFullGroup.get(j).getId()) {
                            subGroup.add(pd);
                        }
                    }
                    if(newsh.isPlanningStillPossibleWithoutCertainPeopleGroups(token, units, subGroup, plannings, true)){
                        if(doLogBacktracker)
                            System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
                        crashingGroupFound.add(indexToPersonDateFullGroup.get(j));
                    } else {
                        if(doLogBacktracker)
                            System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
                    }
                }
                crashingGroupFound.add(indexToPersonDateFullGroup.get(newFullGroup.size() -1));
                foundGroups.add(crashingGroupFound);
            } else {
                if(doLogBacktracker)
                    System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
            }
        }

        // In the previous function, we let out every first element of a group. If there would be an extra group that
        // contains the first element of a group, it could never be found.
        // Therefore, we have to test each possible permutation in which one element of each group is let out.
        // We could search again by expanding until the group crashes, but most groups we will make, won't contain
        // any extra groups. Now we test every group totally, and if that crashes, we will start small and expand again.
        List<List<PersonDate>> testedGroups = new ArrayList<>();

        PersonDate[] peopleToLetOut = findCrashingGroupLoop(newsh, foundGroups, new PersonDate[foundGroups.size()], foundGroups.size(), token, units, plannings, testedGroups, functionCounter, sdf, unknownPeople);
        while(peopleToLetOut != null){

            List<PersonDate> crashingGroupFound = new ArrayList<>();
            for(int i = 0; i < unknownPeople.size(); i++){
                List<PersonDate> subGroup = new ArrayList<>();
                for(PersonDate pd: unknownPeople){
                    if(!Arrays.asList(peopleToLetOut).contains(pd)) {
                        subGroup.add(pd);
                    }
                }
                subGroup.remove(unknownPeople.get(i));
                if(newsh.isPlanningStillPossibleWithoutCertainPeopleGroups(token, units, subGroup, plannings, true)){
                    if(doLogBacktracker)
                        System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
                    crashingGroupFound.add(unknownIndexToPersonDate.get(i));
                } else {
                    if(doLogBacktracker)
                        System.out.println(++functionCounter + " " + sdf.format(new Timestamp(System.currentTimeMillis())));
                }
            }
            foundGroups.add(crashingGroupFound);
            peopleToLetOut = findCrashingGroupLoop(newsh, foundGroups, new PersonDate[foundGroups.size()], foundGroups.size(), token, units, plannings, testedGroups, functionCounter, sdf, unknownPeople);
        }

        Set<Long> pdsInCollision = new HashSet<>();

        for(List<PersonDate> group: foundGroups){
            ArrayNode arr = collisionGroups.addArray();
            for(PersonDate pd: group){
                arr.add(pd.getId());
                pdsInCollision.add(pd.getId());
            }
        }

        for(int i = 0; i < collisions.length; i++){
            if(collisions[i].equals(CollisionState.UNKNOWN)){
                if(pdsInCollision.contains(indexToPersonDate.get(i).getId())){
                    collisions[i] = CollisionState.SOMETIMES;
                } else {
                    collisions[i] = CollisionState.NEVER;
                }
            }
            ObjectNode node = hmarray.addObject();
            node.put("id", indexToPersonDate.get(i).getId());
            node.put("CollisionState", collisions[i].toString());
        }

        return returnJson;
    }

    /**
     * Source: https://stackoverflow.com/questions/18165937/variable-number-of-nested-for-loops
     * Recursive construction to simulate a variable number of nested loops to find which people of each group have
     * to be let out to crash the group.
     *
     * It works kinda like this
     * for(every person in group 1)
     *      for(every person in group 2)
     *          for(every person in group 3)
     *              ...
     *                  for(every person in group n)
     *                      Test if the group crashes if those n people are let out
     *                      If it crashes, return all those people in an array. When these people are let out, we can find a new group!
     *
     * NOTE: this function uses a sort of cache. When we want to test a group that is a subgroup of another group
     *       that is already tested, we don't have to test it.
     *
     * @param sh: a ScheduleHelper object, this object is needed to test if a group crashes or not.
     * @param people: All groups of people that have to be tested
     * @param peopleToLetOut: the people to test of the previous iteration levels. (If we are in for loop 3, these are the people of loop 1 and 2)
     * @param level: the reverse level of recursion. The deepest level is 0.
     * @param token: the token provided by Axians.
     * @param units: a list of the id's of the planning units we want to test.
     * @param plannings: a map with as key the planning units id's to test and their PlanningLong objects as value.
     * @param testedGroups: a list of the groups that have been tested (for the cache functionality)
     * @param functionCounter: not used, can be used if something is wrong to count the amount of times "isPlanningStillPossibleWithoutCertainPeopleGroups" has been called
     * @param sdf: not used, can be used if something is wrong to display a timestamp in a certain manner.
     * @param unknownPeople: the full list of people to be tested.
     * @return the list of PersonDates that have to be let out to find a new group
     */
    private PersonDate[] findCrashingGroupLoop(ScheduleHelper sh, List<List<PersonDate>> people, PersonDate[] peopleToLetOut,
                                               int level, String token, List<String> units, Map<String, PlanningLong> plannings, List<List<PersonDate>> testedGroups, int functionCounter, SimpleDateFormat sdf,
                                               List<PersonDate> unknownPeople) {
        if (level == 0) { // terminating condition
            Set<PersonDate> groupToTest = new HashSet<>();
            for(PersonDate pd: unknownPeople){
                if(!Arrays.asList(peopleToLetOut).contains(pd)){
                    groupToTest.add(pd);
                }
            }
            if(!oneOfTheGroupsContainOtherGroup(testedGroups, new ArrayList<>(groupToTest))) {
                testedGroups.add(new ArrayList<>(groupToTest));
                if (!sh.isPlanningStillPossibleWithoutCertainPeopleGroups(token, units, new ArrayList<>(groupToTest), plannings, true)) {
                    return peopleToLetOut;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {// inductive condition
            for (int i = 0; i < people.get(level-1).size(); i++) {
                peopleToLetOut[level-1] = people.get(level-1).get(i);
                PersonDate[] returnArr = findCrashingGroupLoop(sh, people, peopleToLetOut, level-1, token, units, plannings, testedGroups, functionCounter, sdf, unknownPeople);
                if(returnArr != null){
                    return returnArr;
                }
            }
        }
        return null;
    }

    /**
     * Given a group of groups and a second group, check if the second group is a subgroup of one of the groups in the
     * group of groups.
     * All groups are lists of PersonDate objects.
     *
     * e.g. groupgroup = [1,2][2,3][2,3,4] and group2 = [3,4] => return true because group2 is part of the last group
     */
    private boolean oneOfTheGroupsContainOtherGroup(List<List<PersonDate>> groupgroup, List<PersonDate> group2){
        for(List<PersonDate> list1: groupgroup){
            if(firstGroupContainsOtherGroup(list1, group2)){
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the first group contains the second group.
     * e.g. group1 = [2,3,4] and group2 = [3,4] => return true because group2 is part of the first group.
     * The groups are lists of Person Date objects.
     */
    private boolean firstGroupContainsOtherGroup(List<PersonDate> group1, List<PersonDate> group2){
        for(PersonDate pd2: group2){
            if(!group1.contains(pd2)){
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates whether a valid planning can be made if certain people are let out.
     *
     * @param start: the start day of the to be investigated period in "yyyy-MM-dd"
     * @param end: the end day of the to be investigated period in "yyyy-MM-dd"
     * @param ids: the id's of the planning units to test.
     *
     * Request should contain a list of PersonDates.
     * @return 200 OK {isPossible: true/false}
     *         404 Not found when a planning unit id does not exist
     *         406 When another error occurs
     *         401 Unauthorized when person is not authorized.
     */
    public Result isPlanningStillPossibleForPlanningUnitsWithoutPeople(String start, String end, List<String> ids) {
        if(doLogRequests) {
            System.out.print("Sending 'GET' request to URL : /isPlanningStillPossibleForPlanningUnitsWithoutPeople/" + start + "/" + end + "?");
            for(String id: ids){
                System.out.print("ids="+id+"&");
            }
            System.out.println();
        }
        List<PersonDate> people = new ArrayList<>();
        JsonNode input = Json.parse(request().body().asText());
        for(JsonNode node: input){
            // If there is only one
            if(node.has("dates")){
                PersonDate pd = new PersonDate();
                pd.setEmployeeID(node.get("employeeID").textValue());
                pd.setId(node.get("id").asLong());
                List<ExactDate> eds = new ArrayList<>();
                for(JsonNode dates: node.get("dates")){
                    eds.add(new ExactDate(LocalDate.of(dates.get("date").get(0).asInt(), dates.get("date").get(1).asInt(), dates.get("date").get(2).asInt()),
                            DayPart.valueOf(dates.get("daypart").textValue())));
                }
                pd.setDates(eds);
                people.add(pd);
            }
        }

        LocalDate lstartdate = stringToDate(start);
        LocalDate lenddate = stringToDate(end);
        try {
            if (lstartdate == null || lenddate == null) {
                return notAcceptable();
            } else {
                lstartdate = stringToDate(start).minusDays(5);
                lenddate = stringToDate(end).plusDays(5);
            }
        } catch( Exception e){
            e.printStackTrace();
            return notAcceptable();
        }
        ObjectNode json = Json.newObject();
        try {
            json.put("isPossible", isPlanningStillPossibleForPlanningUnitsWithoutPeopleRequest(getToken(request()), ids, lstartdate, lenddate, people));
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
        return sendJSON(json);
    }

    /**
     * Auxiliary function for isPlanningStillPossibleForPlanningUnitsWithoutPeople
     * Is linked to a more performant function that splits the given people into groups that cannot affect each other.
     */
    private boolean isPlanningStillPossibleForPlanningUnitsWithoutPeopleRequest(String token, List<String> units, LocalDate lstartdate, LocalDate lenddate, List<PersonDate> people) throws Exception{
        Map<String, PlanningLong> plannings = new HashMap<>();
        for(String unit: units) {
            PlanningLong planning = ac.getOnCallPlanning(token, unit);
            plannings.put(unit, planning);
        }
        ScheduleHelper newsh = new ScheduleHelper(planningUnitHelper);
        newsh.ac = ac;
        newsh.hmController = hmController;
        return newsh.isPlanningStillPossibleWithoutCertainPeopleGroups(token, units, people, plannings, true);
        //return sh.isPlanningStillPossibleWithoutCertainPeople(token, units, lstartdate, lenddate, people,plannings, true);
    }

    /**
     * Return literally all holidayMessages in the database in a short format.
     *
     * @return 200 OK
     *         [
     *              {
     *                  id: id of the HM
     *                  exactDates
     *                  [
     *                      {
     *                           date
     *                                [
     *                                    'year': int yyyy
     *                                    'month': int m or mm
     *                                    'day': int d or dd
     *                                 ]
     *                           daypart: "AM" or "PM"
     *                           id: id of the exactDate
     *                       },
     *                       ...
     *                  ]
     *                  state: state of the request: APPROVED, NEW, IN CONSIDERATION or REJECTED
     *                  type: type of the absence: Educative, Yearly, Sickness, ...
     *              },
     *              ...
     *         ]
     *         401 Unauthorized when person is not authorized.
     *         406 Not acceptable when another error occurs.
     */
    public Result getAllHolidayMessagesShortOfEveryone(){
        try{
            if(doLogRequests)
                System.out.println("Sending 'GET' request to URL : /getAllHolidayMessagesShortOfEveryone");
            Planning[] plannings = ac.getOnCallPlanningsArray(getToken(request()));
            Set<String> members = new HashSet<>();
            for (Planning planning: plannings){
                for(PlanningMember pl: planning.getPlanningMembers()){
                    members.add(pl.getMember());
                }
            }
            ObjectNode returnJson = Json.newObject();
            for(String member: members){
                ObjectNode memberNode = returnJson.putObject(member);
                ArrayNode hms = memberNode.putArray("holidayMessages");
                for(HolidayMessage hm: hmController.getHolidayMessageWithEmployeeID(member)){
                    ObjectNode hmNode = hms.addObject();
                    hmNode.put("id", hm.getId());
                    hmNode.put("exactDates", Json.toJson(hm.getExactDates()));
                    hmNode.put("type", hm.getType().toString());
                    hmNode.put("state", hm.getState().toString());
                }
            }
            return sendJSON(returnJson);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Return all holidayMessage id's and states that are new or in consideration under the unitid's of the planner
     *
     * @return 200 OK
     *         [
     *              {
     *                  unitID: id of the planningUnit
     *                  holidayMessages
     *                      [
     *                          {
     *                              id: id of the HM
     *                              state: state of the HM (New or In Consideration)
     *                          },
     *                          ....
     *                      ]
     *               }
     *
     *         ]
     *         401 Unauthorized when person is not authorized.
     *         406 Not acceptable when another error occurs.
     */
    public Result getAllHMIDsPerPlanningUnitUnderPlannerWithID(String plannerID){
        try {
            String token = getToken(request());
            List<String> planningUnits = planningUnitHelper.getAllPlanningUnitIDsOfWhichEmployeeIDIsPlanner(token, plannerID);
            ArrayNode hmsPerPlanningUnit = Json.newArray();
            Map<String, ArrayNode> nodePerPUID = new HashMap<>();
            for(String planningUnit: planningUnits){
                ObjectNode node = hmsPerPlanningUnit.addObject();
                node.put("unitID", planningUnit);
                ArrayNode arrayNode = node.putArray("holidayMessages");
                nodePerPUID.put(planningUnit, arrayNode);
            }
            List<HolidayMessage> hms = hmController.getAllHolidayMessagesNew();
            hms.addAll(hmController.getAllHolidayMessagesInConsideration());
            for(HolidayMessage hm: hms){
                for(PlanningUnitState pus: hm.getPlanningUnitStates()){
                    if(planningUnits.contains(pus.getUnitId())){
                        if(pus.getState().equals(SchedulingState.New) || pus.getState().equals(SchedulingState.InConsideration)) {
                            ObjectNode hmNode = nodePerPUID.get(pus.getUnitId()).addObject();
                            hmNode.put("id", hm.getId());
                            hmNode.put("state", pus.getState().toString());
                        }
                    }
                }
            }
            return sendJSON(hmsPerPlanningUnit);
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }

    }

    /**
     * Simple auxiliary function that translates a String into a LocalDate
     *
     * @param dateString: formatted in "yyyy-MM-dd"
     *
     * @return LocalDate of the given dateString
     */

    private LocalDate stringToDate(String dateString){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date startdate;
            startdate = dateFormat.parse(dateString);
            LocalDate localDate = startdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return localDate;
        } catch (Exception E) {
            E.printStackTrace();
            return null;
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
