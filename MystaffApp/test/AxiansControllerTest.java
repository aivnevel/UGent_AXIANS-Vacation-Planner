import com.fasterxml.jackson.databind.JsonNode;
import controllers.AxiansController;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class AxiansControllerTest {

    @Inject
    private AxiansController ac;

    private static final String VALID_USERNAME = "rodzwaef";
    private static final String VALID_PASSWORD = "vakantie";
    private static final String VALID_TENANT = "ugent2019";
    private static final String VALID_PLANNING_UNIT_ID = "cc38bea0-92c9-44d8-91ca-3b14c0716ed2";
    private static final String VALID_ONCALL_PLANNING_UNIT_ID = "74451708-a011-4744-88a6-2e854c68a85f";
    private static final String INVALID_USERNAME = "";
    private static final String INVALID_PASSWORD = "";
    private static final String INVALID_TOKEN = "";
    private static final String INVALID_TENANT = "";
    private static final String INVALID_PLANNING_UNIT_ID = "abcdefghijklmnopqrstuvwxyz";
    private static final String INVALID_ONCALL_PLANNING_UNIT_ID = "abcdefghijklmnopqrstuvwxyz";

    @Test
    public void loginTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        assertEquals("Logging in with valid credentials should return status 200", 200, result.status());

        result = ac.loginTest(INVALID_USERNAME, INVALID_PASSWORD, false);
        assertEquals("Logging in with invalid credentials should return status 401", 401, result.status());

        result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, true);
        assertEquals("Logging in with valid credentials, but force an internal server error, should return status 500", 500, result.status());

        result = ac.loginTest(INVALID_USERNAME, INVALID_PASSWORD, true);
        assertEquals("Logging in with invalid credentials, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getUserTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getUserTest(token, false);
        assertEquals("Getting a user with a valid token should return status 200", 200, result.status());

        result = ac.getUserTest(INVALID_TOKEN, false);
        assertEquals("Getting a user with an invalid token should return status 401", 401, result.status());

        result = ac.getUserTest(token, true);
        assertEquals("Getting a user with a valid token, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getUserTest(INVALID_TOKEN, true);
        assertEquals("Getting a user with an invalid token, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getUserAbsencesTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getUserAbsencesTest(token, false);
        assertEquals("Getting a user's absences with a valid token should return status 200", 200, result.status());

        result = ac.getUserAbsencesTest(INVALID_TOKEN, false);
        assertEquals("Getting a user's absences with an invalid token should return status 401", 401, result.status());

        result = ac.getUserAbsencesTest(token, true);
        assertEquals("Getting a user's absences with a valid token, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getUserAbsencesTest(INVALID_TOKEN, true);
        assertEquals("Getting a user's absences with an invalid token, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getOrganizationHolidaysTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getOrganizationHolidaysTest(token, VALID_TENANT, false);
        assertEquals("Getting an organization's holidays with a valid token and tenant should return status 200", 200, result.status());

        result = ac.getOrganizationHolidaysTest(token, INVALID_TENANT, false);
        assertEquals("Getting an organization's holidays with a valid token and invalid tenant should return status 500", 500, result.status());

        result = ac.getOrganizationHolidaysTest(INVALID_TOKEN, VALID_TENANT, false);
        assertEquals("Getting an organization's holidays with an invalid token and valid tenant should return status 401", 401, result.status());

        result = ac.getOrganizationHolidaysTest(INVALID_TOKEN, INVALID_TENANT, false);
        assertEquals("Getting an organization's holidays with an invalid token and tenant should return status 500", 500, result.status());

        result = ac.getOrganizationHolidaysTest(token, VALID_TENANT, true);
        assertEquals("Getting an organization's holidays with a valid token and tenant, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getOrganizationHolidaysTest(INVALID_TOKEN, VALID_TENANT, true);
        assertEquals("Getting an organization's holidays with an invalid token and valid tenant, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getOrganizationHolidaysTest(token, INVALID_TENANT, true);
        assertEquals("Getting an organization's holidays with a valid token and invalid tenant, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getOrganizationHolidaysTest(INVALID_TOKEN, INVALID_TENANT, true);
        assertEquals("Getting an organization's holidays with an invalid token and tenant, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getMemberTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getMembersTest(token, false);
        assertEquals("Getting the members with a valid token should return status 200", 200, result.status());

        result = ac.getMembersTest(INVALID_TOKEN, false);
        assertEquals("Getting the members with an invalid token should return status 401", 401, result.status());

        result = ac.getMembersTest(token, true);
        assertEquals("Getting the members with a valid token, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getMembersTest(INVALID_TOKEN, true);
        assertEquals("Getting the members with an invalid token, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getProfileSettingsTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getProfileSettingsTest(token, false);
        assertEquals("Getting your profile settings with a valid token should return status 200", 200, result.status());

        result = ac.getProfileSettingsTest(INVALID_TOKEN, false);
        assertEquals("Getting your profile settings with an invalid token should return status 401", 401, result.status());

        result = ac.getProfileSettingsTest(token, true);
        assertEquals("Getting your profile settings with a valid token, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getProfileSettingsTest(INVALID_TOKEN, true);
        assertEquals("Getting your profile settings with an invalid token, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getOrganizationTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getOrganizationTest(token, false);
        assertEquals("Getting the organization's details with a valid token should return status 200", 200, result.status());

        result = ac.getOrganizationTest(INVALID_TOKEN, false);
        assertEquals("Getting the organization's details with an invalid token should return status 401", 401, result.status());

        result = ac.getOrganizationTest(token, true);
        assertEquals("Getting the organization's details with a valid token, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getOrganizationTest(INVALID_TOKEN, true);
        assertEquals("Getting the organization's details with an invalid token, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getPlanningsTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getPlanningsTest(token, false);
        assertEquals("Getting the organization's plannings with a valid token should return status 200", 200, result.status());

        result = ac.getPlanningsTest(INVALID_TOKEN, false);
        assertEquals("Getting the organization's plannings with an invalid token should return status 401", 401, result.status());

        result = ac.getPlanningsTest(token, true);
        assertEquals("Getting the organization's plannings with a valid token, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getPlanningsTest(INVALID_TOKEN, true);
        assertEquals("Getting the organization's plannings with an invalid token, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getOrganizationTypesTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getOrganizationTypesTest(token, false);
        assertEquals("Getting the organization's plannings with a valid token should return status 200", 200, result.status());

        result = ac.getOrganizationTypesTest(INVALID_TOKEN, false);
        assertEquals("Getting the organization's plannings with an invalid token should return status 401", 401, result.status());

        result = ac.getOrganizationTypesTest(token, true);
        assertEquals("Getting the organization's plannings with a valid token, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getOrganizationTypesTest(INVALID_TOKEN, true);
        assertEquals("Getting the organization's plannings with an invalid token, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getAbsenceTypesTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getAbsenceTypesTest(token, false);
        assertEquals("Getting the organization's plannings with a valid token should return status 200", 200, result.status());

        result = ac.getAbsenceTypesTest(INVALID_TOKEN, false);
        assertEquals("Getting the organization's plannings with an invalid token should return status 401", 401, result.status());

        result = ac.getAbsenceTypesTest(token, true);
        assertEquals("Getting the organization's plannings with a valid token, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getAbsenceTypesTest(INVALID_TOKEN, true);
        assertEquals("Getting the organization's plannings with an invalid token, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getPlanningUnitsTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getPlanningUnitsTest(token, false);
        assertEquals("Getting the planning units with a valid token should return status 200", 200, result.status());

        result = ac.getPlanningUnitsTest(INVALID_TOKEN, false);
        assertEquals("Getting the planning units with an invalid token should return status 200", 401, result.status());

        result = ac.getPlanningUnitsTest(token, true);
        assertEquals("Getting the planning units with a valid token, but force an internal server error should return status 200", 500, result.status());

        result = ac.getPlanningUnitsTest(INVALID_TOKEN, true);
        assertEquals("Getting the planning units with an invalid token, but force an internal server error should return status 200", 500, result.status());
    }

    @Test
    public void getPlanningUnitTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getPlanningUnitTest(token, VALID_PLANNING_UNIT_ID, false);
        assertEquals("Getting a planning unit with a valid token and id should return status 200", 200, result.status());

        result = ac.getPlanningUnitTest(token, INVALID_PLANNING_UNIT_ID, false);
        System.out.println(Helpers.contentAsString(result));
        assertEquals("Getting a planning unit with a valid token and invalid id should return status 500", 500, result.status());

        result = ac.getPlanningUnitTest(INVALID_TOKEN, VALID_PLANNING_UNIT_ID, false);
        assertEquals("Getting a planning unit with an invalid token and valid id should return status 401", 401, result.status());

        result = ac.getPlanningUnitTest(INVALID_TOKEN, INVALID_PLANNING_UNIT_ID, false);
        assertEquals("Getting a planning unit with an invalid token and id should return status 401", 401, result.status());

        result = ac.getPlanningUnitTest(token, VALID_PLANNING_UNIT_ID, true);
        assertEquals("Getting a planning unit with a valid token and id, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getPlanningUnitTest(INVALID_TOKEN, VALID_PLANNING_UNIT_ID, true);
        assertEquals("Getting a planning unit with an invalid token and valid id, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getPlanningUnitTest(token, INVALID_PLANNING_UNIT_ID, true);
        assertEquals("Getting a planning unit with a valid token and invalid id, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getPlanningUnitTest(INVALID_TOKEN, INVALID_PLANNING_UNIT_ID, true);
        assertEquals("Getting a planning unit with an invalid token and id, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void getPlanningTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.getPlanningTest(token, VALID_ONCALL_PLANNING_UNIT_ID, false);
        assertEquals("Getting a planning with a valid token and id should return status 200", 200, result.status());

        result = ac.getPlanningTest(token, INVALID_ONCALL_PLANNING_UNIT_ID, false);
        System.out.println(Helpers.contentAsString(result));
        assertEquals("Getting a planning with a valid token and invalid id should return status 500", 500, result.status());

        result = ac.getPlanningTest(INVALID_TOKEN, VALID_ONCALL_PLANNING_UNIT_ID, false);
        assertEquals("Getting a planning with an invalid token and valid id should return status 401", 401, result.status());

        result = ac.getPlanningTest(INVALID_TOKEN, INVALID_ONCALL_PLANNING_UNIT_ID, false);
        assertEquals("Getting a planning with an invalid token and id should return status 401", 401, result.status());

        result = ac.getPlanningTest(token, VALID_ONCALL_PLANNING_UNIT_ID, true);
        assertEquals("Getting a planning with a valid token and id, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getPlanningTest(INVALID_TOKEN, VALID_ONCALL_PLANNING_UNIT_ID, true);
        assertEquals("Getting a planning with an invalid token and valid id, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getPlanningTest(token, INVALID_ONCALL_PLANNING_UNIT_ID, true);
        assertEquals("Getting a planning with a valid token and invalid id, but force an internal server error, should return status 500", 500, result.status());

        result = ac.getPlanningTest(INVALID_TOKEN, INVALID_ONCALL_PLANNING_UNIT_ID, true);
        assertEquals("Getting a planning with an invalid token and id, but force an internal server error, should return status 500", 500, result.status());
    }

    @Test
    public void isPlannerTest() {
        Result result = ac.loginTest(VALID_USERNAME, VALID_PASSWORD, false);
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        String token = "Bearer " + node.get("token").asText();

        result = ac.isPlannerTest(token, false);
        assertEquals("Asking if you are a planner with a valid token should return status 200", 200, result.status());
        assertEquals("Asking if you are a planner with a valid token should have 'true' in the response body",Helpers.contentAsString(result), "true");

        result = ac.isPlannerTest(INVALID_TOKEN, false);
        assertEquals("Asking if you are a planner with an invalid token should return status 401", 401, result.status());

        result = ac.isPlannerTest(token, true);
        assertEquals("Asking if you are a planner with a valid token, but force an internal server error should return status 500", 500, result.status());

        result = ac.isPlannerTest(INVALID_TOKEN, true);
        assertEquals("Asking if you are a planner with an invalid token, but force an internal server error should return status 500", 500, result.status());
    }

}
