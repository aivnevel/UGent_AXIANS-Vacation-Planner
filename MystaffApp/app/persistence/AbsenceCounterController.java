package persistence;

import businessLogic.AbsenceCounter;
import utilityClasses.Settings;

public class AbsenceCounterController {

    /**
     * Simple get function that returns the AbsenceCounter for a certain employee from the database.
     * @param id: id of the employee.
     */
    public AbsenceCounter getAbsenceCounterWithId(String id) {
        return AbsenceCounter.find.byId(id);
    }

    /**
     * Simple delete function that deletes the AbsenceCounter object with the given id out of the database.
     * @param id: id of the employee.
     */
    public void deleteAbsenceCounterFromEmployeeWithId(String id){
        AbsenceCounter.find.byId(id).delete();
    }

    /**
     * Simple post function that saves the given AbsenceCounter object to the database.
     * Saves if the object was not in the database and posts if it was.
     */
    public void postAbsenceCounter(AbsenceCounter ac){
        if(AbsenceCounter.find.byId(ac.getEmployeeID()) == null){
            ac.save();
        } else {
            ac.update();
        }
    }
}
