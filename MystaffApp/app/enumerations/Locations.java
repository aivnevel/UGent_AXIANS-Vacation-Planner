package enumerations;

import controllers.AxiansController;
import interfaces.Employee;
import models.planning.Location;
import models.planning.Planning;
import models.planning.PlanningMember;
import models.planning.Skill;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/*
 * Ideally would be an enumeration of all locations possible. This can't be done because axians gives ID's to every
 * location. This way, when two different planning units have the same locations, they can be distinguished of each
 * other with the ID. This is why a map is used with the ID as a key and the human readable name as a value.
 * Ideally, this map should be loaded only once, when starting up.
 * Since Axians could make new planning units, and that's saved on their server, we can't cache anything. When this map
 * is needed, it should be reloaded.
 * This class is a singleton.
 */

public class Locations {
    private static Locations instance = new Locations();

    public static Locations getInstance(){
        return instance;
    }

    private Locations(){
        locations = new HashMap<String, String>(); // <ID, Name>
    }

    public Map<String, String> locations;

    public void reload(AxiansController ac, String token) throws Exception{
        Planning[] plannings = ac.getOnCallPlanningsArray(token);
        reload(plannings);
    }

    public void reload(Planning[] plannings){
        locations = new HashMap<String, String>(); // <ID, Name>
        for (Planning planning : plannings) {
            for (Location location : planning.getLocations()) {
                locations.put(location.getId(), location.getName());
            }
        }
    }
}
