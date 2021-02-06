import com.google.common.collect.ImmutableMap;
import enumerations.DayPart;
import enumerations.HolidayType;
import enumerations.SchedulingState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import persistence.DatabaseController;
import play.db.Database;
import play.db.Databases;
import utilityClasses.ExactDate;
import utilityClasses.HolidayMessage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;


public class DatabaseTest {

    Database database;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Random r;

    @Before
    public void createDatabase() {
        database = Databases.createFrom(
                "org.mariadb.jdbc.Driver",
                "jdbc:mysql://192.168.99.100:3306/iiidb",
                ImmutableMap.of(
                        "username", "user",
                        "password", "pass"
                )
        );

        r = new Random();
    }

    @Test
    public void insertHolidayMessageAndExactDatesTest() throws ParseException {
        clearDatabase();

        Date date = format.parse("2019-04-20 09:00:00");
        HolidayMessage h = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.New, HolidayType.Sickness, date, date, "This is a Unit test.");

        List<ExactDate> dates = newExactDates(5, h);


        try (Connection con = database.getConnection()) {

            con.prepareStatement(toInsertQuery(h)).execute();

            for (ExactDate e : dates) {
                con.prepareStatement(toInsertQuery(e)).execute();
            }

            ResultSet rsH = con.prepareStatement("SELECT * FROM holiday_message").executeQuery();
            ResultSet rsE = con.prepareStatement("SELECT * FROM exact_date").executeQuery();

            int msgCount = 0;
            int dateCount = 0;

            while (rsH.next()) {
                msgCount++;
            }

            while (rsE.next()) {
                dateCount++;
            }

            assertEquals(1, msgCount);
            assertEquals(5, dateCount);

        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    @Test
    public void insertAndGetHolidayMessageTest() throws ParseException {
        clearDatabase();
        Date date = format.parse("2019-04-20 09:00:00");
        HolidayMessage h = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.New, HolidayType.Sickness, date, date, "This is a Unit test.");

        try (Connection con = database.getConnection()) {

            con.prepareStatement(toInsertQuery(h)).execute();
            ResultSet rs = con.prepareStatement((getHolidayMessageQuery(h))).executeQuery();

            rs.next();
            assertEquals(h.id, rs.getInt("id"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void insertAndGetMultipleMessagesTest() throws ParseException {
        clearDatabase();
        Date date = format.parse("2019-04-20 09:00:00");

        List<HolidayMessage> messages = new ArrayList<>();

        try (Connection con = database.getConnection()) {
            for (int i = 0; i < 20; i++) {
                messages.add(newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef " + i, "robin-de-zwaef " + i,
                        SchedulingState.New, HolidayType.Sickness, date, date, "This is a Unit test. "));
                con.prepareStatement(toInsertQuery(messages.get(i))).execute();
            }

            for (int i = 0; i < 20; i++) {
                ResultSet rs = con.prepareStatement(getHolidayMessageQuery(messages.get(i))).executeQuery();
                rs.next();
                assertEquals(rs.getInt("id"), messages.get(i).id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void holidayMessageSearchByTypeTest() throws ParseException {
        clearDatabase();
        Date date = format.parse("2019-04-20 09:00:00");

        HolidayMessage h1 = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.New, HolidayType.Sickness, date, date, "This is a Unit test.");
        HolidayMessage h2 = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.New, HolidayType.Educative, date, date, "This is a Unit test.");
        HolidayMessage h3 = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.New, HolidayType.European, date, date, "This is a Unit test.");
        HolidayMessage h4 = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.New, HolidayType.Yearly, date, date, "This is a Unit test.");
        HolidayMessage h5 = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.New, HolidayType.Other, date, date, "This is a Unit test.");

        try (Connection con = database.getConnection()) {

            con.prepareStatement(toInsertQuery(h1)).execute();
            con.prepareStatement(toInsertQuery(h2)).execute();
            con.prepareStatement(toInsertQuery(h3)).execute();
            con.prepareStatement(toInsertQuery(h4)).execute();
            con.prepareStatement(toInsertQuery(h5)).execute();

            ResultSet r1 = con.prepareStatement(getHolidayMessageByTypeQuery(h1)).executeQuery();
            ResultSet r2 = con.prepareStatement(getHolidayMessageByTypeQuery(h2)).executeQuery();
            ResultSet r3 = con.prepareStatement(getHolidayMessageByTypeQuery(h3)).executeQuery();
            ResultSet r4 = con.prepareStatement(getHolidayMessageByTypeQuery(h4)).executeQuery();
            ResultSet r5 = con.prepareStatement(getHolidayMessageByTypeQuery(h5)).executeQuery();

            r1.next();
            r2.next();
            r3.next();
            r4.next();
            r5.next();

            assertEquals(r1.getInt("id"), h1.id);
            assertEquals(r2.getInt("id"), h2.id);
            assertEquals(r3.getInt("id"), h3.id);
            assertEquals(r4.getInt("id"), h4.id);
            assertEquals(r5.getInt("id"), h5.id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void holidayMessageSearchByStateTest() throws ParseException {
        clearDatabase();
        Date date = format.parse("2019-04-20 09:00:00");

        HolidayMessage h1 = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.New, HolidayType.Sickness, date, date, "This is a Unit test.");
        HolidayMessage h2 = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.InConsideration, HolidayType.Educative, date, date, "This is a Unit test.");
        HolidayMessage h3 = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.Rejected, HolidayType.European, date, date, "This is a Unit test.");
        HolidayMessage h4 = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                SchedulingState.Approved, HolidayType.Yearly, date, date, "This is a Unit test.");
        try (Connection con = database.getConnection()) {

            con.prepareStatement(toInsertQuery(h1)).execute();
            con.prepareStatement(toInsertQuery(h2)).execute();
            con.prepareStatement(toInsertQuery(h3)).execute();
            con.prepareStatement(toInsertQuery(h4)).execute();

            ResultSet r1 = con.prepareStatement(getHolidayMessageByStateQuery(h1)).executeQuery();
            ResultSet r2 = con.prepareStatement(getHolidayMessageByStateQuery(h2)).executeQuery();
            ResultSet r3 = con.prepareStatement(getHolidayMessageByStateQuery(h3)).executeQuery();
            ResultSet r4 = con.prepareStatement(getHolidayMessageByStateQuery(h4)).executeQuery();

            r1.next();
            r2.next();
            r3.next();
            r4.next();

            assertEquals(r1.getInt("id"), h1.id);
            assertEquals(r2.getInt("id"), h2.id);
            assertEquals(r3.getInt("id"), h3.id);
            assertEquals(r4.getInt("id"), h4.id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateHolidayMessageTest() throws ParseException {
        clearDatabase();
        Date date = format.parse("2019-04-20 09:00:00");
        List<HolidayMessage> messages = new ArrayList<>();

        try (Connection con = database.getConnection()) {
            HolidayMessage h = newHolidayMessage(Math.abs(r.nextInt()), "robin-de-zwaef", "robin-de-zwaef",
                    SchedulingState.New, HolidayType.Sickness, date, date, "This is a Unit test.");

            con.prepareStatement(toInsertQuery(h)).execute();

            h.setState(SchedulingState.Approved);
            con.prepareStatement(updateHolidayMessageSchedulingStateQuery(h)).execute();

            ResultSet rs = con.prepareStatement(getHolidayMessageQuery(h)).executeQuery();
            rs.next();
            assertEquals(h.getState().ordinal(), rs.getInt("state"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void insertFavoriteTest() {
        // TODO
        clearDatabase();
    }


    @After
    public void shutdownDatabase() {
        clearDatabase();
        database.shutdown();
    }

    private String toInsertQuery(HolidayMessage hm) {
        return "INSERT INTO holiday_message (id, employee_id, type, state, request_date, last_update, request_by_id, planner_of_last_update, comment) VALUES (" + hm.id + ", '" + hm.getEmployeeID() + "', " + hm.getType().ordinal() + ", " + hm.getState().ordinal() + ", '" + format.format(hm.getRequestDate()) + "', '" + format.format(hm.getLastUpdate()) + "', '" + hm.getRequestByID() + "', '" + hm.getPlannerOfLastUpdate() + "', '" + hm.getComment() + "')";
    }

    private String toInsertQuery(ExactDate e) {
        return "INSERT INTO exact_date (id, date, daypart, holiday_message_id) VALUES (" + e.id + ", '" + e.getDate() + "', " + e.getDaypart().ordinal() + ", " + e.getHolidayMessage().id + ");";
    }

    private String getHolidayMessageQuery(HolidayMessage h) {
        return "SELECT * FROM holiday_message WHERE id = " + h.id + ";";
    }

    private String getHolidayMessageByTypeQuery(HolidayMessage h) {
        return "SELECT * FROM holiday_message WHERE type = " + h.getType().ordinal() + ";";
    }

    private String getHolidayMessageByStateQuery(HolidayMessage h) {
        return "SELECT * FROM holiday_message WHERE state = " + h.getState().ordinal() + ";";
    }

    private String updateHolidayMessageSchedulingStateQuery(HolidayMessage h) {
        return "UPDATE holiday_message SET state = " + h.getState().ordinal() + ";";
    }


    private HolidayMessage newHolidayMessage(int id, String requestId, String emplyeeId, SchedulingState state,
                                             HolidayType type, Date requestDate, Date lastUpdate, String comment) {
        HolidayMessage h = new HolidayMessage();
        h.id = id;
        h.setRequestByID(requestId);
        h.setEmployeeID(emplyeeId);
        h.setState(state);
        h.setType(type);
        h.setRequestDate(requestDate);
        h.setLastUpdate(lastUpdate);
        h.setComment(comment);

        return h;
    }

    private List<ExactDate> newExactDates(int count, HolidayMessage h) {
        List<ExactDate> dates = new ArrayList<>();

        for (int i = 1; i <= Math.min(count, 31); i++) {
            ExactDate e = new ExactDate(LocalDate.of(2019, 4, count), DayPart.AM);
            e.setHolidayMessage(h);
            e.id = i;
            dates.add(e);
        }

        h.setExactDates(dates);
        return dates;
    }

    private void clearDatabase() {
        try (Connection con = database.getConnection()) {
            con.prepareStatement("DELETE FROM exact_date WHERE 1=1;").execute();
            con.prepareStatement("DELETE FROM holiday_message WHERE 1=1;").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
