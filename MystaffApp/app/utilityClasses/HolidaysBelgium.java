package utilityClasses;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.time.temporal.TemporalAdjusters.firstInMonth;

public class HolidaysBelgium {

    /*
     * This class calculates every official Belgian holiday.
     * It loads the holidays when constructed and makes them available in a map that has the name of the holiday(period)
     *      as a key and the begin and end date of the holiday(period) as value.
     * When a holiday is only one day long, the begin and end date are the same.
     */

    private static final Logger LOGGER = Logger.getLogger( HolidaysBelgium.class.getName() );

    //holds all holidays and vacations in Belgium
    private Map<String, Date[]> holidaysAndVacationsBelgium;
    //holds only the vacations (like autumn break, spring break...)
    private Map<String, Date[]> vacationsBelgium;

    private int year;

    /*
     * When constructed, load every holiday for a certain year.
     */
    public HolidaysBelgium(int year){
        this.year = year;
        holidaysAndVacationsBelgium = new HashMap<>();
        vacationsBelgium = new HashMap<>();
        try{
            addSummerVacation();
            addAutumnBreak();
            addChristmasBreak();
            addSpringBreak();
            addEasterHolidays();
            addNewYear();
            addEasterandEasterMonday();
            addLabourDay();
            addOurLordAscensionDay();
            addPentecostandPentecostMonday();
            addOurLadyAscension();
            addNationalHoliday();
            addAllSaintsDay();
            addArmisticeDay();
            addChristmasDay();
        }
        catch (ParseException e){
            LOGGER.log(Level.WARNING, e.toString());
        }
    }

    public Map<String, Date[]> getVacationsBelgium(){
        return vacationsBelgium;
    }

    /*
     * Get all loaded holidays.
     */
    public Map<String, Date[]> getHolidaysAndVacationsBelgium(){
        return holidaysAndVacationsBelgium;
    }

    /*
     * Get all loaded holidays between two dates.
     */
    public String getHolidaysAndVacationsBetweenDates(Date d1, Date d2){
        LocalDate begin1 = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end1 = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        ArrayList<String> result = new ArrayList<String>();
        String[] nationalHolidaysLong = new String[]{"zomervakantie","herfstvakantie","kerstvakantie","krokusvakantie","paasvakantie"};
        for(Map.Entry<String, Date[]> entry: holidaysAndVacationsBelgium.entrySet()){
            Date[] dates = entry.getValue();
            LocalDate begin = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate start_vac = dates[0].toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end_vac = dates[1].toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            while(begin.isBefore(end) || begin.isEqual(end)){
                if((begin.isAfter(start_vac) || begin.isEqual(start_vac)) && (begin.isBefore(end_vac) || begin.isEqual(end_vac))){
                    result.add(entry.getKey());
                }
                begin = begin.plusDays(1);
            }
        }
        for(String holiday: result){
            if(begin1.getDayOfYear() == end1.getDayOfYear() && !Arrays.asList(nationalHolidaysLong).contains(holiday)){
                return holiday;
            }
        }
        for(String holiday: result){
            if(begin1.getDayOfYear() != end1.getDayOfYear() && Arrays.asList(nationalHolidaysLong).contains(holiday)){
                return holiday;
            }
        }
        for(String holiday: result){
            if(begin1.getDayOfYear() != end1.getDayOfYear()){
                return holiday;
            }
        }

        return "";
    }

    /*
     * Get the begin- and end date for a certain vacation in an array with those two dates.
     */
    public Date[] getDatesOfHoliday(String vacation){
        return holidaysAndVacationsBelgium.get(vacation);
    }

    private void addSummerVacation() throws ParseException{
        //From 1st of July till 31st of August
        holidaysAndVacationsBelgium.put("zomervakantie", new Date[] {
                new SimpleDateFormat("dd/MM/yyyy").parse("01/07/" + year),
                new SimpleDateFormat("dd/MM/yyyy").parse("31/08/" + year)
        });
        vacationsBelgium.put("zomervakantie", new Date[] {
                new SimpleDateFormat("dd/MM/yyyy").parse("01/07/" + year),
                new SimpleDateFormat("dd/MM/yyyy").parse("31/08/" + year)
        });
    }

    private void addAutumnBreak() throws ParseException {
        Date d = new SimpleDateFormat("dd/MM/yyyy").parse("01/11/" + year);
        DateTime cal = new DateTime(d);
        Date begin;
        Date end;
        //if 1 November is a Sunday, 2 november is the start of autumn break
        if(cal.getDayOfWeek()==DateTimeConstants.SUNDAY){
            DateTime cal2 = new DateTime(d);
            cal2.plusDays(1);
            begin = cal2.toDate();
            end = cal2.plusWeeks(1).minusDays(1).toDate();

        }
        else{ //Autumn break starts on monday during the week of 1 November and takes 1 week
            DateTime beginDateTime = cal.withDayOfWeek(DateTimeConstants.MONDAY);
            begin = beginDateTime.toDate();
            end = beginDateTime.plusWeeks(1).minusDays(1).toDate();

        }
        holidaysAndVacationsBelgium.put("herfstvakantie", new Date[]{
                begin,
                end
        });
        vacationsBelgium.put("herfstvakantie", new Date[]{
                begin,
                end
        });
    }

    private void addChristmasBreak() throws ParseException{
        Date d = new SimpleDateFormat("dd/MM/yyyy").parse("25/12/" + year);
        DateTime cal = new DateTime(d);
        Date begin;
        Date end;
        //If Christmas Day is on Saturday or Sunday, Christmas break starts on next week's Monday and takes two weeks
        if((cal.getDayOfWeek() == DateTimeConstants.SUNDAY) || (cal.getDayOfWeek() == DateTimeConstants.SATURDAY)){
            DateTime cal2 = cal.plusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY);
            begin = cal2.toDate();
            end = cal2.plusWeeks(2).minusDays(1).toDate();

        }
        //Christmas break starts on monday in week where Christmas Day takes place and takes two weeks
        else{
            DateTime beginDateTime = cal.withDayOfWeek(DateTimeConstants.MONDAY);
            begin = beginDateTime.toDate();
            end = beginDateTime.plusWeeks(2).minusDays(1).toDate();

        }
        holidaysAndVacationsBelgium.put("kerstvakantie", new Date[]{
                begin,
                end
        });
        vacationsBelgium.put("kerstvakantie", new Date[]{
                begin,
                end
        });
    }

    private void addSpringBreak() throws ParseException{
        DateTime cal = EasterDate(year);
        DateTime beginDateTime = cal.minusWeeks(7).plusDays(1); //7 weeks before Easter on a monday
        Date begin = beginDateTime.toDate();
        Date end = beginDateTime.plusWeeks(1).minusDays(1).toDate();
        holidaysAndVacationsBelgium.put("krokusvakantie", new Date[]{
                begin,
                end
        });
        vacationsBelgium.put("krokusvakantie", new Date[]{
                begin,
                end
        });
    }

    private void addEasterHolidays() throws ParseException{
        DateTime cal = EasterDate(year);
        Date begin;
        Date end;
        if(cal.getMonthOfYear() == DateTimeConstants.MARCH){ //if Easter is in March, holidays start monday after Easter
            begin = cal.plusDays(1).toDate();
            end = cal.plusWeeks(2).toDate();
        }
        //If Easter is after 15th of April, Easter Holidays start 2nd monday before Easter
        //And add one day to holidays for Easter Monday
        else if(cal.getMonthOfYear() == DateTimeConstants.APRIL && cal.getDayOfMonth()>15){
            begin = cal.minusWeeks(2).plusDays(1).toDate();
            end = cal.plusDays(1).toDate();
        }
        //Easter Holidays start first monday of April and takes two weeks
        else{
            LocalDate now = LocalDate.of(cal.getYear(), cal.getMonthOfYear(), cal.getDayOfMonth());
            LocalDate firstMonday = now.with(firstInMonth(DayOfWeek.MONDAY));
            begin = Date.from(firstMonday.atStartOfDay(ZoneId.systemDefault()).toInstant());
            DateTime beginDateTime = new DateTime(begin);
            end = beginDateTime.plusWeeks(2).minusDays(1).toDate();
        }
        holidaysAndVacationsBelgium.put("paasvakantie", new Date[]{
                begin,
                end
        });
        vacationsBelgium.put("paasvakantie", new Date[]{
                begin,
                end
        });
    }

    private void addNewYear() throws ParseException {
        Date new_year = new SimpleDateFormat("dd/MM/yyyy").parse("01/01/"+year);
        holidaysAndVacationsBelgium.put("Nieuwjaarsdag", new Date[]{new_year, new_year});
    }

    private void addEasterandEasterMonday() throws ParseException {
        DateTime dateTime = EasterDate(year);
        Date easter = dateTime.toDate();
        holidaysAndVacationsBelgium.put("Pasen", new Date[]{easter, easter});
        Date easter_monday = dateTime.plusDays(1).toDate();
        holidaysAndVacationsBelgium.put("Paasmaandag", new Date[]{easter_monday, easter_monday});
    }

    private void addLabourDay() throws ParseException {
        Date labour_day = new SimpleDateFormat("dd/MM/yyyy").parse("01/05/"+year);
        holidaysAndVacationsBelgium.put("Dag van de Arbeid",new Date[]{labour_day, labour_day});
    }

    private void addOurLordAscensionDay() throws ParseException {
        DateTime dateTime = EasterDate(year);
        Date ascenscion_day = dateTime.plusWeeks(6).withDayOfWeek(DateTimeConstants.THURSDAY).toDate();
        holidaysAndVacationsBelgium.put("O.L.H. Hemelvaart", new Date[]{ascenscion_day, ascenscion_day});
    }

    private void addPentecostandPentecostMonday() throws ParseException {
        DateTime dateTime = EasterDate(year);
        Date pentecost = dateTime.plusWeeks(7).toDate();
        holidaysAndVacationsBelgium.put("Pinksteren", new Date[]{pentecost, pentecost});
        Date pentecost_monday = dateTime.plusWeeks(7).plusDays(1).toDate();
        holidaysAndVacationsBelgium.put("Pinkstermaandag", new Date[]{pentecost_monday, pentecost_monday});
    }

    private void addNationalHoliday() throws ParseException {
        Date nationalHoliday = new SimpleDateFormat("dd/MM/yyyy").parse("21/07/"+year);
        holidaysAndVacationsBelgium.put("Nationale Feestdag", new Date[]{nationalHoliday, nationalHoliday});
    }

    private void addOurLadyAscension() throws ParseException {
        Date ourLadyAscension = new SimpleDateFormat("dd/MM/yyyy").parse("15/08/"+year);
        holidaysAndVacationsBelgium.put("O.L.V. Hemelvaart", new Date[]{ourLadyAscension, ourLadyAscension});
    }

    private void addAllSaintsDay() throws ParseException {
        Date allSaintsDay = new SimpleDateFormat("dd/MM/yyyy").parse("01/11/"+year);
        holidaysAndVacationsBelgium.put("Allerheiligen", new Date[]{allSaintsDay, allSaintsDay});
    }

    private void addArmisticeDay() throws ParseException {
        Date armistice_day = new SimpleDateFormat("dd/MM/yyyy").parse("11/11/"+year);
        holidaysAndVacationsBelgium.put("Wapenstilstand", new Date[]{armistice_day, armistice_day});
    }

    private void addChristmasDay() throws ParseException {
        Date christmas = new SimpleDateFormat("dd/MM/yyyy").parse("25/12/"+year);
        holidaysAndVacationsBelgium.put("Kerstmis", new Date[]{christmas, christmas});
    }

    private static DateTime EasterDate(int year) throws ParseException{
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int L = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * L) / 451;
        int i1 = h + L - 7 * m + 114;
        int month = i1 / 31;
        int day = (i1 % 31) + 1;
        Date date = new SimpleDateFormat("dd/MM/yyyy").parse(day + "/" + month + "/" + year);
        return new DateTime(date);
    }
}
