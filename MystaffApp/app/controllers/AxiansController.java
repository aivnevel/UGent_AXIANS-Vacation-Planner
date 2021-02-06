package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import exceptions.NoSuchIDException;
import exceptions.UnauthorizedException;
import exceptions.UnknownErrorException;
import language.TranslateService;
import models.*;
import models.planning.Planning;
import models.planning.PlanningLong;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

// Manages all communication with the MyStaff API
public class AxiansController extends Controller {

    private static final String LOGIN_URL = "https://mystaff.axians.be/rest/auth/login";
    private static final String MEMBER_DETAILS_URL = "https://mystaff.axians.be/rest/mystaff/onbase/user";
    private static final String PROFILE_SETTINGS_URL = "https://mystaff.axians.be/rest/mystaff/onbase/member/profile";
    private static final String ORGANIZATION_URL = "https://mystaff.axians.be/rest/mystaff/onbase/organizations/ugent2019";
    private static final String MEMBERS_URL = "https://mystaff.axians.be/rest/mystaff/onbase/members";
    private static final String ONCALL_PLANNINGS_URL = "https://mystaff.axians.be/rest/mystaff/oncall/plannings";
    private static final String ORGANIZATION_TYPES_URL = "https://mystaff.axians.be/rest/mystaff/onbase/organizationtypes";
    private static final String CREATE_ABSENCE_URL = "https://mystaff.axians.be/rest/mystaff/onbase/members/%userid%/absence";
    private static final String DELETE_ABSENCE_URL = "https://mystaff.axians.be/rest/mystaff/onbase/members/%userid%/absence/%absenceid%";
    private static final String ABSENCE_TYPES_URL = "https://mystaff.axians.be/rest/mystaff/onbase/absencetypes";
    private static final String ORGANIZATION_HOLIDAYS_URL = "https://mystaff.axians.be/rest/mystaff/onbase/organizations/%tenant%/holidays";
    private static final String PLANNING_UNITS_URL = "https://mystaff.axians.be/rest/mystaff/onbase/planning-units";
    private final String tenant;
    private final boolean doLog;

    @Inject
    public AxiansController(Config config){
        tenant = config.getString("axians.tenant");
        doLog = config.getBoolean("mystaff.doLogRequests");
    }

    // Use the same ObjectMapper in every method
    private static final ObjectMapper _mapper = new ObjectMapper();

    //
    // Summary:
    //      Is called from frontend when the user tries to log in. Passes the user credentials to the MyStaff API
    //      and returns its response.
    // Form Data:
    //      username:
    //          The user's login name.
    //      password:
    //          The user's password.
    // Returns:
    //      User credentials are valid:
    //          200 OK result with the token and refreshToken in the body as a JSON string.
    //      User credentials are invalid:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result login() {
        try {
            if(doLog)
                System.out.println("Sending 'POST' request to URL : /login");
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(LOGIN_URL, "POST",
                    "Content-Type", "application/json", "X-Requested-With", "XMLHttpRequest", "Accept", "application/json", "Host", "mystaff.axians.be", "Content-Length", "55");

            if (con == null) return internalServerError();

            String username;
            String password;
            Map<String, String[]> formData = request().body().asMultipartFormData().asFormUrlEncoded();

            Base64.Decoder decoder = Base64.getDecoder();
            username = new String(decoder.decode(formData.get("username")[0]));
            password = new String(decoder.decode(formData.get("password")[0]));

            String JSON = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
            byte[] outputInBytes = JSON.getBytes("UTF-8");

            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(outputInBytes);
            os.close();

            switch (con.getResponseCode()) {
                case 200: {
                    JsonNode jsonNode = Json.parse(getResponse(con));
                    return ok(jsonNode).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the logged in user's data from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with user's data in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getUser() {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/user");
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(MEMBER_DETAILS_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the logged in user's absences from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with user's absence data in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getUserAbsences() {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /me/absences");
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(MEMBERS_URL, "GET",
                    "Content-Type", "application/json", "tenant", tenant, "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 401:
                    return unauthorized();
                case 500:
                    return internalServerError();
            }

            Member[] members = _mapper.readValue(getResponse(con), Member[].class);
            User user = getUserObject(getToken(request()));

            switch (con.getResponseCode()) {
                case 200: {
                    Member currentMember = Arrays.stream(members).filter(x -> x.getId().equals(user.getUserId())).collect(Collectors.toList()).get(0);
                    Absence[] currentAbsences = currentMember.getAbsences();
                    JsonNode node = Json.toJson(currentAbsences);
                    return ok(node).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the logged in user's absences from the MyStaff API from the back end.
    //
    // @param token: Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Returns:
    //      Array of absences of that user.
    //      Null if an error occurs.

    public Absence[] getUserAbsences(String token){
        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(MEMBERS_URL, "GET",
                    "Content-Type", "application/json", "tenant", tenant, "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return null;

            switch (con.getResponseCode()) {
                case 401:
                    return null;
                case 500:
                    return null;
            }

            Member[] members = _mapper.readValue(getResponse(con), Member[].class);
            User user = getUserObject(token);

            switch (con.getResponseCode()) {
                case 200: {
                    Member currentMember = Arrays.stream(members).filter(x -> x.getId().equals(user.getUserId())).collect(Collectors.toList()).get(0);
                    Absence[] currentAbsences = currentMember.getAbsences();
                    return currentAbsences;
                }
                default: {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    //
    // Summary:
    //      Submit a new absence created by a user to the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Parameters:
    //      type:
    //          Type of the absence.
    //      begin:
    //          Begin date of the absence.
    //      end:
    //          End date of the absence.
    //      dailyStartTime:
    //          Indicates when during the day the absence begins.
    //      dailyEndTime:
    //          Indicates when during the day the absence ends.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          201 Created result with newly created absence's id in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 201 or 401:
    //          500 Internal Server Error result.
    public Result postUserAbsences() {
        try {
            if(doLog)
                System.out.println("Sending 'POST' request to URL : /absences");
            String token = getToken(request());
            User user = getUserObject(token);

            String type = "";
            String begin = "";
            String end = "";
            String dailyStartTime = "";
            String dailyEndTime = "";
            final Set<Map.Entry<String, String[]>> entries = request().queryString().entrySet();
            for (Map.Entry<String, String[]> entry : entries) {
                final String key = entry.getKey();
                final String value = Arrays.toString(entry.getValue());
                switch (key) {
                    case "type":
                        type = value.replace("[", "").replace("]", "");
                        break;
                    case "begin":
                        begin = value.replace("[", "").replace("]", "");
                        break;
                    case "end":
                        end = value.replace("[", "").replace("]", "");
                        break;
                    case "dailyStartTime":
                        dailyStartTime = value.replace("[", "").replace("]", "");
                        break;
                    case "dailyEndTime":
                        dailyEndTime = value.replace("[", "").replace("]", "");
                        break;
                    default:
                        break;
                }
            }

            JsonNode jsonArr = Json.newArray();
            ObjectNode empNode = ((ArrayNode) jsonArr).addObject();
            empNode.put("type", getType(type));
            empNode.put("begin", begin);
            empNode.put("end", end);
            empNode.put("dailyStartTime", dailyStartTime);
            empNode.put("dailyEndTime", dailyEndTime);

            //System.out.println(empNode);

            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(CREATE_ABSENCE_URL.replace("%userid%", user.getUserId()), "POST",
                    "Content-Type", "application/json", "tenant", tenant, "X-Authorization", token);

            if (con == null) return internalServerError();

            con.setDoOutput(true);
            con.getOutputStream().write(empNode.toString().getBytes("UTF-8"));

            switch (con.getResponseCode()) {
                case 201: {
                    JsonNode node = Json.parse(getResponse(con));
                    return created(node).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Submit a new absence created by a user to the MyStaff API using the back end.
    //
    // @param token: Token that is sent to the MyStaff API to identify the current logged in user.
    // @param absence: The absence object to be sent.
    // @param userid: The userid of the employee that has to get an absence.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          201
    //      Invalid X-Authorization token:
    //          401
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 201 or 401:
    //          500
    public int postUserAbsence(String token, JsonNode jsonNode, String userid){
        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(CREATE_ABSENCE_URL.replace("%userid%", userid), "POST",
                    "Content-Type", "application/json", "tenant", tenant, "X-Authorization", token);
            if (con == null) return 500;
            con.setDoOutput(true);
            con.getOutputStream().write(jsonNode.toString().getBytes("UTF-8"));
            if(con.getResponseCode() != 201 && con.getResponseCode() != 401){
                System.err.println(con.getResponseCode());
                System.err.println(con.getResponseMessage());
                return 500;
            }
            return con.getResponseCode();

        } catch(Exception e){
            e.printStackTrace();
            return 500;
        }
    }

    //
    // Summary:
    //      Deletes the absence of a user from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          204 No Content result.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 204 or 401:
    //          500 Internal Server Error result.
    public Result deleteUserAbsence(String userId, String absenceId) {
        try {
            if(doLog)
                System.out.println("Sending 'DELETE' request to URL : /" + userId + "/absences/" + absenceId);
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(DELETE_ABSENCE_URL
                            .replace("%userid%", userId)
                            .replace("%absenceid%", absenceId),
                    "DELETE",
                    "Content-Type", "application/json", "tenant", tenant, "X-Authorization", getToken(request()), "Accept", "application/json", "Host", "mystaff.axians.be");

            if (con == null) return internalServerError();
            con.setDoOutput(true);

            switch (con.getResponseCode()) {
                case 204: {
                    return noContent();
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Deletes the absence of a user from the MyStaff API from the back end.
    //
    // @param token: Token that is sent to the MyStaff API to identify the current logged in user.
    // @param absenceId: The ID of the absence object to be deleted.
    // @param userid: The userid of the employee that has to delete an absence.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          204
    //      Invalid X-Authorization token:
    //          401
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 204 or 401:
    //          500
    public int deleteUserAbsence(String token, String absenceId, String userId) {
        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(DELETE_ABSENCE_URL
                            .replace("%userid%", userId)
                            .replace("%absenceid%", absenceId),
                    "DELETE",
                    "Content-Type", "application/json", "tenant", tenant, "X-Authorization", getToken(request()), "Accept", "application/json", "Host", "mystaff.axians.be");

            if (con == null) return 500;
            con.setDoOutput(true);

            if(con.getResponseCode() != 201 && con.getResponseCode() != 401){
                return 500;
            }
            return con.getResponseCode();

        } catch (Exception e) {
            e.printStackTrace();
            return 500;
        }
    }

    //
    // Summary:
    //      Retrieves the holidays for the specified tenant from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Parameters:
    //      userId:
    //          The user's id.
    //      tenant:
    //          Tenant that is used to make the request to the MyStaff API.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with holidays data in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getOrganizationHolidays(String tenant) {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/holidays/" + tenant);
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ORGANIZATION_HOLIDAYS_URL.replace("%tenant%", tenant), "GET",
                    "Content-Type", "application/json", "tenant", tenant, "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the members for the specified tenant from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    //  Parameters:
    //      tenant:
    //          The tenant of the users.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with the members data in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getMembers() {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/members");
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(MEMBERS_URL, "GET",
                    "Content-Type", "application/json", "tenant", tenant, "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the profile settings from the logged in user from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with the profile settings data in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getProfileSettings() {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/profilesettings");
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(PROFILE_SETTINGS_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the organization's data from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with the organization's data in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getOrganization() {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/organization");
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ORGANIZATION_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the planning's data for the specified tenant from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with the plannings's data in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getPlannings() {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/plannings");
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ONCALL_PLANNINGS_URL, "GET",
                    "tenant", tenant, "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the organization types from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with the organization types in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getOrganizationTypes() {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/organizationtypes");
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ORGANIZATION_TYPES_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the absence types from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with the absence types in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getAbsenceTypes() {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/absencetypes");
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ABSENCE_TYPES_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the planning units from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with the planning units in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getPlanningUnits(String tenant) {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/planningUnits/" + tenant);
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(PLANNING_UNITS_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()), "tenant", tenant);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves the planning unit from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Parameters:
    //      tenant:
    //          The tenant of the users.
    //      id:
    //          Id of the planning unit.
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with the planning unit in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getPlanningUnit(String tenant, String id) {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/planningUnit/" + tenant + "/" + id);
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(PLANNING_UNITS_URL + "/" + id, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization",  getToken(request()), "tenant", tenant);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    //
    // Summary:
    //      Retrieves all planning data that is associated with the id from the MyStaff API.
    //
    // Headers:
    //      X-Authorization:
    //          Token that is sent to the MyStaff API to identify the current logged in user.
    //
    // Parameters:
    //      id:
    //          Id of the planning unit
    //
    // Returns:
    //      Valid X-Authorization token:
    //          200 OK result with the planning data that is associated with the id in the body as a JSON string.
    //      Invalid X-Authorization token:
    //          401 Unauthorized result.
    //      An Exception was thrown or MyStaff API responded with an HTTP code that is not 200 or 401:
    //          500 Internal Server Error result.
    public Result getPlanning(String id) {
        try {
            if(doLog)
                System.out.println("Sending 'GET' request to URL : /api/planning/" + id );
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ONCALL_PLANNINGS_URL + "?id=" + id, "GET",
                    "tenant", tenant, "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", getToken(request()));

            if (con == null) return internalServerError();

            String response = getResponse(con);
            if (response.equals("[]"))
                return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(response).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    // Object getters

    public User getUserObject(String token) throws UnauthorizedException, UnknownErrorException, Exception {
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(MEMBER_DETAILS_URL, "GET",
                "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                User user = _mapper.readValue(getResponse(con), User.class);
                return user;
            }
            default: {
                throw new UnknownErrorException();
            }
        }
    }

    public Member[] getMembersArray(String token) throws UnauthorizedException, UnknownErrorException, Exception {
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(MEMBERS_URL, "GET",
                    "Content-Type", "application/json", "tenant", tenant, "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                Member[] member = _mapper.readValue(getResponse(con), Member[].class);
                return member;
            }
            default: {
                throw new UnknownErrorException();
            }
        }
    }

    public ProfileSettings getProfileSettingsObject(String token) throws UnauthorizedException, UnknownErrorException, Exception {
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(PROFILE_SETTINGS_URL, "GET",
                "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                ProfileSettings profileSettings = _mapper.readValue(getResponse(con), ProfileSettings.class);
                return profileSettings;
            }
            default: {
                throw new UnknownErrorException();
            }
        }
    }

    public Organization getOrganizationObject(String token) throws UnauthorizedException, UnknownErrorException, Exception {
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ORGANIZATION_URL, "GET",
                    "Content-Type", "application/json", "tenant", tenant, "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                Organization organization = _mapper.readValue(getResponse(con), Organization.class);
                return organization;
            }
            default: {
                throw new UnknownErrorException();
            }
        }
    }

    public Planning[] getOnCallPlanningsArray(String token) throws UnauthorizedException, UnknownErrorException, Exception {
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ONCALL_PLANNINGS_URL, "GET",
                    "tenant", tenant, "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                Planning[] plannings = _mapper.readValue(getResponse(con), Planning[].class);
                return plannings;
            }
            default: {
                throw new UnknownErrorException();
            }
        }
    }

    public String[] getOrganizationTypesArray(String token) throws UnauthorizedException, UnknownErrorException, Exception {
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ORGANIZATION_TYPES_URL, "GET",
                "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                String[] organizationTypes = _mapper.readValue(getResponse(con), String[].class);
                return organizationTypes;
            }
            default: {
                throw new UnknownErrorException();
            }
        }
    }

    public String[] getAbsenceTypesArray(String token) throws UnauthorizedException, UnknownErrorException, Exception {
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ABSENCE_TYPES_URL, "GET",
                "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                String[] absenceTypes = _mapper.readValue(getResponse(con), String[].class);
                return absenceTypes;
            }
            default: {
                throw new UnknownErrorException();
            }
        }
    }

    public Holiday[] getOrganizationHolidaysArray(String token) throws UnauthorizedException, UnknownErrorException, Exception{
        User user = getUserObject(token);
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ORGANIZATION_HOLIDAYS_URL.replace("%tenant%", user.getTenant()), "GET",
                    "Content-Type", "application/json", "tenant", user.getTenant(), "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                Holiday[] holidays = _mapper.readValue(getResponse(con), Holiday[].class);
                return holidays;
            }
            default: {
                throw new UnknownErrorException();
            }
        }
    }

    public PlanningUnitShort[] getPlanningUnitsShortArray(String token) throws UnauthorizedException, UnknownErrorException, Exception{
        User user = getUserObject(token);
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(PLANNING_UNITS_URL, "GET",
                "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token, "tenant", user.getTenant());
        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                PlanningUnitShort[] plannings = _mapper.readValue(getResponse(con), PlanningUnitShort[].class);
                return plannings;
            }
            default: {
                throw new UnknownErrorException();
            }
        }

    }

    public PlanningUnit getPlanningUnitObject(String token, String id) throws UnauthorizedException, UnknownErrorException, Exception, NoSuchIDException {
        User user = getUserObject(token);
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(PLANNING_UNITS_URL + "/" + id, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token, "tenant", user.getTenant());

        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                PlanningUnit planning = _mapper.readValue(getResponse(con), PlanningUnit.class);
                if(planning == null){
                    throw new NoSuchIDException("PlanningUnit with id " + id + " does not exist.");
                }
                return planning;
            }
            default: {
                throw new UnknownErrorException();
            }
        }
    }

    public PlanningLong getOnCallPlanning(String token, String id) throws UnauthorizedException, UnknownErrorException, Exception, NoSuchIDException {
        HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ONCALL_PLANNINGS_URL + "?id=" + id, "GET",
                "tenant", tenant, "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

        if (con == null) throw new UnknownErrorException();
        switch (con.getResponseCode()) {
            case 401: {
                throw new UnauthorizedException();
            }
            case 201: case 200: {
                try {
                    String response = getResponse(con).replace("\n", "").replace("\r", "");
                    //System.out.println(response);
                    PlanningLong[] planning = _mapper.readValue(response, PlanningLong[].class);
                    if(planning == null){
                        throw new NoSuchIDException("PlanningUnit with id " + id + " does not exist.");
                    }
                    return planning[0];
                } catch(Exception e){
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    //e.printStackTrace();
                }

            }
            default: {
                throw new UnknownErrorException();
            }
        }

    }

    // Helper methods

    public boolean isAuthorised(String token){
        try{
            getUserObject(token);
            return true;
        } catch(Exception e){
            return false;
        }
    }

    public String getToken(Http.Request request) {
        return "Bearer " + request.getHeaders().toMap().get("token").get(0);
    }

    private String getResponse(HttpsURLConnection con) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (IOException ex) {

        }
        return "";
    }

    private HttpsURLConnection getConnectionToWithMethodAndHeaders(String address, String method, String... headers) {
        if (headers.length % 2 != 0) return null;
        if (!(method.equals("GET") || method.equals("POST") || method.equals("DELETE"))) return null;

        try {
            URL url = new URL(address);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod(method);

            for (int i = 0; i < headers.length; i += 2) {
                con.setRequestProperty(headers[i], headers[i + 1]);
            }

            return con;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getType(String type) {
        System.err.println(type);
        switch (type) {
            case "Jaarlijks verlof":
                return "YEARLY_VACATION";
            case "Europees verlof":
                return "EUROPEAN_VACATION";
            case "Educatief verlof":
                return "EDUCATIONAL_VACATION";
            default:
                return "OTHER";
        }
    }

    ///////////////////////////////////////////// Under this line, all functions have the same functionality of these
    ///////////////////////////////////////////// above, but they are meant to be used in tests.
    public Result loginTest(String username, String password, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(LOGIN_URL, "POST",
                    "Content-Type", "application/json", "X-Requested-With", "XMLHttpRequest", "Accept", "application/json", "Host", "mystaff.axians.be", "Content-Length", "55");

            if (con == null) return internalServerError();

            String JSON = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
            byte[] outputInBytes = JSON.getBytes("UTF-8");

            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(outputInBytes);
            os.close();

            switch (con.getResponseCode()) {
                case 200: {
                    JsonNode jsonNode = Json.parse(getResponse(con));
                    return ok(jsonNode).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getUserTest(String token, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(MEMBER_DETAILS_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getUserAbsencesTest(String token, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(MEMBERS_URL, "GET",
                    "Content-Type", "application/json", "tenant", tenant, "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 401:
                    return unauthorized();
                case 500:
                    return internalServerError();
            }

            Member[] members = _mapper.readValue(getResponse(con), Member[].class);
            User user = getUserObject(token);

            switch (con.getResponseCode()) {
                case 200: {
                    Member currentMember = Arrays.stream(members).filter(x -> x.getId().equals(user.getUserId())).collect(Collectors.toList()).get(0);
                    Absence[] currentAbsences = currentMember.getAbsences();
                    JsonNode node = Json.toJson(currentAbsences);
                    return ok(node).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getOrganizationHolidaysTest(String token, String tenant, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {

            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ORGANIZATION_HOLIDAYS_URL.replace("%tenant%", tenant), "GET",
                    "Content-Type", "application/json", "tenant", tenant, "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getMembersTest(String token, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(MEMBERS_URL, "GET",
                    "Content-Type", "application/json", "tenant", tenant, "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getProfileSettingsTest(String token, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(PROFILE_SETTINGS_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getOrganizationTest(String token, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ORGANIZATION_URL, "GET",
                    "Content-Type", "application/json", "tenant", tenant, "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getPlanningsTest(String token, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ONCALL_PLANNINGS_URL, "GET",
                    "tenant", tenant, "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getOrganizationTypesTest(String token, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ORGANIZATION_TYPES_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getAbsenceTypesTest(String token, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ABSENCE_TYPES_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getPlanningUnitsTest(String token, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            User user = getUserObject(token);
            if (user == null)
                return unauthorized();

            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(PLANNING_UNITS_URL, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token, "tenant", user.getTenant());

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getPlanningUnitTest(String token, String id, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            User user = getUserObject(token);
            if (user == null)
                return unauthorized();
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(PLANNING_UNITS_URL + "/" + id, "GET",
                    "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token, "tenant", user.getTenant());

            if (con == null) return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(getResponse(con)).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result getPlanningTest(String token, String id, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();

        try {
            HttpsURLConnection con = getConnectionToWithMethodAndHeaders(ONCALL_PLANNINGS_URL + "?id=" + id, "GET",
                    "tenant", tenant, "Content-Type", "application/json", "Accept", "application/json", "Host", "mystaff.axians.be", "X-Authorization", token);

            if (con == null) return internalServerError();

            String response = getResponse(con);
            if (response.equals("[]"))
                return internalServerError();

            switch (con.getResponseCode()) {
                case 200: {
                    return ok(response).as("application/json");
                }
                case 401: {
                    return unauthorized();
                }
                case 500: {
                    return internalServerError();
                }
                default: {
                    return internalServerError();
                }
            }
        } catch (Exception e) {
            return internalServerError();
        }
    }

    public Result isPlannerTest(String token, boolean internalServerError) {

        if (internalServerError)
            return internalServerError();
        try {
            User user = getUserObject(token);

            boolean isPlanner = false;
            PlanningUnitShort[] plannings = getPlanningUnitsShortArray(token);
            PlanningUnit unit = getPlanningUnitObject(token, plannings[plannings.length - 1].getId());
            if (unit.getOnCallPlanners().contains(user.getUserId()))
                isPlanner = true;
            return ok(isPlanner + "");
        } catch (Exception e){
            return unauthorized();
        }
    }
}
