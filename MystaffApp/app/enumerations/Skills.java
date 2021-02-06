package enumerations;

import controllers.AxiansController;
import models.planning.Planning;
import models.planning.Skill;

import java.util.HashMap;
import java.util.Map;

/*
 * Ideally would be an enumeration of all skills possible. This can't be done because axians gives ID's to every
 * location. This way, when two different planning units have the same skills, they can be distinguished of each
 * other with the ID. This is why a map is used with the ID as a key and the human readable name as a value.
 * Ideally, this map should be loaded only once, when starting up.
 * Since Axians could make new planning units, and that's saved on their server, we can't cache anything. When this map
 * is needed, it should be reloaded.
 * This class is a singleton.
 */

public class Skills {

    private static Skills instance = new Skills();

    public static Skills getInstance(){
        return instance;
    }

    private Skills(){
        skills = new HashMap<String, String>(); // <ID, Naam>
    }

    public Map<String, String> skills;

    public void reload(AxiansController ac, String token) throws Exception{
        Planning[] plannings = ac.getOnCallPlanningsArray(token);
        reload(plannings);
    }

    public void reload(Planning[] plannings){
        skills = new HashMap<String, String>();
        for (Planning planning : plannings) {
            for (Skill skill : planning.getSkills()) {
                skills.put(skill.getId(), skill.getName());
            }
        }
    }
}
