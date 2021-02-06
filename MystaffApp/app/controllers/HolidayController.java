package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import org.joda.time.DateTime;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilityClasses.HolidaysBelgium;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/*
 * HolidayController does everything that has to do with official Belgian holidays.
 * It can return the holidays between two dates or the dates of a holiday (period)
 */

public class HolidayController extends Controller {

    private final boolean doLog;
    private final AxiansController axiansController;

    @Inject
    public HolidayController(Config config, AxiansController axiansController){
        doLog = config.getBoolean("mystaff.doLogRequests");
        this.axiansController = axiansController;
    }

    /**
     * Returns one holiday that falls between two dates. If no holiday is between the two dates, returns ""
     * Multiple cases:
     *      - Both dates are in the same holiday period => this holiday period is returned
     *      - Start date is in a holiday period, end date is out of it => holiday period is returned
     *      - In the chosen period is a holiday period and also a holiday => return holiday period
     *      - Start date is end date and the day is part of a period and it is a holiday => return holiday
     *              (happens in easter break when it's easter)
     *
     *
     * @param start the start date of the period we want to check
     * @param end the end date of the period we want to check
     * @return 200 OK
     * {
     *     vacation: name of the holiday or "" if no holidays are in the period
     * }
     *         406 Not acceptable when date is not formatted correctly
     */
    public Result getHolidayBetweenTwoDates(String start, String end) {
        if(axiansController.isAuthorised(getToken(request()))) {
            try {
                if (doLog)
                    System.out.println("Sending 'GET' request to URL : /getHolidayBetweenTwoDates/" + start + "/" + end);
                DateTime start1 = new DateTime(new SimpleDateFormat("dd-MM-yyyy").parse(start));
                DateTime end1 = new DateTime(new SimpleDateFormat("dd-MM-yyyy").parse(end));
                HolidaysBelgium holidaysBelgium = new HolidaysBelgium(start1.getYear());
                String vacation = holidaysBelgium.getHolidaysAndVacationsBetweenDates(start1.toDate(), end1.toDate());
                ObjectNode objectNode = Json.newObject();
                objectNode.put("vacation", vacation);
                return sendJSON(objectNode);
            } catch (ParseException e) {
                e.printStackTrace();
                return notAcceptable("Date must be formatted in \"dd-MM-yyyy\"");
            }
        } else {
            return unauthorized();
        }
    }

    /**
     * Returns the start and end date of a certain holiday if it is known. If a not known holiday is given, start and
     * end are both ""
     *
     * @param vacation: the name of the holiday
     * @param date: a random date used to give the year we want to know the holiday in. Today is used often.
     *
     * @return 200
     * {
     *     start: start of holiday period in "yyyy-MM-dd HH:mm:ss"
     *     end: end of holiday period in "yyyy-MM-dd HH:mm:ss"
     * }
     *         406 Not acceptable when date is not formatted correctly
     */
    public Result getDatesOfHoliday(String vacation, String date){
        if(axiansController.isAuthorised(getToken(request()))) {
            try {
                if (doLog)
                    System.out.println("Sending 'GET' request to URL : /getDatesOfHoliday/" + vacation + "/" + date);
                DateTime dateTime = new DateTime(new SimpleDateFormat("dd-MM-yyyy").parse(date));
                int year = dateTime.getYear();
                HolidaysBelgium holidaysBelgium = new HolidaysBelgium(year);
                Date[] dates = holidaysBelgium.getDatesOfHoliday(vacation);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String start = "";
                String end = "";
                try {
                    start = dateFormat.format(dates[0]);
                    end = dateFormat.format(dates[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ObjectNode objectNode = Json.newObject();
                objectNode.put("start", start);
                objectNode.put("end", end);
                return sendJSON(objectNode);
            } catch (ParseException e) {
                e.printStackTrace();
                return notAcceptable("Date is not in the right format \"dd-MM-yyyy\"");
            }
        } else {
            return unauthorized();
        }
    }

    /**
     * Returns all the vacations in a year (not the holidays, they are retrieved from the web api)
     * Format is an array of {vacation: [startDate, endDate]} objects
     */

    public Result getVacationsOfYear(int year){
        if(axiansController.isAuthorised(getToken(request()))) {
            if (doLog)
                System.out.println("Sending 'GET' request to URL : /getVacationsOfYear/" + year);
            HolidaysBelgium holidaysBelgium = new HolidaysBelgium(year);
            Map<String, Date[]> vacationsBelgium = holidaysBelgium.getVacationsBelgium();
            ArrayNode arrayNode = Json.newArray();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Map.Entry<String, Date[]> entry : vacationsBelgium.entrySet()) {
                ObjectNode objectNode = arrayNode.addObject();
                ArrayNode dates = objectNode.putArray(entry.getKey());
                for (Date d : entry.getValue()) {
                    dates.add(dateFormat.format(d));
                }
            }
            return sendJSON(arrayNode);
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
