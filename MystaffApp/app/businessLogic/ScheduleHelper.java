package businessLogic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AxiansController;
import enumerations.DayPart;
import enumerations.SchedulingState;
import models.planning.*;
import models.planning.days.WeekDay;
import persistence.HolidayMessageController;
import play.libs.Json;
import utilityClasses.ExactDate;
import utilityClasses.HolidayMessage;
import utilityClasses.PlanningUnitState;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

/*
The ScheduleHelper calculates a lot of things that come in handy for the Scheduler.
-It calculates the numbers of persons that are needed and available on a certain day.
    These numbers are important for the planner. If three people ask for an absence a certain day, and he sees that
    3-5 people can be given an absence, he immediately sees that he could approve them.
-It can also check if certain combinations of absences are possible without breaking the whole planning.
    To provide this functionality, it uses a two versions of a backtracking algorithm.
    One stops when it finds a possible planning. The other one searches the best possible planning with the most people
        reserve.

 */

public class ScheduleHelper {

    final private static String REQUIRED = "required";
    final private static String ALTERNATIVE = "alternative";
    @Inject
    public AxiansController ac;
    @Inject
    public HolidayMessageController hmController;

    private boolean doLogBacktracker = false;

    private PlanningUnitHelper planningUnitHelper;

    // These lists are primary used in for-loops.
    private List<ExactDate> allDates;

    // Make maps that contains the people that have the correct skills to function on a certain daypart.
    // Keys: shift, 'required' or 'alternative', exact date
    private Map<Shift, Map<String, Map<ExactDate, List<PlanningMember>>>> peopleWithCorrectSkills;


    // Maps with keys shift and exactdate that keep track of all people that are
    //  - Possible if only looked at the skills and the absences (not the simultaneous things)
    //  - Realistically possible to plan in
    // Sets are used so we don't have to worry about doubles.
    private Map<Shift, Map<ExactDate, Set<PlanningMember>>> tempPlanning;
    private Map<Shift, Map<ExactDate, Set<PlanningMember>>> finalPlanning;

    // This map has the same structure as the temp and final plannings. The backtracker will copy the finalPlanning to
    // this planning if it's max number of reserve people is bigger than the current max.
    private Map<Shift, Map<ExactDate, Set<PlanningMember>>> maxPlanning;
    private int maxReserve;


    // Map the detailed info about a planningUnit on it's ID
    private Map<String, PlanningLong> units;

    // Map with keys shifts and the weekday as integer. It holds the people with the correct skillset per day per shift.
    private Map<Shift, Map<Integer, Set<String>>> requiredSkills;
    private Map<Shift, Map<Integer, Set<String>>> alternativeSkills;
    // Map the number of people that are needed on certain days
    private Map<Shift, Map<Integer, Integer>> peopleNeeded;
    private int maxRecup = 0;

    // This is the backstack used by the backtracker
    private Stack<SchedulingPerson> stack;

    // The key is the planningsunitID. Used for the case where more than one planningsunit is checked.
    private Map<String, List<PlanningMember>> planningMembersPerPlanningsID;
    private Map<String, List<Shift>> shiftsPerPlanningsUnitID;

    private Map<Shift, Map<Integer, WeekDay>> weekdays;

    @Inject
    public ScheduleHelper(PlanningUnitHelper planningUnitHelper){
        this.planningUnitHelper = planningUnitHelper;
    }

    /**
    GetScheduledDataForPlanningUnit does what it promises.
    Shifts are saved on the Axians server for one week. Every week has the same structure.
    It contains demands for 7 objects: Monday, Tuesday, ... One for every day.
    For convenience, these are forwarded not as day-objects, but as an array with indexes 0-6 where 0 is sunday.
    0 as sunday for a better collaboration with the frontend calendar templates.
    Each day contains   the skills that are needed that day
                        the skills that are optional that day
                        the number of people needed

    {
        id: id of the shift,
        name: name of the shift,
        location: location of the shift,
        shifts:
        [
            {
                0
                number: number of people needed,
                requiredSkills: skills that must be in this shift,
                alternativeSkills: skills that may be in this shift
            }, {
            1
            ..
            }, ... {
            6
            ...
            }
        ]
    }

     */

    public static JsonNode getScheduledDataForPlanningUnit(PlanningLong unit) throws Exception {
        List<Shift> shifts = unit.getShifts();
        ObjectNode returnJson = Json.newObject();
        ArrayNode arr = returnJson.putArray("shifts");
        for(Shift shift: shifts){
            ObjectNode node = arr.addObject();
            node.put("id", shift.getId());
            node.put("name", shift.getName());
            node.put("location", shift.getLocation());

            Map<Integer, WeekDay> map = new HashMap<>();
            map.put(1, shift.getDemands().getMONDAY());
            map.put(2, shift.getDemands().getTUESDAY());
            map.put(3, shift.getDemands().getWEDNESDAY());
            map.put(4, shift.getDemands().getTHURSDAY());
            map.put(5, shift.getDemands().getFRIDAY());
            map.put(6, shift.getDemands().getSATURDAY());
            map.put(0, shift.getDemands().getSUNDAY());

            for(Integer weekdayStr: map.keySet()){
                ObjectNode weekday = node.putObject(weekdayStr+"");
                weekday.put("number", map.get(weekdayStr).getNumber());
                ArrayNode mondayRequiredSkills = weekday.putArray("requiredSkills");
                for(String skill: map.get(weekdayStr).getRequiredSkills()){
                    mondayRequiredSkills.add(skill);
                }
                ArrayNode mondayAlternativeSkills = weekday.putArray("alternativeSkills");
                for(String skill: map.get(weekdayStr).getAlternativeSkills()){
                    mondayAlternativeSkills.add(skill);
                }
            }

        }
        return returnJson;
    }



    /**
    to see the format of the returned JSON, please check jsonIfyReserve.
    getAvailablePeoplePerDayPartForPlanningUnits returns the employees that COULD be scheduled in a certain period.
    The start and end of the period are given in "yyyy-mm-dd" format.
    It looks which skills are needed and which skills are possible for each day part.
    Then it adds the people that are not absent that have these skills.
    Next it checks all constraints like simultaneous shifts and days with recup with the help of a backtracker.
    This function returns the
        -number of needed people
        -minimum number of people that can be given an absence
        -maximum number of people that can be given an absence
    for each shift separately as well as in total.
    It also returns the for each day, for every shift the specific people id's that could be planned in and that are
    planned in in this case.
    It may seem weird that two separate numbers are given, we would want only one number, THE number of people that could
    be given an absence. This is impossible.
        e.g. There are two shifts.
            In one shift, there is only one person needed, but he needs a complex skillset.
            In the second shift, there is als one person needed, but with a simple skillset.
            In the first shift, there is one person with the correct skillset
            In the second shift, there are 4 people with the correct skillset.
            Now, the person in the first shift can not be given an absence, he is crucial to that shift.
            But 3 people in the second shift can be given an absence.
            Thus we say: there are 2 people needed and 0-3 can be given an absence.
     */

    public JsonNode getAvailablePeoplePerDayPartForPlanningUnits(String token, List<String> planningUnits, LocalDate start, LocalDate end, Map<String, PlanningLong> units, boolean firstTime) throws Exception {
        this.units = units;
        if(firstTime) {
            initiateNonChangingStructures(token, planningUnits, start, end);

            fillPeopleWithCorrectSkills();

            shiftPeopleOutThatAreAbsent();
        }

        resetPlannings(token, planningUnits, start, end);

        fillTempPlanning();

        //filterTempPlanningWithoutConstraints1();
        filterTempPlanningWithoutConstraints2();

        // tempPlanning is now filled with all people that have the correct skills to function that day. Now we should
        // look which people can't function anymore because of shift constraints.
        stack = prepareBackStack();
        if(!stack.isEmpty()){
            solveBackTrackReserve();
        } else {
            if(checkFinalPlanning() >= 0) {
                copyFinalPlanningToMaxPlanning();
            }
        }


        return jsonIfyReserve();
    }

    /**
     * Calculates if a planning is still possible without certain people. Used to check combinations of holidayMessages.
     * HM's are represented by more simple PersonDate objects.
     * The algorithm is very similar to that of getAvailablePeoplePerDayPartForPlanningUnits, but it stops when one
     * valid planning is found and doesn't search for the optimal one.
     *
     * @return true when the planning is still possible en false otherwise.
     */
    public boolean isPlanningStillPossibleWithoutCertainPeople(String token, List<String> planningUnits, LocalDate start, LocalDate end, List<PersonDate> peopleList, Map<String, PlanningLong> units,boolean firstTime){
        try {
            this.units = units;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
            if(doLogBacktracker)
                System.out.println("\tstart " + sdf.format(new Timestamp(System.currentTimeMillis())));
            // This construction makes sure that the long process of building the needed maps only happens once.
            // NOTE: this was good in earlier states of the algorithm where every call used the same start and end date
            //          now, every call uses other dates and this system does not work properly anymore...
            if (firstTime) {
                initiateNonChangingStructures(token, planningUnits, start, end);
                fillPeopleWithCorrectSkills();
                shiftPeopleOutThatAreAbsent();
            }
            resetPlannings(token, planningUnits, start, end);
            fillTempPlanning();
            shiftCertainPeopleOut(peopleList);
            filterTempPlanningWithoutConstraints2();

            // tempPlanning is now filled with all people that have the correct skills to function that day. Now we should
            // look which people can't function anymore because of shift constraints.
            stack = prepareBackStack();

            int i = solveBackTrack(0);
            if(doLogBacktracker)
                System.out.println("\tend backtrack " + sdf.format(new Timestamp(System.currentTimeMillis())));
            return i > -1;

        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calculate the longest possible recup period for certain shifts.
     *
     * The recup number says how many days a person can not work after he has done a certain shift.
     * If recup is 2 for a certain shift on monday, that person can not work in any shift on tuesday or wednesday.
     *
     * This number is used by the algorithms that have to check whether a planning can still be made if certain people
     * are absent. The performance of those algorithms is heavily dependent on the amount of absences (holidayMessages)
     * there are tested at a time. To split them op, the HM's are splitted in groups based on the recup number. If there
     * are e number of days between two groups of holidaymessages that is greater to the recup period, they
     * can never affect each other. This limits the number of HM's that is tested at a time and heavily increases
     * performance.
     *
     * @param pls: a list of PlanningLong objects (represent planning units).
     */
    private void calculateMaxRecup(List<PlanningLong> pls){
        int maxRecup = 0;
        for(PlanningLong pl: pls){
            for(Shift shift: pl.getShifts()) {
                if(shift.getDemands().getMONDAY().getFreePeriodDays() > maxRecup) maxRecup = shift.getDemands().getMONDAY().getFreePeriodDays();
                if(shift.getDemands().getTUESDAY().getFreePeriodDays() > maxRecup) maxRecup = shift.getDemands().getMONDAY().getFreePeriodDays();
                if(shift.getDemands().getWEDNESDAY().getFreePeriodDays() > maxRecup) maxRecup = shift.getDemands().getMONDAY().getFreePeriodDays();
                if(shift.getDemands().getTHURSDAY().getFreePeriodDays() > maxRecup) maxRecup = shift.getDemands().getMONDAY().getFreePeriodDays();
                if(shift.getDemands().getFRIDAY().getFreePeriodDays() > maxRecup) maxRecup = shift.getDemands().getMONDAY().getFreePeriodDays();
                if(shift.getDemands().getSATURDAY().getFreePeriodDays() > maxRecup) maxRecup = shift.getDemands().getMONDAY().getFreePeriodDays();
                if(shift.getDemands().getSUNDAY().getFreePeriodDays() > maxRecup) maxRecup = shift.getDemands().getMONDAY().getFreePeriodDays();
            }
        }
        this.maxRecup = maxRecup;
    }

    // Does the same as isPlanningStillPossibleWithoutCertainPeople with one big optimalisation difference.
    // If we want to test one date in january and another in august, we would have to test way too much days, the
    // back stack would be very big and performance would be very bad.
    // Therefore, this function tests the hm's split up per group that can influence each other. To do this, groups are
    // made with a number of days that equals the maximum recup days between them.
    // NOTE: also the period before and after the hm period contains an extra amount of days that is equal to the max
    //       number of recup days plus one.
    public boolean isPlanningStillPossibleWithoutCertainPeopleGroups(String token, List<String> planningUnits, List<PersonDate> peopleList, Map<String, PlanningLong> units, boolean firstTime){
        try {
            this.units = units;
            List<List<PersonDate>> peopleGroup = partitionSchedulingPeople(peopleList);
            for (List<PersonDate> list : peopleGroup) {
                LocalDate min = null;
                LocalDate max = null;
                for (PersonDate pd : list) {
                    if (min == null) {
                        min = pd.getDates().get(0).getDate();
                        max = pd.getDates().get(pd.getDates().size() - 1).getDate();
                    } else {
                        if (pd.getDates().get(0).getDate().isBefore(min)) {
                            min = pd.getDates().get(0).getDate();
                        }
                        if (pd.getDates().get(pd.getDates().size() - 1).getDate().isAfter(max)) {
                            max = pd.getDates().get(pd.getDates().size() - 1).getDate();
                        }
                    }
                }
                min = min.minusDays(maxRecup + 1);
                max = max.plusDays(maxRecup + 1);
                // firstTime can not be used, because of the different day periods.
                if (!isPlanningStillPossibleWithoutCertainPeople(token, planningUnits, min, max, list, units,true)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Partitions a list of people into multiple sublists of people with an amount greater than the maximum number of
     * recup days between them. These subgroups can never influence each other.
     * By deviding the big group into multiple subgroups, the backstack of the backtrackers becomes way smaller and the
     * performance is much higher.
     */
    public List<List<PersonDate>> partitionSchedulingPeople(List<PersonDate> people){
        Collections.sort(people);
        List<List<PersonDate>> returnPeople = new ArrayList<>();
        List<PersonDate> temppeople = new ArrayList<>();
        for(int i = 0; i < people.size(); i++){
            if(i == 0){
                temppeople.add(people.get(i));
            } else {
                if(DAYS.between(people.get(i-1).getDates().get(0).getDate(), people.get(i).getDates().get(0).getDate()) > maxRecup){
                    List<PersonDate> newGroup = new ArrayList<>();
                    for(PersonDate pd: temppeople){
                        newGroup.add(pd);
                    }
                    returnPeople.add(newGroup);
                    temppeople = new ArrayList<>();
                    temppeople.add(people.get(i));
                } else {
                    temppeople.add(people.get(i));
                }
            }
        }
        if(!temppeople.isEmpty()) {
            List<PersonDate> newGroup = new ArrayList<>();
            for (PersonDate pd : temppeople) {
                newGroup.add(pd);
            }
            returnPeople.add(newGroup);
        }
        return returnPeople;
    }

    // This should be called every first time a certain set of days and planningUnits is tested.
    // When a second test occurs on these same variables, this function should not be called again.
    // NOTE: EVERYTHING should be the same if you don't call this function => pu's AND dates!
    private void initiateNonChangingStructures(String token, List<String> planningUnits, LocalDate start, LocalDate end) throws Exception {
        // Map the detailed info about a planningUnit on it's ID
        //units = new HashMap<>();
        planningMembersPerPlanningsID = new HashMap<>();
        shiftsPerPlanningsUnitID = new HashMap<>();
        // Make maps that contains the people that have the correct skills to function on a certain daypart.
        // Keys: shift, 'required' or 'alternative', exact date
        peopleWithCorrectSkills = new HashMap<>();
        // Map with Shifts as keys and weekday as integer
        requiredSkills = new HashMap<>();
        alternativeSkills = new HashMap<>();
        // Map the number of people that are needed on certain days
        peopleNeeded = new HashMap<>();

        // These lists are primary used in for-loops.
        allDates = new ArrayList<>();

        for(String plunit: planningUnits){
            planningMembersPerPlanningsID.put(plunit, planningUnitHelper.getAllEmployeesOfPlanningUnitWithId(token, plunit));
        }

        for(PlanningLong unit: units.values()) {
            shiftsPerPlanningsUnitID.put(unit.getId(), new ArrayList<>());
            for (Shift shift : unit.getShifts()) {
                shiftsPerPlanningsUnitID.get(unit.getId()).add(shift);
                //shifts.add(shift);
            }
        }

        List<Shift> tempshifts = new ArrayList<>();
        for(String unit: units.keySet()){
            for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                tempshifts.add(shift);
            }
        }


        weekdays = makeWeekDayMaps(tempshifts);

        for(String unit: units.keySet()){
            for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                peopleWithCorrectSkills.put(shift, new HashMap<>());
                peopleWithCorrectSkills.get(shift).put(REQUIRED, new HashMap<>());
                peopleWithCorrectSkills.get(shift).put(ALTERNATIVE, new HashMap<>());
                requiredSkills.put(shift, new HashMap<>());
                alternativeSkills.put(shift, new HashMap<>());
                peopleNeeded.put(shift, new HashMap<>());
                for (int i = 1; i < 8; i++) {
                    requiredSkills.get(shift).put(i, new HashSet<>());
                    alternativeSkills.get(shift).put(i, new HashSet<>());
                    if (!peopleNeeded.get(shift).containsKey(i)) {
                        peopleNeeded.get(shift).put(i, weekdays.get(shift).get(i).getNumber());
                    } else {
                        peopleNeeded.get(shift).put(i, peopleNeeded.get(shift).get(i) + weekdays.get(shift).get(i).getNumber());
                    }
                }
            }
        }

        LocalDate temp = start;
        while(temp.isBefore(end.plusDays(1))) {
            ExactDate edAM = new ExactDate(temp, DayPart.AM);
            ExactDate edPM = new ExactDate(temp, DayPart.PM);
            allDates.add(edAM);
            allDates.add(edPM);
            for (String unit : units.keySet()) {
                for (Shift shift : shiftsPerPlanningsUnitID.get(unit)) {
                    peopleWithCorrectSkills.get(shift).get(REQUIRED).put(edAM, new ArrayList<>());
                    peopleWithCorrectSkills.get(shift).get(ALTERNATIVE).put(edAM, new ArrayList<>());
                    peopleWithCorrectSkills.get(shift).get(REQUIRED).put(edPM, new ArrayList<>());
                    peopleWithCorrectSkills.get(shift).get(ALTERNATIVE).put(edPM, new ArrayList<>());
                }
            }

            temp = temp.plusDays(1);
        }

        // Is used to check if shifts affect the current shift with their recupdays.
        // By using this as a maximum window, not all dates have to be checked and the algorithm is more performant.
        calculateMaxRecup(new ArrayList<>(units.values()));

        // This loop gets all shifts of all planningUnits and fills the skill maps and the number map as well.
        // The dictionary Map<Integer, WeekDay> is needed if we don't want to use a 7-double if-structure.
        for(String unit: units.keySet()){
            for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {

                Map<Integer, WeekDay> demandsMap = makeWeekDayMap(shift);
                for (ExactDate ed : allDates) {
                    int weekDay = ed.getDate().getDayOfWeek().getValue();
                    if (weekdays.get(shift).get(weekDay).getFreePeriodDays() > maxRecup) {
                        maxRecup = weekdays.get(shift).get(weekDay).getFreePeriodDays();
                    }
                    if (weekdays.get(shift).get(weekDay).getRequiredSkills() != null) {
                        for (String skill : weekdays.get(shift).get(weekDay).getRequiredSkills()) {
                            requiredSkills.get(shift).get(weekDay).add(skill);
                        }
                    }
                    if (weekdays.get(shift).get(weekDay).getAlternativeSkills() != null) {
                        for (String skill : weekdays.get(shift).get(weekDay).getAlternativeSkills()) {
                            alternativeSkills.get(shift).get(weekDay).add(skill);
                        }
                    }
                }
            }

        }
    }

    /**
     * Resets tempPlanning and finalPlanning.
     * This has to be done every time the back tracker has to start over.
     */

    private void resetPlannings(String token, List<String> planningUnits, LocalDate start, LocalDate end){
        // Maps with keys shift and exactdate that keep track of all people that are
        //  - Possible if only looked at the skills and the absences (not the simultanious things)
        //  - Realistically possible to plan in
        // Sets are used so we don't have to worry about doubles.
        tempPlanning = new HashMap<>();
        finalPlanning = new HashMap<>();
        maxPlanning = null;


        // After this two loops, all maps and lists are initiated empty.
        for(String unit: units.keySet()){
            for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                tempPlanning.put(shift, new HashMap<>());
                finalPlanning.put(shift, new HashMap<>());
            }
        }
        LocalDate temp = start;
        while(temp.isBefore(end.plusDays(1))){
            ExactDate edAM = new ExactDate(temp, DayPart.AM);
            ExactDate edPM = new ExactDate(temp, DayPart.PM);
            for(String unit: units.keySet()){
                for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                    tempPlanning.get(shift).put(edAM, new HashSet<>());
                    tempPlanning.get(shift).put(edPM, new HashSet<>());
                    finalPlanning.get(shift).put(edAM, new HashSet<>());
                    finalPlanning.get(shift).put(edPM, new HashSet<>());
                }
            }

            temp = temp.plusDays(1);
        }

        // Will be used by the backtracker to keep track of the current maximum.
        maxReserve = -1;
    }

    // This loop checks if people are needed on a certain daypart, and if so, adds them to the list.
    // If a day does not need people, don't add them to the stack.
    private void fillPeopleWithCorrectSkills() {
        for(String unit: units.keySet()){
            for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                for (ExactDate ed : allDates) {
                    if(peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue()) != 0) {
                        for (PlanningMember member : planningMembersPerPlanningsID.get(unit)) {
                            boolean neededNotfound = false;
                            boolean alternativeNotfound = false;
                            for (String skill : requiredSkills.get(shift).get(ed.getDate().getDayOfWeek().getValue())) {
                                if (!Arrays.asList(member.getSkills()).contains(skill)) {
                                    neededNotfound = true;
                                }
                            }
                            for (String skill : alternativeSkills.get(shift).get(ed.getDate().getDayOfWeek().getValue())) {
                                if (!Arrays.asList(member.getSkills()).contains(skill)) {
                                    alternativeNotfound = true;
                                }
                            }
                            if (!neededNotfound) {
                                peopleWithCorrectSkills.get(shift).get(REQUIRED).get(ed).add(member);
                            }
                            if (!alternativeNotfound) {
                                peopleWithCorrectSkills.get(shift).get(ALTERNATIVE).get(ed).add(member);
                            }
                        }
                    }
                }
            }
        }
    }

    // This loops shifts out everybody that is absent based on their holidayMessages.
    // When a HM is only approved for a certain pu, and not globally approved, it is also shifted out if this pu is
    // tested.
    private void shiftPeopleOutThatAreAbsent(){
        for(String unit: units.keySet()){
            for(PlanningMember pl: planningMembersPerPlanningsID.get(unit)) {
                for (Shift shift : shiftsPerPlanningsUnitID.get(unit)) {
                    List<HolidayMessage> hmlist = hmController.getHolidayMessagesApprovedOfDoctorWithID(pl.getMember());
                    for (HolidayMessage hm : hmlist) {
                        for (ExactDate ed : hm.getExactDates()) {
                            if (allDates.contains(ed)) {
                                peopleWithCorrectSkills.get(shift).get(REQUIRED).get(ed).remove(pl);
                                peopleWithCorrectSkills.get(shift).get(ALTERNATIVE).get(ed).remove(pl);
                            }
                        }
                    }
                    hmlist = hmController.getHolidayMessagesNewOfDoctorWithID(pl.getMember());
                    hmlist.addAll(hmController.getHolidayMessagesInConsiderationOfDoctorWithID(pl.getMember()));
                    for (HolidayMessage hm : hmlist) {
                        for(PlanningUnitState pus: hm.getPlanningUnitStates()) {
                            if(pus.getUnitId().equals(unit) && pus.getState().equals(SchedulingState.Approved)) {
                                for (ExactDate ed : hm.getExactDates()) {
                                    if (allDates.contains(ed)) {
                                        peopleWithCorrectSkills.get(shift).get(REQUIRED).get(ed).remove(pl);
                                        peopleWithCorrectSkills.get(shift).get(ALTERNATIVE).get(ed).remove(pl);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Shifts out all people in the given list out of every shift on the dayParts that they want to be absent.
    private void shiftCertainPeopleOut(List<PersonDate> pdlist){
        for(String unit: units.keySet()){
            for(PersonDate pd: pdlist) {
                for (PlanningMember pl : planningMembersPerPlanningsID.get(unit)) {
                    if (pl.getMember().equals(pd.getEmployeeID())) {
                        for (Shift shift : shiftsPerPlanningsUnitID.get(unit)) {
                            for (ExactDate ed : pd.getDates()) {
                                if (allDates.contains(ed)) {
                                    tempPlanning.get(shift).get(ed).remove(pl);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // This function fills the tempPlanning. This is not a final planning! It only contains the people that have the
    //      correct skills to function on that day. No constraints are tested.
    private void fillTempPlanning(){
        for(String unit: units.keySet()){
            for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                for (ExactDate ed : allDates) {
                    for (PlanningMember pl : peopleWithCorrectSkills.get(shift).get(REQUIRED).get(ed)) {
                        tempPlanning.get(shift).get(ed).add(pl);
                    }
                    for (PlanningMember pl : peopleWithCorrectSkills.get(shift).get(ALTERNATIVE).get(ed)) {
                        tempPlanning.get(shift).get(ed).add(pl);
                    }
                }
            }
        }
    }

    // Shifts out the people of which we know they must be planned in that day, and plan them in the final planning.
    // 1) If a person has only the required or the alternative skills for a certain day, he must be planned in that day.
    //      This is logical, because the day must be filled up eventually, and these people can't be planned in on days
    //      That require other skills.
    // 2) If the people in the tempPlanning and the people in the finalPlanning summed together equal the number of needed
    //      people, then plan them all in. The day must be fully planned, thus this is correct.
    // 3) Then make a list for every person of shifts that become impossible with the simultaneous rules, when this list is
    //      empty, the person can be planned in.
    // Repeat until no effect is visible.

    // Number 1) is moved to another function, because when different people are tested to let out, this could bring trouble.
    private void filterTempPlanningWithoutConstraints2() {
        boolean changesMade = true;
        while (changesMade) {
            changesMade = false;
            // If the number of people in tempPlanning + the number of people in the finalPlanning equal the number of needed
            //      people, plan them in.
            for(String unit: units.keySet()){
                for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                    for (ExactDate ed : allDates) {
                        //NOT for(PlanningMember pl: tempPlanning.get(shift).get(ed))! Otherwise ConcurrentModificationException
                        if (weekdays.get(shift).get(ed.getDate().getDayOfWeek().getValue()).getNumber() == tempPlanning.get(shift).get(ed).size() + finalPlanning.get(shift).get(ed).size()
                        && tempPlanning.get(shift).get(ed).size() > 0) {
                            for (int i = tempPlanning.get(shift).get(ed).size() - 1; i >= 0; i--) {
                                PlanningMember pl = (PlanningMember) tempPlanning.get(shift).get(ed).toArray()[i];
                                planPersonIn(shift, ed, pl);
                                changesMade = true;
                            }
                        }
                    }
                }
            }

            // Make a list of all people that can not be planned in if this person would be planned in
            Map<Shift, Map<ExactDate, Map<PlanningMember, Set<SchedulingObject>>>> affectedPeople = new HashMap<>();
            for(String unit: units.keySet()){
                for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                    affectedPeople.put(shift, new HashMap<>());
                    for (ExactDate ed : allDates) {
                        affectedPeople.get(shift).put(ed, new HashMap<>());
                        for (PlanningMember pl : tempPlanning.get(shift).get(ed)) {
                            affectedPeople.get(shift).get(ed).put(pl, new HashSet<>());
                            for (Shift othershift : shiftsPerPlanningsUnitID.get(unit)) {
                                ExactDate othered = new ExactDate(ed.getDate().plusDays(maxRecup), DayPart.AM);
                                while (othered.getDate().isAfter(ed.getDate()) && allDates.contains(othered)) {
                                    if (allDates.contains(othered)) {
                                        if (tempPlanning.get(othershift).get(othered).contains(pl)) {
                                            if (othered.getDate().isAfter(ed.getDate()) && DAYS.between(ed.getDate(), othered.getDate()) <= weekdays.get(shift).get(ed.getDate().getDayOfWeek().getValue()).getFreePeriodDays()) {
                                                affectedPeople.get(shift).get(ed).get(pl).add(new SchedulingObject(othershift, othered));
                                            }
                                        }
                                        if (othered.getDaypart().equals(DayPart.AM)) {
                                            othered.setDaypart(DayPart.PM);
                                        } else {
                                            othered.setDate(othered.getDate().minusDays(1));
                                            othered.setDaypart(DayPart.AM);
                                        }
                                    }
                                }

                                othered = new ExactDate(ed.getDate().minusDays(maxRecup), DayPart.AM);
                                while (othered.getDate().isBefore(ed.getDate()) && allDates.contains(othered)) {
                                    if (allDates.contains(othered)) {
                                        if (tempPlanning.get(othershift).get(othered).contains(pl)) {
                                            if (othered.getDate().isBefore(ed.getDate()) && DAYS.between(othered.getDate(), ed.getDate()) <= weekdays.get(shift).get(othered.getDate().getDayOfWeek().getValue()).getFreePeriodDays()) {
                                                affectedPeople.get(shift).get(ed).get(pl).add(new SchedulingObject(othershift, othered));
                                            }
                                        }
                                        if (othered.getDaypart().equals(DayPart.AM)) {
                                            othered.setDaypart(DayPart.PM);
                                        } else {
                                            othered.setDate(othered.getDate().plusDays(1));
                                            othered.setDaypart(DayPart.AM);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // If a person does not affect other shifts, it can be safely planned in.
            for(String unit: units.keySet()){
                for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                    for (ExactDate ed : allDates) {
                        for (PlanningMember pl : tempPlanning.get(shift).get(ed)) {
                            if (affectedPeople.get(shift).get(ed).isEmpty()) {
                                planPersonIn(shift, ed, pl);
                                changesMade = true;
                            }
                        }
                    }
                }
            }
        }
    }

    // See filterTempPlanningWithoutConstraints2
    private void filterTempPlanningWithoutConstraints1(){
        // If a certain day contains a person that only has the required or alternative skills, no other skills,
        // Then plan him in the finalPlanning and delete him out of the tempPlanning
        for(String unit: units.keySet()){
            for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                for (ExactDate ed : allDates) {
                    // Making an array to overcome ConcurrentModificationException
                    PlanningMember[] planningMembers = new PlanningMember[tempPlanning.get(shift).get(ed).size()];
                    Iterator<PlanningMember> iterator = tempPlanning.get(shift).get(ed).iterator();
                    for (int i = 0; i < tempPlanning.get(shift).get(ed).size(); i++) {
                        planningMembers[i] = iterator.next();
                    }
                    for (int i = tempPlanning.get(shift).get(ed).size() - 1; i >= 0; i--) {
                        boolean nonRequiredFound = false;
                        boolean nonAlternativeFound = false;
                        for (String skill : planningMembers[i].getSkills()) {
                            if (!requiredSkills.get(shift).get(ed.getDate().getDayOfWeek().getValue()).contains(skill)) {
                                nonRequiredFound = true;
                            }
                            if (!alternativeSkills.get(shift).get(ed.getDate().getDayOfWeek().getValue()).contains(skill)) {
                                nonAlternativeFound = true;
                            }
                        }
                        if (!nonRequiredFound || !nonAlternativeFound) {
                            planPersonIn(shift, ed, planningMembers[i]);
                        }
                    }
                }
            }
        }
    }
    // This algorithm tries to find the best possible planning. That is the planning with the most people in reserve.
    // It starts with a stack of people. This could be all people in a certain planning unit. In the default flow,
    //      "filterTempPlanningWithoutConstraints" filters out some people with no constraints on them to make this quicker.
    // First a person is popped of the stack. NOTE: a person here is a certain person on a certain day in a certain shift
    // Then, if the person could be planned in, it gets planned in.
    //      Else, we will remember that this person can't be planned in (delete from tempPlanning)
    // Next, we check if the finalPlanning is still OK (no shifts that can't be filled in anymore)
    //      If it's not OK, push the person back on the stack(*), undo the planning in (is more difficult than one may
    //      think at the first glance) and return -2.
    // Then, call solveBackTrackReserve recursively.
    //      If we get a value that's greater than 0, we found a solution with some reserve people.
    //      If we get -2, the recursion we called has got a dead end.
    //      If we get 0, we found a solution with no reserve people.
    // If it's return value is 0 or greater, we got a solution, save it.
    // Now we will go to the next person, therefore we have to undo the person we just planned in. We don't have to push
    // him back, else we would have an endless loop.
    // (*) Because our recursion child has pushed it's person back onto the stack, we can now examine it. Because the
    //     rules are now different, the person could now be possible.
    //  Repeat until no people are left.
    private int solveBackTrackReserve(){
        Queue<SchedulingPerson> myQueue = new LinkedList<>();
        while(!stack.isEmpty()){
            SchedulingPerson sp = stack.pop();
            if (canPersonBePlannedIn(sp.getShift(), sp.getEd(), sp.getPl())) {
                planPersonIn(sp.getShift(), sp.getEd(), sp.getPl());
            } else {
                tempPlanning.get(sp.getShift()).get(sp.getEd()).remove(sp.getPl());
            }
            int check = checkFinalPlanning();
            if(check == -2){
                myQueue.add(sp);
            } else {
                if(check >= 0){
                    maxReserve = check;
                    copyFinalPlanningToMaxPlanning();
                }
                int sbt = solveBackTrackReserve();
                if (sbt >= 0) {
                    if (sbt > maxReserve) {
                        maxReserve = sbt;
                        copyFinalPlanningToMaxPlanning();
                    }
                }
            }
            deletePersonFromPlanning(sp.getShift(), sp.getEd(), sp.getPl());
        }
        while(!myQueue.isEmpty()){
            stack.push(myQueue.remove());
        }
        return checkFinalPlanning();
    }

    // This algorithm checks if there is at least one solution possible, if so, return a number greater than -1.
    // It is almost the same as solveBackTrackReserve, but is shortcutted. This way it does not search any further
    // if one solution is found.
    private int solveBackTrack(int i){
        Queue<SchedulingPerson> myQueue = new LinkedList<>();
        while(!stack.isEmpty()){
            SchedulingPerson sp = stack.pop();
            if (canPersonBePlannedIn(sp.getShift(), sp.getEd(), sp.getPl())) {
                planPersonIn(sp.getShift(), sp.getEd(), sp.getPl());

            } else {
                tempPlanning.get(sp.getShift()).get(sp.getEd()).remove(sp.getPl());
            }
            int check = checkFinalPlanning();

            // We only want a certain solution, so shortcut the backtracker.
            if(check == -2) {
                myQueue.add(sp);
            }
            else{
                if(check >= 0){
                    copyFinalPlanningToMaxPlanning();
                    return check;
                }
                int sbt = solveBackTrack(i+1);
                if (sbt >= 0) {
                    copyFinalPlanningToMaxPlanning();
                    return sbt;
                }
            }
            deletePersonFromPlanning(sp.getShift(), sp.getEd(), sp.getPl());
        }
        while(!myQueue.isEmpty()){
            stack.push(myQueue.remove());
        }
        int check = checkFinalPlanning();
        return check;
    }

    // To see the meaning of every variable, please check getAvailablePeoplePerDayPartForPlanningUnits
    // Returns a JSON of the form
    //
    //  [
    //      {
    //          date: "yyyy-mm-dd",
    //          dayPart: "AM" or "PM",
    //          totalMaxReserve: int,
    //          totalMinReserve: int,
    //          totalNumberNeeded: int,
    //          shift 'shiftname':
    //          {
    //              numberOfPeopleNeeded: int,
    //              reserve: int,
    //              requiredPeople:    String[],
    //              alternativePeople: String[],
    //              finalPeople:       String[],
    //          }
    //      },
    //      ...
    //  ]
    private JsonNode jsonIfyReserve(){
        Map<Shift, Map<ExactDate, Set<PlanningMember>>> planning = maxPlanning;
        ArrayNode arr = Json.newArray();
        for(ExactDate ed: allDates){
            ObjectNode node = arr.addObject();
            node.put("date", ed.getDate().toString());
            node.put("dayPart", ed.getDaypart().toString());
            int totalNumberNeeded = 0;
            //int totalReserve = 0;
            for(String unit: units.keySet()){
                for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                    totalNumberNeeded += peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue());
                }
            }
            node.put("totalNumberNeeded", totalNumberNeeded);
            ArrayNode shiftsNode = node.putArray("shifts");
            if(maxPlanning!= null) {
                for (PlanningLong unit : units.values()) {
                    for (Shift shift : unit.getShifts()) {
                        ObjectNode shiftNode = shiftsNode.addObject();
                        shiftNode.put("name", shift.getName());
                        if (maxPlanning.get(shift).containsKey(ed)) {
                            shiftNode.put("reserve", maxPlanning.get(shift).get(ed).size() - peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue()));
                        } else {
                            shiftNode.put("reserve", 0);
                        }
                        shiftNode.put("numberOfPeopleNeeded", peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue()));
                        ArrayNode arr1 = shiftNode.putArray("requiredPeople");
                        for (PlanningMember pl : peopleWithCorrectSkills.get(shift).get(REQUIRED).get(ed)) {
                            arr1.add(pl.getMember());
                        }

                        ArrayNode arr2 = shiftNode.putArray("alternativePeople");
                        for (PlanningMember pl : peopleWithCorrectSkills.get(shift).get(ALTERNATIVE).get(ed)) {
                            arr2.add(pl.getMember());
                        }

                        ArrayNode arr3 = shiftNode.putArray("finalPeople");
                        for (PlanningMember pl : planning.get(shift).get(ed)) {
                            arr3.add(pl.getMember());
                        }
                    }
                }
                if(totalNumberNeeded != 0) {
                    node.put("totalMaxReserve", getMaxReserveMembers(ed));
                    node.put("totalMinReserve", getMinReserveMembers(ed));
                } else {
                    node.put("totalMaxReserve", 0);
                    node.put("totalMinReserve", 0);
                }
            } else {
                node.put("solutionPossible", false);
            }
        }
        return arr;
    }

    // Find the maximum number of people that can be deleted on a way all shifts are still fulfilled.
    // To do this, we will take members out of the final planning with the least amount of conflicts in the other
    // shifts.
    private int getMaxReserveMembers(ExactDate ed){
        int reservePeople = 0;
        // Keep track of the shifts a person is in.
        if(maxPlanning != null) {
            // Was Map<PlanningMember, Set<Shift>> earlier, that was wrong, because there are multiple planningMembers
            // for the same member, thus we have to keep track of the member, or their ID => String
            Map<String, Set<Shift>> shiftsPersonIsIn = new HashMap<>();
            Map<Shift, Set<String>> maxPlanningCopy = new HashMap<>();
            for(String unit: units.keySet()){
                for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                    for (PlanningMember pl : maxPlanning.get(shift).get(ed)) {
                        if (!maxPlanningCopy.containsKey(shift)) {
                            maxPlanningCopy.put(shift, new HashSet<>());
                        }
                        maxPlanningCopy.get(shift).add(pl.getMember());
                        if (!shiftsPersonIsIn.containsKey(pl.getMember())) {
                            shiftsPersonIsIn.put(pl.getMember(), new HashSet<>());
                        }
                        shiftsPersonIsIn.get(pl.getMember()).add(shift);
                    }
                }
            }
            // If a person is in only one shift, he can safely be deleted if that shift has enough people left.
            for (String plId : shiftsPersonIsIn.keySet()) {
                if (shiftsPersonIsIn.get(plId).size() == 1) {
                    //You can't do something like .get(0) or .first() in a hashset.
                    for (Shift shift : shiftsPersonIsIn.get(plId)) {
                        if ((maxPlanningCopy.get(shift).size() - reservePeople) > peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue())) {
                            // Would be more performant, can't be done because of the for-loop with the keys of a set.
                            //maxPlanningCopy.get(shift).remove(pl);
                            //shiftsPersonIsIn.remove(pl);
                            reservePeople += 1;
                        }
                    }
                }
            }
            // Get max number of shifts a person is in.
            int maxShiftsAPersonHas = 0;
            for (Collection col : shiftsPersonIsIn.values()) {
                if (col.size() > maxShiftsAPersonHas) {
                    maxShiftsAPersonHas = col.size();
                }
            }
            // For people with more than one shift, it gets more complicated.
            // We start with two. If a person is in two shifts, and those two shifts both have more people in them than
            //      needed, the person can be safely deleted from them.
            // this algorithm is not optimal.
            //      When there are three shifts
            //          person one is in shift 1 and 2
            //          person two is in shift 1 and 3
            //          person three is in shift 2 and 3
            //          person four is in shift 1 and 2
            //
            //          shift 1 has 2 people more than needed
            //          shift 2 has 2 people more than needed
            //          shift 3 has 2 people more than needed
            //
            //          If people one, two and three are planned in, we have the best solution with three reserve people
            //          if people one and four are planned in, nobody else can be planned in, and only 2 people instead
            //              of three can be planned in.
            // In theory, we should check every solution. We won't do that here.
            if (maxShiftsAPersonHas > 1) {
                for (int i = 2; i <= maxShiftsAPersonHas; i++) {
                    for (String plId : shiftsPersonIsIn.keySet()) {
                        if (shiftsPersonIsIn.get(plId).size() == i) {
                            boolean allShiftsHaveMoreThanNeeded = true;

                            for (Shift shift : shiftsPersonIsIn.get(plId)) {
                                if (maxPlanningCopy.get(shift).size() <= peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue())) {
                                    allShiftsHaveMoreThanNeeded = false;
                                }
                            }

                            if (allShiftsHaveMoreThanNeeded) {
                                // Would be more performant, can't be done because of the for-loop with the keys of a set.
                                //for (Shift shift : shiftsPersonIsIn.get(pl)) {
                                //    maxPlanningCopy.get(shift).remove(pl);
                                //}
                                //shiftsPersonIsIn.remove(pl);
                                reservePeople += 1;
                            }
                        }
                    }
                }
            }
        }
        return reservePeople;
    }

    // Find the minimum number of people that can be deleted on a way all shifts are still fulfilled.
    // This one is easy. Just find the shift with the least amount of reserve people.
    private int getMinReserveMembers(ExactDate ed){
        int reservePeople = 999999999;
        // Keep track of the shifts a person is in.
        if(maxPlanning != null) {
            Map<Shift, Set<PlanningMember>> maxPlanningCopy = new HashMap<>();
            for(String unit: units.keySet()){
                for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                    for (PlanningMember pl : maxPlanning.get(shift).get(ed)) {
                        if (!maxPlanningCopy.containsKey(shift)) {
                            maxPlanningCopy.put(shift, new HashSet<>());
                        }
                        maxPlanningCopy.get(shift).add(pl);
                    }
                }
            }
            for(String unit: units.keySet()) {
                for(Shift shift: shiftsPerPlanningsUnitID.get(unit)) {
                    if(maxPlanningCopy.containsKey(shift)) {
                        if (maxPlanningCopy.get(shift).size() - peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue()) < reservePeople) {
                            reservePeople = maxPlanningCopy.get(shift).size() - peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue());
                        }
                    }
                }
            }
        }
        return reservePeople;
    }

    // Before calling this function, canPersonBePlannedIn should have been called and should have returned true.
    // To plan a person in, it should be moved from the tempPlanning to the finalPlanning.
    // He should be removed from the tempPlanning that are simultaneous or have certain incompatible constraints.
    private void planPersonIn(Shift shift, ExactDate ed, PlanningMember pl){
        tempPlanning.get(shift).get(ed).remove(pl);
        finalPlanning.get(shift).get(ed).add(pl);
        // Delete this person in every shift that is simultaneous with this shift.
        // Checking Planningmembers instead of members is ok, simultaneous shifts only occur under the same planningunits.
        for(PlanningLong planning: units.values()){
            for(SimultaneousShift simshiftobject: planning.getSimultaneousShifts()){
                for(String simshift: simshiftobject.getShifts()){
                    if(simshift.equals(shift.getId())){
                        for(String shiftToDeleteString: simshiftobject.getShifts()){
                            if(!shiftToDeleteString.equals(shift.getId())){
                                Shift shiftToDelete = null;
                                for(Shift shiftje: shiftsPerPlanningsUnitID.get(planning.getId())){
                                    if(shiftje.getId().equals(shiftToDeleteString)){
                                        shiftToDelete = shiftje;
                                    }
                                }
                                tempPlanning.get(shiftToDelete).get(ed).remove(pl);
                            }
                        }
                    }
                }
            }
        }

        // Delete this person in every shift that this shift has recup on, or that has recup on this shift.
        // NOTE: We should also look under other planningunits where this person is presented by other planningMembers
        //      Thus we should check if the planningMembers' ID is the same, not if the whole planningsmember is the smame.
        // If a shift happens, and this shift is AM and another is PM, what should we do?
        for(String unit: units.keySet()){
            for(Shift othershift: shiftsPerPlanningsUnitID.get(unit)) {
                ExactDate othered = new ExactDate(ed.getDate().plusDays(maxRecup), DayPart.AM);
                while (othered.getDate().isAfter(ed.getDate()) && allDates.contains(othered)) {
                    if (othered.getDate().isAfter(ed.getDate()) && DAYS.between(ed.getDate(), othered.getDate()) <= weekdays.get(shift).get(ed.getDate().getDayOfWeek().getValue()).getFreePeriodDays()) {
                        Set<SchedulingPerson> peopleToDelete = new HashSet<>();
                        for(PlanningMember otherpl: tempPlanning.get(othershift).get(othered)){
                            // In other planningunits, a member is another planningsmember object, with the same memberID.
                            if(otherpl.getMember().equals(pl.getMember())){
                                // Concurrent modification exception
                                // tempPlanning.get(othershift).get(othered).remove(otherpl);
                                peopleToDelete.add(new SchedulingPerson(othershift, othered, otherpl));
                            }
                        }
                        for(SchedulingPerson p: peopleToDelete){
                            tempPlanning.get(p.getShift()).get(p.getEd()).remove(p.getPl());
                        }

                    }
                    if (othered.getDaypart().equals(DayPart.AM)) {
                        othered.setDaypart(DayPart.PM);
                    } else {
                        othered.setDate(othered.getDate().minusDays(1));
                        othered.setDaypart(DayPart.AM);
                    }
                }

                othered = new ExactDate(ed.getDate().minusDays(maxRecup), DayPart.AM);
                while (othered.getDate().isBefore(ed.getDate()) && allDates.contains(othered)) {
                    if (othered.getDate().isBefore(ed.getDate()) && DAYS.between(othered.getDate(), ed.getDate()) <= weekdays.get(shift).get(othered.getDate().getDayOfWeek().getValue()).getFreePeriodDays()) {
                        for(PlanningMember otherpl: tempPlanning.get(othershift).get(othered)){
                            // In other planningunits, a member is another planningsmember object, with the same memberID.
                            if(otherpl.getMember().equals(pl.getMember())){
                                tempPlanning.get(othershift).get(othered).remove(otherpl);
                            }
                        }
                    }
                    if (othered.getDaypart().equals(DayPart.AM)) {
                        othered.setDaypart(DayPart.PM);
                    } else {
                        othered.setDate(othered.getDate().plusDays(1));
                        othered.setDaypart(DayPart.AM);
                    }
                }
            }
        }
    }

    // When we plan a person out of the finalPlanning, we can't just delete it. To enable the backtracker to do it's
    // work, it should also be planned back in the tempPlanning.
    // When planning a person in, we check al other places where this person can no longer be planned in and delete
    // the person from those places in the tempPlanning. When deleting the person from the planning, this should be
    // undone.
    // That makes this function the opposite of planPersonIn.
    private void deletePersonFromPlanning(Shift shift, ExactDate ed, PlanningMember pl){
        tempPlanning.get(shift).get(ed).add(pl);
        finalPlanning.get(shift).get(ed).remove(pl);

        // If this person has the required or alternative skills for a simultaneous shift, and he has no other
        // constraints, he can be put in the tempPlanning again.
        for(PlanningLong planning: units.values()){
            for(SimultaneousShift simshiftobject: planning.getSimultaneousShifts()){
                for(String simshift: simshiftobject.getShifts()){
                    if(simshift.equals(shift.getId())){
                        for(String shiftToAddString: simshiftobject.getShifts()){
                            if(!shiftToAddString.equals(shift.getId())) {
                                Shift shiftToAdd = null;
                                for (Shift shiftje : shiftsPerPlanningsUnitID.get(planning.getId())) {
                                    if (shiftje.getId().equals(shiftToAddString)) {
                                        shiftToAdd = shiftje;
                                    }
                                }
                                if ((peopleWithCorrectSkills.get(shift).get(REQUIRED).get(ed).contains(pl)
                                        || peopleWithCorrectSkills.get(shift).get(ALTERNATIVE).get(ed).contains(pl))
                                        && canPersonBePlannedIn(shift, ed, pl)) {
                                    tempPlanning.get(shiftToAdd).get(ed).add(pl);
                                }
                            }
                        }
                    }
                }
            }
        }
        // Add this person in every shift that this sift has recup on, or that has recup on this shift, if he can be
        // planned in there.
        for(String unit: units.keySet()) {
            for (Shift othershift : shiftsPerPlanningsUnitID.get(unit)) {
                ExactDate othered = new ExactDate(ed.getDate().plusDays(maxRecup), DayPart.AM);
                while (othered.getDate().isAfter(ed.getDate()) && allDates.contains(othered)) {
                    for(PlanningMember otherpl: planningMembersPerPlanningsID.get(unit)){
                        if(otherpl.getMember().equals(pl.getMember())){
                            if (peopleWithCorrectSkills.get(othershift).get(REQUIRED).get(othered).contains(otherpl) || peopleWithCorrectSkills.get(othershift).get(ALTERNATIVE).get(othered).contains(otherpl)) {
                                if (DAYS.between(ed.getDate(), othered.getDate()) <= weekdays.get(shift).get(ed.getDate().getDayOfWeek().getValue()).getFreePeriodDays()) {
                                    if (canPersonBePlannedIn(shift, othered, otherpl)) {
                                        //We can't just add pl, if we are in another planningunit, this is the wrong object!
                                        //tempPlanning.get(shift).get(othered).add(pl);
                                        tempPlanning.get(shift).get(othered).add(otherpl);
                                    }
                                }
                            }
                        }
                    }
                    if (othered.getDaypart().equals(DayPart.AM)) {
                        othered.setDaypart(DayPart.PM);
                    } else {
                        othered.setDate(othered.getDate().minusDays(1));
                        othered.setDaypart(DayPart.AM);
                    }
                }

                othered = new ExactDate(ed.getDate().minusDays(maxRecup), DayPart.AM);
                while (othered.getDate().isBefore(ed.getDate()) && allDates.contains(othered)) {
                    for(PlanningMember otherpl: planningMembersPerPlanningsID.get(unit)) {
                        if(otherpl.getMember().equals(pl.getMember())) {
                            if (peopleWithCorrectSkills.get(othershift).get(REQUIRED).get(othered).contains(otherpl) || peopleWithCorrectSkills.get(othershift).get(ALTERNATIVE).get(othered).contains(otherpl)) {
                                if (DAYS.between(othered.getDate(), ed.getDate()) <= weekdays.get(shift).get(othered.getDate().getDayOfWeek().getValue()).getFreePeriodDays()) {
                                    if (canPersonBePlannedIn(shift, othered, otherpl)) {
                                        //We can't just add pl, if we are in another planningunit, this is the wrong object!
                                        //tempPlanning.get(shift).get(othered).add(pl);
                                        tempPlanning.get(shift).get(othered).add(otherpl);
                                    }
                                }
                            }
                        }
                    }
                    if (othered.getDaypart().equals(DayPart.AM)) {
                        othered.setDaypart(DayPart.PM);
                    } else {
                        othered.setDate(othered.getDate().plusDays(1));
                        othered.setDaypart(DayPart.AM);
                    }
                }
            }
        }
    }

    // Check if a person can be planned in, returns true if possible.
    // A person can not be planned in if this shift has recup on a shift where this person is planned in, or that shift
    //      has recup on this shift.
    // A person can also not be planned in if it is simultenous with a shift where this person is planned in.
    private boolean canPersonBePlannedIn(Shift shift, ExactDate ed, PlanningMember pl){
        // Make a list of all people that can not be planned in if this person would be planned in
        List<SchedulingObject> affectedPeople = new ArrayList<>();
        // Recup
        for(String unit: units.keySet()) {
            for (Shift othershift : shiftsPerPlanningsUnitID.get(unit)) {
                ExactDate othered = new ExactDate(ed.getDate().plusDays(maxRecup), DayPart.AM);
                while (othered.getDate().isAfter(ed.getDate()) && allDates.contains(othered)) {
                    if (allDates.contains(othered)) {
                        // Old way => WRONG because in another planningUnit, the person is presented by another planningMember with the same memberID!
                        /*if (tempPlanning.get(othershift).get(othered).contains(pl)) {
                            if (othered.getDate().isAfter(ed.getDate()) && DAYS.between(ed.getDate(), othered.getDate()) <= weekdays.get(shift).get(ed.getDate().getDayOfWeek().getValue()).getFreePeriodDays()) {
                                affectedPeople.add(new SchedulingObject(othershift, othered));
                            }
                        }*/
                        for(PlanningMember plmember: tempPlanning.get(othershift).get(othered)){
                            if(plmember.getMember().equals(pl.getMember())){
                                if (othered.getDate().isAfter(ed.getDate()) && DAYS.between(ed.getDate(), othered.getDate()) <= weekdays.get(shift).get(ed.getDate().getDayOfWeek().getValue()).getFreePeriodDays()) {
                                    affectedPeople.add(new SchedulingObject(othershift, othered));
                                }
                            }
                        }
                    }
                    if (othered.getDaypart().equals(DayPart.AM)) {
                        othered.setDaypart(DayPart.PM);
                    } else {
                        othered.setDate(othered.getDate().minusDays(1));
                        othered.setDaypart(DayPart.AM);
                    }
                }

                othered = new ExactDate(ed.getDate().minusDays(maxRecup), DayPart.AM);
                while (othered.getDate().isBefore(ed.getDate()) && allDates.contains(othered)) {
                    if (allDates.contains(othered)) {
                        for(PlanningMember otherpl: planningMembersPerPlanningsID.get(unit)) {
                            if (otherpl.getMember().equals(pl.getMember())) {
                                /*if (tempPlanning.get(othershift).get(othered).contains(otherpl)) {
                                    if (othered.getDate().isBefore(ed.getDate()) && DAYS.between(othered.getDate(), ed.getDate()) <= weekdays.get(shift).get(othered.getDate().getDayOfWeek().getValue()).getFreePeriodDays()) {
                                        affectedPeople.add(new SchedulingObject(othershift, othered));
                                    }
                                }*/
                                for(PlanningMember plmember: tempPlanning.get(othershift).get(othered)){
                                    if(plmember.getMember().equals(pl.getMember())){
                                        if (othered.getDate().isBefore(ed.getDate()) && DAYS.between(othered.getDate(), ed.getDate()) <= weekdays.get(shift).get(othered.getDate().getDayOfWeek().getValue()).getFreePeriodDays()) {
                                            affectedPeople.add(new SchedulingObject(othershift, othered));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (othered.getDaypart().equals(DayPart.AM)) {
                        othered.setDaypart(DayPart.PM);
                    } else {
                        othered.setDate(othered.getDate().plusDays(1));
                        othered.setDaypart(DayPart.AM);
                    }
                }
            }
            if(!affectedPeople.isEmpty()){
                return false;
            }
        }
        // If there is a simultaneous shift, and the person is planned in that shift, he can't be in this shift.
        for(PlanningLong planning: units.values()){
            if(planning.getShifts().contains(shift)){
                for(SimultaneousShift simShift: planning.getSimultaneousShifts()){
                    if(Arrays.asList(simShift.getShifts()).contains(shift.getId())){
                        for(Shift othershift: shiftsPerPlanningsUnitID.get(planning.getId())){
                            if(Arrays.asList(simShift.getShifts()).contains(othershift.getId())){
                                if(!shift.equals(othershift)) {
                                    if (finalPlanning.get(othershift).get(ed).contains(pl)) {
                                        affectedPeople.add(new SchedulingObject(othershift, ed)); }
                                }
                            }
                        }
                    }
                }
            }
        }
        // If that list is empty, the person can be planned in.
        return affectedPeople.isEmpty();
    }

    // Returns -2 if planning can not be fulfilled, this is a wrong path.
    // Returns -1 if planning is not yet fully planned, but it could be possible.
    // Returns 0 if planning is fully planned with no reserve people.
    // Returns a bigger number if the planning is fully planned that is equal to the number of reserve people.
    //      NOTE: this is a good identifier of the best planner, but it is not the actual number of reserve people.
    //          If a person is possible in two shifts on the same day, this algorithm will return 2 for that day,
    //          But in the end, only one person is reserve. See getMaxReserveMembers for a correct calculation.
    private int checkFinalPlanning(){
        // If the planning can never be fulfilled => If the number of people that are planned in + the number of people
        //      that could be planned in is smaller than the number of people that must be planned in.
        for(String unit: units.keySet()) {
            for (Shift shift : shiftsPerPlanningsUnitID.get(unit)) {
                for (ExactDate ed : allDates) {
                    if (tempPlanning.get(shift).get(ed).size() + finalPlanning.get(shift).get(ed).size() < peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue())) {
                        return -2;
                    }
                }
            }
        }
        // If the finalPlanning is not fully planned => The number of planned in people is smaller than the number of
        //      needed people.
        for(String unit: units.keySet()) {
            for (Shift shift : shiftsPerPlanningsUnitID.get(unit)) {
                for (ExactDate ed : allDates) {
                    if (finalPlanning.get(shift).get(ed).size() < peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue())) {
                        return -1;
                    }
                }
            }
        }
        // Return the number of reserve people. This is the sum of the number that are planned in with the number of
        //      people that could be planned in minus the number of people needed that day.
        int reservePeople = 0;
        for(String unit: units.keySet()) {
            for (Shift shift : shiftsPerPlanningsUnitID.get(unit)) {
                for (ExactDate ed : allDates) {
                    if (peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue()) != 0) {
                        reservePeople += tempPlanning.get(shift).get(ed).size() + finalPlanning.get(shift).get(ed).size() - peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue());
                    }
                }
            }
        }
        return reservePeople;
    }


    // This function fills the stack for the backtracker.
    // Doing this wrong or right can have a very big impact on the performance of the backtracker.
    // Ideally, the backtracker gets the people first of the shifts that have the least possible people.
    // This way, less people have to be tested and the recursion tree can get pruned of way earlier.
    // To do this, we have to fill the stack in reverse order, with the shifts that have a lot of possibilities first.
    private Stack<SchedulingPerson> prepareBackStack(){
        Stack<SchedulingPerson> stack = new Stack<>();
        Map<Integer, List<SchedulingPerson>> schedulingPersonsPerPeopleNeeded = new HashMap<>();
        int min = 999999999;
        int max = -9999999;
        for(String unit: units.keySet()) {
            for (Shift shift : shiftsPerPlanningsUnitID.get(unit)) {
                for (ExactDate ed : allDates) {
                    for (PlanningMember pl : tempPlanning.get(shift).get(ed)) {
                        int peopleToPlanIn = peopleNeeded.get(shift).get(ed.getDate().getDayOfWeek().getValue()) - finalPlanning.get(shift).get(ed).size();
                        if(peopleToPlanIn > max){
                            max = peopleToPlanIn;
                        }
                        if(peopleToPlanIn < min){
                            min = peopleToPlanIn;
                        }
                        if(schedulingPersonsPerPeopleNeeded.containsKey(peopleToPlanIn)) {
                            schedulingPersonsPerPeopleNeeded.get(peopleToPlanIn).add(new SchedulingPerson(shift, ed, pl));
                        } else{
                            schedulingPersonsPerPeopleNeeded.put(peopleToPlanIn, new ArrayList<>());
                            schedulingPersonsPerPeopleNeeded.get(peopleToPlanIn).add(new SchedulingPerson(shift, ed, pl));
                        }
                        stack.push(new SchedulingPerson(shift, ed, pl));
                    }
                }
            }
        }
        for(int i = max; i >= min; i--){
            if(schedulingPersonsPerPeopleNeeded.containsKey(i)){
                for(SchedulingPerson sp: schedulingPersonsPerPeopleNeeded.get(i)){
                    stack.push(sp);
                }
            }
        }
        return stack;
    }

    /**
     * Initiates maxPlanning with all shifts and exact dates loaded.
     */
    private void emptyMaxPlanning(){
        maxPlanning = new HashMap<>();
        for(String unit: units.keySet()) {
            for (Shift shift : shiftsPerPlanningsUnitID.get(unit)) {
                maxPlanning.put(shift, new HashMap<>());
                for (ExactDate ed : allDates) {
                    maxPlanning.get(shift).put(ed, new HashSet<>());
                    maxPlanning.get(shift).put(ed, new HashSet<>());
                }
            }
        }
    }

    /**
     * Copy the final planning to the maxPlanning.
     * NOTE: it is not a deep copy, maxPlanning just gets filled for every shift and every ed in finalPlanning
     */
    private void copyFinalPlanningToMaxPlanning(){
        emptyMaxPlanning();
        for(String unit: units.keySet()) {
            for (Shift shift : shiftsPerPlanningsUnitID.get(unit)) {
                for (ExactDate ed : allDates) {
                    for (PlanningMember pl : finalPlanning.get(shift).get(ed)) {
                        maxPlanning.get(shift).get(ed).add(pl);
                    }
                }
            }
        }
    }

    // Map<Integer, WeekDay> means the following:
    //      The integer stands for the weekday => 1 = monday, 2 = tuesday, ...
    //      A WeekDay object contains the needed people and skills on one day.
    private static Map<Integer, WeekDay> makeWeekDayMap(Shift shift){
        Map<Integer, WeekDay> demandsMap = new HashMap<>();
        demandsMap.put(1, shift.getDemands().getMONDAY());
        demandsMap.put(2, shift.getDemands().getTUESDAY());
        demandsMap.put(3, shift.getDemands().getWEDNESDAY());
        demandsMap.put(4, shift.getDemands().getTHURSDAY());
        demandsMap.put(5, shift.getDemands().getFRIDAY());
        demandsMap.put(6, shift.getDemands().getSATURDAY());
        demandsMap.put(7, shift.getDemands().getSUNDAY());
        return demandsMap;
    }

    // Makes a Map<Integer, WeekDay> for every Shift.
    // See makeWeekDayMap.
    private static Map<Shift, Map<Integer, WeekDay>> makeWeekDayMaps(List<Shift> shifts){
        Map<Shift, Map<Integer, WeekDay>> weekdays = new HashMap<>();
        for(Shift shift: shifts){
            weekdays.put(shift, makeWeekDayMap(shift));
        }
        return weekdays;
    }

    private static PlanningMember getPlanningMemberWithId(List<PlanningMember> list, String id){
        for(PlanningMember member: list){
            if(member.getMember().equals(id)){
                return member;
            }
        }
        return null;
    }
}
