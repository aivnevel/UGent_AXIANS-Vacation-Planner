package persistence;

import businessLogic.AbsenceCounter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers.AxiansController;
import controllers.MailController;
import enumerations.DayPart;
import enumerations.HolidayType;
import enumerations.SchedulingState;
import exceptions.NoSuchIDException;
import exceptions.UnauthorizedException;
import models.Absence;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilityClasses.*;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DatabaseController extends Controller {
    @Inject
    private HolidayMessageController holidayMessageController;
    @Inject
    private SettingsController settingsController;
    @Inject
    private AxiansController axiansController;
    @Inject
    private MailController mailController;
    @Inject
    private AbsenceCounterController absenceCounterController;

    private double possibleDaysOfAbsence;
    //private String url = "https://localhost:9000/";
    private String url = "/";
    private static boolean doSend;
    private static boolean doLog;
    private static boolean doSendToPlanner;

    @Inject
    public DatabaseController(Config config){
        possibleDaysOfAbsence = config.getInt("axians.standardMaxAbsenceDays");
        doSend = config.getBoolean("mails.doSend");
        doSendToPlanner = config.getBoolean("mails.doSendToPlanner");
        doLog = config.getBoolean("mystaff.doLogRequests");
    }


    private static final ObjectMapper _mapper = new ObjectMapper();

    /**
     *   Get the absenceCounters for a certain year for a list of people, usually used for one person.
     *
     *   Returns*   200 OK
     *   {
     *       totalPossibleDays: double that contains the default number of absence days for a person.
     *       [
     *           {
     *               employeeID: the ID of the employee.
     *               maxDaysPossibleThisYear: double that contains the maximum of days that can be used this year.
     *                           => This value is defaulted in application.conf via axians.standardMaxAbsenceDays
     *               daysLeftApproved: double that contains the maximum of days - the days that are approved.
     *               daysLeftApprNewInCons: double that contains the maximum of days - all absences that are approved
     *                   and not approved (not the rejected)
     *           }
     *       ]
     *   }
     *
     */
    public Result getAbsenceCounters(int year, List<String> employeeIDs){
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog) {
                System.out.print("Sending 'GET' request to URL : /getAbsenceCounters/" + year + "/?");
                for (String id : employeeIDs) {
                    System.out.print("employeeIDs=" + id + "&");
                }
                System.out.println();
            }
            double approvedDays = 0;
            double possibleDays = 0;
            ObjectNode returnJson = Json.newObject();
            returnJson.put("totalPossibleDays", possibleDaysOfAbsence);
            ArrayNode arr = returnJson.putArray("counters");
            for (String id : employeeIDs) {
                AbsenceCounter ac = absenceCounterController.getAbsenceCounterWithId(id);
                if (ac == null) {
                    ac = new AbsenceCounter(id, possibleDaysOfAbsence);
                }
                approvedDays = 0;
                possibleDays = 0;
                ObjectNode node = arr.addObject();
                node.put("employeeID", id);
                for (HolidayMessage hm : holidayMessageController.getHolidayMessageWithEmployeeID(id)) {
                    for (ExactDate ed : hm.getExactDates()) {
                        if (ed.getDate().getYear() == year) {
                            if (hm.getState().equals(SchedulingState.Approved)) {
                                approvedDays += 0.5;
                                possibleDays += 0.5;
                            } else if (!hm.getState().equals(SchedulingState.Rejected)) {
                                possibleDays += 0.5;
                            }
                        }
                    }
                }
                node.put("daysLeftApproved", ac.getMaxDaysThisYear() - approvedDays);
                node.put("daysLeftApprNewInCons", ac.getMaxDaysThisYear() - possibleDays);
                node.put("maxDaysPossibleThisYear", ac.getMaxDaysThisYear());
                if (ac.getLastUpdate() != null)
                    node.put("lastUpdate", ac.getLastUpdate().toString());
                if (ac.getLastComment() != null)
                    node.put("lastComment", ac.getLastComment());
            }
            return sendJSON(returnJson);
        } else {
            return unauthorized();
        }
    }


    /**
     * Post the absenceCounter of an employee to the database. This is the max number of days an employee can be
     * absent in a year.
     *
     * Body should be an absenceCounter object
     *
     * Returns 200 OK if everything is okay
     * {
     *   employeeID: ID of the employee that holds the number.
     *   maxDaysThisYear: Max days an employee can be absent this year.
     * }
     * Returns 500 Internal server error if there was an error posting.
     *
     */
    public Result postAbsenceCounter() {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'POST' request to URL : /postAbsenceCounter");
            JsonNode json = Json.parse(request().body().asText());
            try {
                AbsenceCounter ac = new AbsenceCounter(json.get("employeeID").asText(), json.get("maxDaysThisYear").asDouble());
                ac.setLastComment(json.get("lastComment").asText());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                if (json.has("lastUpdate")) {
                    try {
                        ac.setLastUpdate(format.parse(json.get("lastUpdate").textValue()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                absenceCounterController.postAbsenceCounter(ac);
            } catch (Exception e) {
                e.printStackTrace();
                return internalServerError("Could not post");
            }
            return ok();
        } else {
            return unauthorized();
        }
    }

    /**
     * Delete a AbsenceCounter object out of the database for a certain employee.
     *
     * @param id is the employeeID of the person to which this AbsenceCounter is linked.
     */

    public Result deleteAbsenceCounter(String id){
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'DELETE' request to URL : /deleteAbsenceCounter");
            absenceCounterController.deleteAbsenceCounterFromEmployeeWithId(id);
            return ok();
        } else {
            return unauthorized();
        }
    }


    /**
     * Get a Settings object from the database.
     * These are used to save the configuration of the filters etc. at the front end.
     *
     * Returns 200 OK Settings object in Json if object was found.
     *        404 Not found if object was not found
     */
    public Result getSettingsFromEmployeeWithId(String id){
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getSettingsFromEmployeeWithId/" + id);
            Settings message = settingsController.getSettingsFromEmployeeWithId(id);
            if (message == null) {
                System.err.println("Cound not find SettingsObject with id: " + id + " not found.");
                return notFound("SettingsObject with id: " + id + " not found.");
            }
            JsonNode json = Json.toJson(message);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     * Post a Settings object to the database.
     *
     * Body should be a Settings object in JSON.
     */
    public Result postSettings() {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'POST' request to URL : " + url + "settings");
            JsonNode json = Json.parse(request().body().asText());
            String id = "error";
            try {
                Settings settings = _mapper.treeToValue(json, Settings.class);
                id = settingsController.addSettingsFromEmployeeWithId(settings);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (id.equals("error")) {
                return internalServerError("Could not post settings.");
            }
            return ok(Json.toJson(id));
        } else {
            return unauthorized();
        }
    }

    /**
     * Delete a Settings object out of the database for a certain employee.
     *
     * @param id is the employeeID of the person to which these settings are linked.
     */

    public Result deleteSettings(String id){
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'DELETE' request to URL : " + url + "deleteSettings");
            settingsController.deleteSettingsFromEmployeeWithId(id);
            return ok();
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage in the database.
     */
    public Result getAllHolidayMessages() {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getAllHolidayMessages");
            List<HolidayMessage> messages = holidayMessageController.getAllHolidayMessages();
            JsonNode json = Json.toJson(messages);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get the holidayMessage in the database with a certain ID.
     */
    public Result getHolidayMessageWithDatabaseID(long id) {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getHolidayMessageWithDatabaseID/" + id);
            HolidayMessage message = holidayMessageController.getHolidayMessageWithDatabaseID(id);
            if (message == null) {
                System.err.println("HolidayMessage with id: " + id + " not found.");
                return notFound("HolidayMessage with id: " + id + " not found.");
            }
            JsonNode json = Json.toJson(message);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage in the database of a certain employee.
     */
    public Result getHolidayMessageWithEmployeeID(String id) {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getHolidayMessageWithEmployeeID/" + id);
            List<HolidayMessage> message = holidayMessageController.getHolidayMessageWithEmployeeID(id);
            if (message.isEmpty()) {
                System.err.println("No HolidayMessages found for employee with id: " + id + ".");
                return notFound("No HolidayMessages found for employee with id: " + id + ".");
            }
            JsonNode json = Json.toJson(message);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage that is approved in the database of a certain employee.
     */
    public Result getHolidayMessagesApprovedOfDoctorWithID(String id) {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getHolidayMessagesApprovedOfDoctorWithID/" + id);
            List<HolidayMessage> messages = holidayMessageController.getHolidayMessagesApprovedOfDoctorWithID(id);
            return sendJSON(Json.toJson(messages));
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage that is rejected in the database of a certain employee.
     */
    public Result getHolidayMessagesRejectedOfDoctorWithID(String id) {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getHolidayMessagesRejectedOfDoctorWithID/" + id);
            List<HolidayMessage> messages = holidayMessageController.getHolidayMessagesRejectedOfDoctorWithID(id);
            JsonNode json = Json.toJson(messages);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage that is in consideration in the database of a certain employee.
     */
    public Result getHolidayMessagesInConsiderationOfDoctorWithID(String id) {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getHolidayMessagesInConsiderationOfDoctorWithID/" + id);
            List<HolidayMessage> messages = holidayMessageController.getHolidayMessagesInConsiderationOfDoctorWithID(id);
            JsonNode json = Json.toJson(messages);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage that is new in the database of a certain employee.
     */
    public Result getHolidayMessagesNewOfDoctorWithID(String id) {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getHolidayMessagesNewOfDoctorWithID/" + id);
            List<HolidayMessage> messages = holidayMessageController.getHolidayMessagesNewOfDoctorWithID(id);
            JsonNode json = Json.toJson(messages);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage that is approved in the database.
     */
    public Result getAllHolidayMessagesApproved() {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getAllHolidayMessagesApproved");
            List<HolidayMessage> messages = holidayMessageController.getAllHolidayMessagesApproved();
            JsonNode json = Json.toJson(messages);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage that is rejected in the database.
     */
    public Result getAllHolidayMessagesRejected() {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getAllHolidayMessagesRejected");
            List<HolidayMessage> messages = holidayMessageController.getAllHolidayMessagesRejected();
            JsonNode json = Json.toJson(messages);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage that is new in the database.
     */
    public Result getAllHolidayMessagesNew() {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getAllHolidayMessagesNew");
            List<HolidayMessage> messages = holidayMessageController.getAllHolidayMessagesNew();
            JsonNode json = Json.toJson(messages);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage that is in consideration in the database.
     */
    public Result getAllHolidayMessagesInConsideration() {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getAllHolidayMessagesInConsideration");
            List<HolidayMessage> messages = holidayMessageController.getAllHolidayMessagesInConsideration();
            JsonNode json = Json.toJson(messages);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage that is new or in consideration in the database.
     */
    public Result getAllHolidayMessagesNewOrInConsideration() {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getAllHolidayMessagesNewOrInConsideration");
            List<HolidayMessage> messages = holidayMessageController.getAllHolidayMessagesNew();
            messages.addAll(holidayMessageController.getAllHolidayMessagesInConsideration());
            JsonNode json = Json.toJson(messages);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     *   Get every holidayMessage of a certain type.
     */
    public Result getAllHolidayMessagesByType(String typeString) {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getAllHolidayMessagesByType");
            List<HolidayMessage> messages = holidayMessageController.getAllHolidayMessagesByType(HolidayType.valueOf(typeString));
            JsonNode json = Json.toJson(messages);
            return sendJSON(json);
        } else {
            return unauthorized();
        }
    }

    /**
     * Post a certain HolidayPlanningsMessage.
     * First make the Json into a HolidayPlanningsMessage object
     * Then add this update to the database for every planning unit this planner is planner of.
     * Then if wanted, send a mail to the employee about his approved/rejected absence.
     *      (no mail is sent when the global state is still new or in consideration, when not all planners have reacted)
     * Then an absence is sent to Axians, if the global state changes to approved when this message is saved.
     *
     * Returns 200 OK with the ID of the posted hpm if everything is okay.
     * Returns 500 Internal server error when Axians couldn't be reached. In this case, the planningsMessage is saved
     *             with state NEW and Comment: "There was an error".
     */
    public Result postHolidayPlanningsMessage() {
        try{
            if(doLog)
                System.out.println("Sending 'POST' request to URL : " + url + "holidayPlanningsMessage");
            JsonNode json = Json.parse(request().body().asText());
            HolidayPlanningsMessage hpm = new HolidayPlanningsMessage();
            if (json.has("comment"))
                hpm.setComment(json.get("comment").textValue());
            hpm.setId(json.get("id").numberValue().longValue());
            hpm.setPlannerID(json.get("plannerID").textValue());
            hpm.setState(SchedulingState.valueOf(json.get("state").textValue()));
            holidayMessageController.addPlannerUpdate(getToken(request()), hpm);
            if(doSend) {
                mailController.sendMailNewHolidayPlanningsMessages(getToken(request()), hpm);
            }
            // Add absence to Axians system.
            HolidayMessage hm = HolidayMessage.find.byId(hpm.getId());
            if(hm.getState().equals(SchedulingState.Approved)){
                List<ExactDate> exactDates = hm.getExactDates();
                try {
                    Collections.sort(exactDates);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                String dailyStartTime;
                String dailyEndTime;
                if(exactDates.size() > 1 && exactDates.get(0).getDate() == exactDates.get(1).getDate()){
                    dailyStartTime = "09:00";
                    dailyEndTime = "17:00";
                } else if(exactDates.get(0).getDaypart().equals(DayPart.AM)){
                    dailyStartTime = "09:00";
                    dailyEndTime = "13:00";
                } else {
                    dailyStartTime = "13:00";
                    dailyEndTime = "17:00";
                }
                String begin = LocalDateToString(exactDates.get(0).getDate());
                String end = LocalDateToString(exactDates.get(exactDates.size()-1).getDate());
                ObjectNode empNode = Json.newObject();
                empNode.put("type", formatType(hm.getType().toString()));
                empNode.put("begin", begin);
                empNode.put("end", end);
                empNode.put("dailyStartTime", dailyStartTime);
                empNode.put("dailyEndTime", dailyEndTime);
                int errorCode = axiansController.postUserAbsence(getToken(request()), empNode, hm.getEmployeeID());
                if(errorCode != 201 && errorCode != 200){
                    // Redo the modification
                    hpm.setComment("There was an error.");
                    hpm.setState(SchedulingState.New);
                    holidayMessageController.addPlannerUpdate(getToken(request()), hpm);
                    return internalServerError("Could not post to Axians.");
                }
            }
            return ok(hpm.getId() + "");
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Simple translation function
     * HolidayMessage type => absence type
     */
    private String formatType(String type) {
        if (type == "Yearly") {
            return "YEARLY_VACATION";
        } else if (type == "European") {
            return "EUROPEAN_VACATION";
        } else if (type == "Educative") {
            return "EDUCATIONAL_VACATION";
        } else {
            return "OTHER";
        }
    }

    /**
     * This is the same function as postHolidayPlanningsMessage, but only for a single planningUnit.
     */
    public Result postHolidayPlanningsMessageForPlanningsUnit(String unit) {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'POST' request to URL : " + url + "postHolidayPlanningsMessageForPlanningsUnit");
            JsonNode json = Json.parse(request().body().asText());
            HolidayPlanningsMessage hpm = new HolidayPlanningsMessage();
            if (json.has("Comment"))
                hpm.setComment(json.get("comment").textValue());
            hpm.setId(json.get("id").numberValue().longValue());
            hpm.setPlannerID(json.get("plannerID").textValue());
            hpm.setState(SchedulingState.valueOf(json.get("state").textValue()));
            List<String> list = new ArrayList<>();
            list.add(unit);
            holidayMessageController.addPlannerUpdateForPlanningsUnits(hpm, list);
            if (doSend) {
                mailController.sendMailNewHolidayPlanningsMessages(getToken(request()), hpm);
            }
            // Add absence to Axians system.
            HolidayMessage hm = HolidayMessage.find.byId(hpm.getId());
            if (hm.getState().equals(SchedulingState.Approved)) {
                List<ExactDate> exactDates = hm.getExactDates();
                Collections.sort(exactDates);
                String dailyStartTime;
                String dailyEndTime;
                if (exactDates.size() > 1 && exactDates.get(0).getDate() == exactDates.get(1).getDate()) {
                    dailyStartTime = "09:00";
                    dailyEndTime = "17:00";
                } else if (exactDates.get(0).getDaypart().equals(DayPart.AM)) {
                    dailyStartTime = "09:00";
                    dailyEndTime = "13:00";
                } else {
                    dailyStartTime = "13:00";
                    dailyEndTime = "17:00";
                }
                String begin = LocalDateToString(exactDates.get(0).getDate());
                String end = LocalDateToString(exactDates.get(exactDates.size() - 1).getDate());
                ObjectNode empNode = Json.newObject();
                empNode.put("type", formatType(hm.getType().toString()));
                empNode.put("begin", begin);
                empNode.put("end", end);
                empNode.put("dailyStartTime", dailyStartTime);
                empNode.put("dailyEndTime", dailyEndTime);
                if (axiansController.postUserAbsence(getToken(request()), empNode, hm.getEmployeeID()) != 201) {
                    // Redo the modification
                    hpm.setComment("There was an error.");
                    hpm.setState(SchedulingState.New);
                    holidayMessageController.addPlannerUpdateForPlanningsUnits(hpm, list);
                    return internalServerError("Could not post to Axians.");
                }
            }
            return ok(hpm.getId() + "");
        } else {
            return unauthorized();
        }
    }

    /**
     * Post a HolidayMessage to the database
     * First convert the Json to a HolidayMessage
     * Then save it
     * Then send a confirmation email to the employee
     * If this is an update to an absence that was already approved, delete it again from the Axians servers.
     *
     */
    public Result postHolidayMessage() {
        try{
            if(doLog)
                System.out.println("Sending 'POST' request to URL : " + url + "holidayMessage");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            JsonNode json = Json.parse(request().body().asText());
            HolidayMessage hm = new HolidayMessage();
            //HolidayMessage hm = Json.fromJson(json, HolidayMessage.class);
            hm.setRequestByID(json.get("requestByID").textValue());
            if (json.has("lastUpdate"))
                try {
                    hm.setLastUpdate(format.parse(json.get("lastUpdate").textValue()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            try {
                hm.setRequestDate(format.parse(json.get("requestDate").textValue()));
            } catch (Exception e) {
                //e.printStackTrace();
                hm.setRequestDate(new Date());
            }
            if (json.has("id")) {
                hm.id = json.get("id").numberValue().longValue();
            }
            hm.setType(HolidayType.valueOf(json.get("type").textValue()));
            hm.setEmployeeID(json.get("employeeID").textValue());
            hm.setState(SchedulingState.valueOf(json.get("state").textValue()));
            if (json.has("comment"))
                hm.setComment(json.get("comment").textValue());
            hm.setExactDates(new ArrayList<ExactDate>());
            for (JsonNode exdate : json.get("exactDates")) {
                try {
                    if (!exdate.get("date").isArray()) {
                        try {
                            Date d = format.parse(exdate.get("date").textValue());
                            LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            ExactDate date = new ExactDate(ld, DayPart.valueOf(exdate.get("dayPart").textValue()));
                            hm.getExactDates().add(date);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        ArrayNode arr = (ArrayNode) exdate.get("date");
                        LocalDate ld = LocalDate.of(
                                arr.get(0).numberValue().intValue(),
                                arr.get(1).numberValue().intValue(),
                                arr.get(2).numberValue().intValue());
                        ExactDate ed = new ExactDate(ld, DayPart.valueOf(exdate.get("daypart").textValue()));
                        hm.getExactDates().add(ed);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();

                }
            }
            Long id = -1L;
            if (holidayMessageController.isAlreadyInDatabase(hm)) {
                holidayMessageController.updateHolidayMessage(hm);
            } else {
                id = holidayMessageController.addHolidayMessage(getToken(request()), hm);
            }
            if(doSend) {
                if (hm.getHistory().isEmpty()) {
                    if (hm.getEmployeeID().equals(hm.getRequestByID())) {
                        mailController.sendMailNewHolidayMessageBySelf(getToken(request()), hm);
                    } else {
                        mailController.sendMailNewHolidayMessageByPlanner(getToken(request()), hm);
                    }
                    if(doSendToPlanner){
                        mailController.sendMailNewHolidayMessageToPlanner(getToken(request()), hm);
                    }
                } else {
                    mailController.sendMailModificationHolidayMessage(getToken(request()), hm);
                }
            }

            if(hm.getHistory().size() != 0) {
                // Delete absence at axians if it was approved
                Absence[] absences = axiansController.getUserAbsences(getToken(request()));
                HolidayMessageHistory previoushm = hm.getHistory().get(hm.getHistory().size()-1);
                List<ExactDateHMH> exactDateHMHS = previoushm.getExactDates();
                Collections.sort(exactDateHMHS);
                for(Absence absence: absences){
                    if(absence.getInterval().getBeginDate().equals(LocalDateToString(exactDateHMHS.get(0).getDate())) &&
                            absence.getInterval().getEndDate().equals(LocalDateToString(exactDateHMHS.get(exactDateHMHS.size()-1).getDate()))){
                        axiansController.deleteUserAbsence(getToken(request()), absence.getId(), hm.getEmployeeID());
                    }
                }
            }
            return ok(id + "");
        } catch(UnauthorizedException e){
            return unauthorized();
        } catch(NoSuchIDException e){
            return notFound(e.getMessage());
        } catch(Exception e){
            return notAcceptable();
        }
    }

    /**
     * Delete a holidayMessage
     * If it was approved, delete it from the Axians servers.
     * Also send a mail to the employee about the deletion.
     */
    public Result deleteHolidayMessage(long id) {
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'DELETE' request to URL : " + url + "holidayMessage");
            HolidayMessage hm = null;
            hm = HolidayMessage.find.byId(id);

            // Delete it from axians
            if (hm.getState().equals(SchedulingState.Approved)) {
                Absence[] absences = axiansController.getUserAbsences(getToken(request()));
                List<ExactDate> exactDate = hm.getExactDates();
                Collections.sort(exactDate);
                for (Absence absence : absences) {
                    if (absence.getInterval().getBeginDate().equals(LocalDateToString(exactDate.get(0).getDate())) &&
                            absence.getInterval().getEndDate().equals(LocalDateToString(exactDate.get(exactDate.size() - 1).getDate()))) {
                        axiansController.deleteUserAbsence(getToken(request()), absence.getId(), hm.getEmployeeID());
                    }
                }
            }
            if (doSend) {
                mailController.sendMailDeleteMessage(getToken(request()), hm);
            }
            holidayMessageController.deleteHolidayMessage(id);
            return ok();
        } else {
            return unauthorized();
        }
    }

    /**
     * Simple auxiliary function to send a JsonNode.
     */
    private Result sendJSON(JsonNode jsonNode) {
        try {
            return ok(jsonNode).as("application/json");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return notFound();
    }

    /**
     * Get the number of holidayMessages (int) that are rejected since a certain date of a certain person.
     *
     * @param id: the employeeID
     * @param date: date to search from "yyyy-mm-dd". e.g. "2019-04-28"
     */
    public Result getNumberOfTimesPersonIsRejectedSince(String id, String date){
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : " + url + "getNumberOfTimesPersonIsRejectedSince/" + id + "/" + date);
            LocalDate start = stringToDate(date);
            int numberOfRejected = 0;
            for (HolidayMessage hm : holidayMessageController.getAllHolidayMessagesOfEmployeeAfter(id, start)) {
                if (hm.getState().equals(SchedulingState.Rejected)) {
                    numberOfRejected++;
                }
            }
            return ok(Json.toJson(numberOfRejected));
        } else {
            return unauthorized();
        }
    }

    /**
     * Simple auxiliary function to get token out of request.
     */
    private String getToken(Http.Request request) {
        return "Bearer " + request.getHeaders().toMap().get("token").get(0);
    }

    /**
     * Simple translation function
     * yyyy-mm-dd => LocalDate
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
     * Simple translation function
     * LocalDate => yyyy-mm-dd
     */
    private String LocalDateToString(LocalDate date){
        String monthZero = date.getMonth().getValue() < 10 ? "0" : "";
        String dayZero = date.getDayOfMonth() < 10 ? "0" : "";
        return date.getYear() + "-" + monthZero + date.getMonth().getValue() + "-" + dayZero + date.getDayOfMonth();
    }
}
