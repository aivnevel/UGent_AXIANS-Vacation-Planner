package enumerations;

import controllers.AxiansController;
import models.planning.Planning;

import java.util.HashMap;
import java.util.Map;

/*
 * Ideally would be an enumeration of all planning units possible. This can't be done because axians gives ID's to every
 * location. This way, when two different planning units have the same name, they can be distinguished of each
 * other with the ID. This is why a map is used with the ID as a key and the human readable name as a value.
 * Ideally, this map should be loaded only once, when starting up.
 * Since Axians could make new planning units, and that's saved on their server, we can't cache anything. When this map
 * is needed, it should be reloaded.
 * This class is a singleton.
 */

public class PlanningUnits {

    private static PlanningUnits instance;

    public static PlanningUnits getInstance(){
        if (instance == null) {
            instance = new PlanningUnits();
        }
        return instance;
    }

    private PlanningUnits(){
        planningUnits = new HashMap<String, String>(); // <ID, Naam>
    }

    public Map<String, String> planningUnits;

    public void reload(AxiansController ac, String token) throws Exception {
        Planning[] plannings = ac.getOnCallPlanningsArray(token);
        reload(plannings);
    }

    public void reload(Planning[] plannings){
        for (Planning planning : plannings) {
            planningUnits.put(planning.getId(), planning.getName());
        }
    }
}