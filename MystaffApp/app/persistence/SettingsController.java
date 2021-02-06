package persistence;

import play.mvc.Controller;
import utilityClasses.Settings;

public class  SettingsController extends Controller {

    /**
     * Simple post function that saves the given Settings object to the database.
     * Saves if the object was not in the database and posts if it was.
     */
    public String addSettingsFromEmployeeWithId(Settings settings) {
        Settings settUitDb = Settings.find.byId(settings.employeeID);
        if(settUitDb == null){
            settings.save();
            settUitDb = Settings.find.byId(settings.employeeID);
        } else {
            settings.update();
        }
        return settUitDb.employeeID;
    }

    /**
     * Simple delete function that deletes the Settings object with the given id out of the database.
     * @param id: id of the employee.
     */
    public void deleteSettingsFromEmployeeWithId(String id){
        Settings.find.byId(id).delete();
    }

    /**
     * Simple get function that returns the Settings for a certain employee from the database.
     * @param id: id of the employee.
     */
    public Settings getSettingsFromEmployeeWithId(String id){
        return Settings.find.byId(id);
    }
}
