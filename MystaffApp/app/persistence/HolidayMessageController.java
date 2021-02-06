package persistence;

import businessLogic.PlanningUnitHelper;
import enumerations.HolidayType;
import enumerations.SchedulingState;
import utilityClasses.*;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HolidayMessageController {

    @Inject
    private PlanningUnitHelper planningUnitHelper;

    public HolidayMessage getHolidayMessageWithDatabaseID(long id) {
        return HolidayMessage.find.byId(id);
    }

    /**
     * Retrieves the first 2500 HolidayMessages from the database that
     * are linked to the employee with the passed ID. The HolidayMessages
     * are ordered by their 'requestData' property.
     *
     * @param ID id of the employee.
     * @return List<HolidayMessage> of this employee.
     */
    public List<HolidayMessage> getHolidayMessageWithEmployeeID(String ID) {
        return HolidayMessage.find.query().where()
                .ilike("employeeID", ID)
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
    }

    /**
     * Retrieves the first 2500 approved (state = SchedulingState.Approved)
     * HolidayMessages from the database that are linked to the doctor with
     * the passed ID. The HolidayMessages are ordered by their 'requestData'
     * property.
     *
     * @param ID id of the doctor.
     * @return List<HolidayMessage> of this doctor, which are all approved.
     */
    public List<HolidayMessage> getHolidayMessagesApprovedOfDoctorWithID(String ID) {
        return HolidayMessage.find.query().where()
                .ilike("employeeID", ID)
                .and()
                .ilike("state", SchedulingState.Approved.ordinal() + "")
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
    }

    /**
     * Retrieves the first 2500 rejected (state = SchedulingState.Rejected)
     * HolidayMessages from the database that are linked to the doctor with
     * the passed ID. The HolidayMessages are ordered by their 'requestData'
     * property.
     *
     * @param ID id of the doctor.
     * @return List<HolidayMessage> of this doctor, which are all rejected.
     */
    public List<HolidayMessage> getHolidayMessagesRejectedOfDoctorWithID(String ID) {
        return HolidayMessage.find.query().where()
                .ilike("employeeID", ID)
                .and()
                .ilike("state", SchedulingState.Rejected.ordinal() + "")
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
    }

    /**
     * Retrieves the first 2500 HolidayMessages, which are being considered,
     * (state = SchedulingState.InConsideration) from the database that are
     * linked to the doctor with the passed ID. The HolidayMessages are
     * ordered by their 'requestData' property.
     *
     * @param ID id of the doctor.
     * @return List<HolidayMessage> of this doctor, which are all being considered.
     */
    public List<HolidayMessage> getHolidayMessagesInConsiderationOfDoctorWithID(String ID) {
        return HolidayMessage.find.query().where()
                .ilike("employeeID", ID)
                .and()
                .ilike("state", SchedulingState.InConsideration.ordinal() + "")
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
    }

    /**
     * Retrieves the first 2500 new (state = SchedulingState.New) HolidayMessages
     * from the database that are linked to the doctor with the passed ID. The
     * HolidayMessages are ordered by their 'requestData' property.
     *
     * @param ID id of the doctor.
     * @return List<HolidayMessage> of this doctor, which are all new.
     */
    public List<HolidayMessage> getHolidayMessagesNewOfDoctorWithID(String ID) {
        return HolidayMessage.find.query().where()
                .ilike("employeeID", ID)
                .and()
                .ilike("state", SchedulingState.New.ordinal() + "")
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
    }

    /**
     * Retrieves the first 2500 approved (state = SchedulingState.Approved)
     * HolidayMessages from the database. The HolidayMessages are ordered
     * by their 'requestData' property.
     *
     * @return List<HolidayMessage> which are all approved.
     */
    public List<HolidayMessage> getAllHolidayMessagesApproved() {
        return HolidayMessage.find.query().where()
                .ilike("state", SchedulingState.Approved.ordinal() + "")
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
    }

    /**
     * Retrieves the first 2500 rejected (state = SchedulingState.Rejected)
     * HolidayMessages from the database. The HolidayMessages are ordered
     * by their 'requestData' property.
     *
     * @return List<HolidayMessage> which are all rejected.
     */
    public List<HolidayMessage> getAllHolidayMessagesRejected() {
        return HolidayMessage.find.query().where()
                .ilike("state", SchedulingState.Rejected.ordinal() + "")
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
    }

    /**
     * Retrieves the first 2500 HolidayMessages, which are being considered,
     * (state = SchedulingState.InConsideration) from the database. The
     * HolidayMessages are ordered by their 'requestData' property.
     *
     * @return List<HolidayMessage> which are all being considered.
     */
    public List<HolidayMessage> getAllHolidayMessagesInConsideration() {
        return HolidayMessage.find.query().where()
                .ilike("state", SchedulingState.InConsideration.ordinal() + "")
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
    }

    /**
     * Retrieves the first 2500 new (state = SchedulingState.New) HolidayMessages
     * from the database. The HolidayMessages are ordered by their 'requestData' property.
     *
     * @return List<HolidayMessage> which are all new.
     */
    public List<HolidayMessage> getAllHolidayMessagesNew() {
        return HolidayMessage.find.query().where()
                .ilike("state", SchedulingState.New.ordinal() + "")
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
    }

    /**
     * Retrieves the first 2500 HolidayMessages from the database whose 'type'
     * property is equal to the HolidayType that is passed as a parameter.
     * The HolidayMessages are ordered by their 'requestData' property.
     *
     * @param type indicates the kind of HolidayMessage.
     * @return List<HolidayMessage> of this type.
     */
    public List<HolidayMessage> getAllHolidayMessagesByType(HolidayType type) {
        return HolidayMessage.find.query().where()
                .ilike("type", type.toString())
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
    }

    /**
     * Retrieves all HolidayMessages from the database.
     *
     * @return List<HolidayMessage> containing all HolidayMessages in the database.
     */
    public List<HolidayMessage> getAllHolidayMessages() {
        return HolidayMessage.find.all();
    }

    /**
     * Retrieves all HolidayMessages from the database which occur after
     * the specified date.
     *
     * @param date indicates starting from what date HolidayMessages should
     *             be returned.
     * @return List<HolidayMessage> after date.
     */
    public List<HolidayMessage> getAllHolidayMessagesAfter(LocalDate date) {
        List<HolidayMessage> returnmsgs;
        List<HolidayMessage> querymsgs;
        querymsgs = HolidayMessage.find.query()
                .where()
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
        returnmsgs = new ArrayList<>();
        for (HolidayMessage msg : querymsgs) {
            if (msg.getExactDates().get(msg.getExactDates().size() - 1).getDate().toEpochDay() > date.toEpochDay()) {
                returnmsgs.add(msg);
            }
        }
        return returnmsgs;
    }

    /**
     * Retrieves all HolidayMessages from the database which occur after
     * the specified date for a specific employee.
     *
     * @param id is the id of the employee
     * @param date indicates starting from what date HolidayMessages should
     *             be returned.

     * @return List<HolidayMessage> after date.
     */
    public List<HolidayMessage> getAllHolidayMessagesOfEmployeeAfter(String id, LocalDate date) {
        List<HolidayMessage> returnmsgs;
        List<HolidayMessage> querymsgs;
        querymsgs = HolidayMessage.find.query()
                .where()
                .ilike("employeeID", id)
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
        returnmsgs = new ArrayList<>();
        for (HolidayMessage msg : querymsgs) {
            if (msg.getExactDates().get(msg.getExactDates().size() - 1).getDate().toEpochDay() > date.toEpochDay()) {
                returnmsgs.add(msg);
            }
        }
        return returnmsgs;
    }

    /**
     * Retrieves all HolidayMessages from the database which occur after
     * the specified date.
     *
     * @param start indicates starting from what date HolidayMessages should
     *             be returned.
     * @param end indicates the end date before which HolidayMessages should
     *            be returned.
     * @return List<HolidayMessage> after date.
     */
    public List<HolidayMessage> getAllHolidayMessagesBetween(LocalDate start, LocalDate end) {
        List<HolidayMessage> returnmsgs;
        List<HolidayMessage> querymsgs;
        querymsgs = HolidayMessage.find.query()
                .where()
                .orderBy("requestDate")
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList();
        returnmsgs = new ArrayList<>();
        for (HolidayMessage msg : querymsgs) {
            if (msg.getExactDates().get(msg.getExactDates().size() - 1).getDate().toEpochDay() > start.toEpochDay()
            && msg.getExactDates().get(0).getDate().toEpochDay() < end.toEpochDay()) {
                returnmsgs.add(msg);
            }
        }
        return returnmsgs;
    }

    /**
     * Deletes the HolidayMessage from the database.
     *
     * @param hm HolidayMessage to delete
     */
    public void deleteHolidayMessage(HolidayMessage hm) {
        hm.delete();
    }

    /**
     * Checks if the passed HolidayMessage is already in the database.
     *
     * @param hm HolidayMessage which is being checked.
     * @return true if the HolidayMessage is already in the database,
     * false if it isn't.
     */
    public boolean isAlreadyInDatabase(HolidayMessage hm) {
        return HolidayMessage.find.byId(hm.getId()) != null;
    }

    /**
     * Updates the HolidayMessage in the database that as the same id
     * as the passed HolidayMessage. The ExactDates that are linked to
     * the HolidayMessage in the database will also be updated with the
     * ExactDates of the passed HolidayMessage. The ExactDates of the
     * HolidayMessage in the database will be added to the 'exact_date_hmh'
     * table in the database.
     *
     * @param hm the updated HolidayMessage.
     */
    public void updateHolidayMessage(HolidayMessage hm) {
        HolidayMessage hmUitDatabase = HolidayMessage.find.byId(hm.getId());

        HolidayMessageHistory hmh = new HolidayMessageHistory();
        hmh.setComment(hmUitDatabase.getComment());
        hmh.setHolidayMessage(hmUitDatabase);
        hmh.setRequestByID(hmUitDatabase.getRequestByID());
        hmh.setRequestDate(hmUitDatabase.getRequestDate());
        hmh.setState(hmUitDatabase.getState());
        hmh.setType(hmUitDatabase.getType());
        hmh.setExactDates(new ArrayList<>());

        HolidayMessageHistory hmhUitDatabase = HolidayMessageHistory.find.byId(addHolidayMessageHistory(hmh));

        for (ExactDate ed : hmUitDatabase.getExactDates()) {
            ExactDateHMH edhmh = new ExactDateHMH(ed.getDate(), ed.getDaypart());
            hmhUitDatabase.getExactDates().add(edhmh);
            edhmh.setHolidayMessage(hmhUitDatabase);
            edhmh.save();
        }
        for (ExactDate ed : hmUitDatabase.getExactDates()) {
            if (!hm.getExactDates().contains(ed)) {
                ed.delete();
            }
        }
        for (ExactDate ed : hm.getExactDates()) {
            if (!hmUitDatabase.getExactDates().contains(ed)) {
                hmUitDatabase.getExactDates().add(ed);
                ed.setHolidayMessage(hmUitDatabase);
                ed.save();
            }
        }

        hm.getHistory().add(hmhUitDatabase);
        hmhUitDatabase.update();
        hm.update();
    }

    /**
     * Applies the update of a certain planner to a certain HolidayMessage
     * A HolidayMessage can only be approved if a planner of every planning unit
     *      the employee is in, has approved that HolidayMessage.
     * This function adds the decision a certain planner has made for every
     *      planning unit he is planner of, to the holidayMessages PlanningUnitStates
     *
     *
     * @param token X-Authorization token to be used for requesting data from
     *              the MyStaff API.
     * @param hpm   the HolidayPlanningsMessage that will be used to update the
     *              holidayMessage.
     */
    public void addPlannerUpdate(String token, HolidayPlanningsMessage hpm) throws Exception {
        List<String> unitsOfPlanner = planningUnitHelper.getAllPlanningUnitIDsOfWhichEmployeeIDIsPlanner(token, hpm.getPlannerID());
        addPlannerUpdateForPlanningsUnits(hpm, unitsOfPlanner);
    }

    /**
     * Applies the update of a certain planner to a certain HolidayMessage
     * A HolidayMessage can only be approved if a planner of every planning unit
     *      the employee is in, has approved that HolidayMessage.
     * This function adds the decision a certain planner has made for every
     *      planning unit he is planner of, to the holidayMessages PlanningUnitStates
     *
     * First add the hpm with in it: the plannerID and the decision he made for this absence to the HolidayMessage
     * Then change the state of the HolidayMessage with the new info
     *      If at least one planner rejected the absence, the global state is rejected
     *      If not: if at least one planner has it new: the state is new
     *      If not: if at least one planner has it in consideration: that is the state
     *      If not: state is approved
     *
     * @param hpm   the HolidayPlanningsMessage that will be used to update de
     *              planner.
     * @param unitsOfPlanner    the planning units of the planner
     */
    public void addPlannerUpdateForPlanningsUnits(HolidayPlanningsMessage hpm, List<String> unitsOfPlanner) {
        HolidayMessage hmUitDatabase = HolidayMessage.find.byId(hpm.id);
        boolean containsRejected = false;
        boolean containsNew = false;
        boolean containsInConsideration = false;


        for (PlanningUnitState pus : hmUitDatabase.getPlanningUnitStates()) {
            if (unitsOfPlanner.contains(pus.getUnitId())) {
                pus.setState(hpm.getState());
                pus.setComment(hpm.getComment());
                pus.setPlannerID(hpm.getPlannerID());
                pus.update();
                hmUitDatabase.update();
            }
            if (pus.getState() == SchedulingState.Rejected) {
                containsRejected = true;
            } else if (pus.getState() == SchedulingState.New) {
                containsNew = true;
            } else if (pus.getState() == SchedulingState.InConsideration) {
                containsInConsideration = true;
            }
        }
        if (containsRejected) {
            hmUitDatabase.setState(SchedulingState.Rejected);
        } else if (containsNew) {
            hmUitDatabase.setState(SchedulingState.New);
        } else if (containsInConsideration) {
            hmUitDatabase.setState(SchedulingState.InConsideration);
        } else {
            hmUitDatabase.setState(SchedulingState.Approved);
        }
        hmUitDatabase.save();
    }

    /**
     * Add a holidayMessageHistory to it's HolidayMessage
     * This function is called when an employee changes an absence that was already in the database.
     * Now the old data of the absence is added to a HolidayMessageHistory object and it is replaced by the new data.
     *
     * First save all the hmh's dates in the database, then save the hmh, then get it back out of the database
     *      (this way is used because the save() option does not modify the object's ID)
     * Then save it's dates again.
     * This very not clean method has to be used because of the limitations of ebean.
     * Otherwise, it does not save everything properly.
     *
     * @return the id of the newly saved hmh object
     */
    public long addHolidayMessageHistory(HolidayMessageHistory hmh) {
        List<ExactDateHMH> dates = hmh.getExactDates();
        for (ExactDateHMH date : dates) {
            date.setHolidayMessage(hmh);
        }
        hmh.save();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = "";
        try {
            date = dateFormat.format(hmh.getRequestDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        HolidayMessageHistory hmhUitDatabase = HolidayMessageHistory.find.query().where()
                .ilike("request_date", date)
                .and()
                .ilike("request_by_id", hmh.getRequestByID())
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList().get(0);

        for (ExactDateHMH date1 : dates) {
            hmhUitDatabase.getExactDates().add(date1);
            date1.setHolidayMessage(hmhUitDatabase);
            date1.save();
        }
        hmhUitDatabase.save();
        return hmhUitDatabase.getId();
    }

    /**
     * Add a HolidayMessage to the database.
     *
     * First link all dates to the holidayMessage.
     * Then add a PlanningUnitState object for every PlanningUnitState the employee is part of.
     * Then save the HolidayMessage
     * Then get it back out of the database
     * Then save the dates and the PlanningUnitStates
     * This very not clean method has to be used because of the limitations of ebean.
     * Otherwise, it does not save everything properly.
     *
     * @return id of the newly saved hm
     */
    public long addHolidayMessage(String token, HolidayMessage hm) throws Exception{

        List<ExactDate> dates = hm.getExactDates();
        List<String> planningUnits = planningUnitHelper.getAllPlanningUnitIDsInWhichEmployeeIDIsIn(token, hm.getEmployeeID());

        for (ExactDate date : dates) {
            date.setHolidayMessage(hm);
        }

        List<PlanningUnitState> puslist = new ArrayList<>();

        for (String unit : planningUnits) {
            PlanningUnitState pus = new PlanningUnitState();
            pus.setState(SchedulingState.New);
            pus.setUnitId(unit);
            pus.setHolidayMessage(hm);
            puslist.add(pus);
        }

        hm.setPlanningUnitStates(puslist);

        for (PlanningUnitState pus : puslist) {
            pus.setHolidayMessage(hm);
        }

        hm.save();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = "";
        try {
            date = dateFormat.format(hm.getRequestDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        HolidayMessage hmUitDatabase = HolidayMessage.find.query().where()
                .ilike("request_date", date)
                .and()
                .ilike("request_by_id", hm.getRequestByID())
                .setFirstRow(0)
                .setMaxRows(2500)
                .findPagedList()
                .getList().get(0);

        for (PlanningUnitState pus : puslist) {
            hmUitDatabase.getPlanningUnitStates().add(pus);
            pus.setHolidayMessage(hmUitDatabase);
            pus.save();
        }

        for (ExactDate date1 : dates) {
            hmUitDatabase.getExactDates().add(date1);
            date1.setHolidayMessage(hmUitDatabase);
            date1.save();
        }
        hmUitDatabase.save();
        return hmUitDatabase.getId();
    }

    /**
     * Deletes the HolidayMessage from the database whose id matches
     * the passed id.
     *
     * @param id id of the HolidayMessage that is to be deleted.
     */
    public void deleteHolidayMessage(long id) {
        HolidayMessage hm = HolidayMessage.find.byId(id);
        if (hm != null) {
            deleteHolidayMessage(hm);
        }
    }


}
