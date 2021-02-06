package models.planning;


import com.fasterxml.jackson.annotation.JsonProperty;
import models.planning.days.*;

public class Demands {

    @JsonProperty("MONDAY")
    private MONDAY MONDAY;
    @JsonProperty("TUESDAY")
    private TUESDAY TUESDAY;
    @JsonProperty("WEDNESDAY")
    private WEDNESDAY WEDNESDAY;
    @JsonProperty("THURSDAY")
    private THURSDAY THURSDAY;
    @JsonProperty("FRIDAY")
    private FRIDAY FRIDAY;
    @JsonProperty("SATURDAY")
    private SATURDAY SATURDAY;
    @JsonProperty("SUNDAY")
    private SUNDAY SUNDAY;

    public models.planning.days.MONDAY getMONDAY() {
        return MONDAY;
    }

    public void setMONDAY(models.planning.days.MONDAY MONDAY) {
        this.MONDAY = MONDAY;
    }

    public models.planning.days.TUESDAY getTUESDAY() {
        return TUESDAY;
    }

    public void setTUESDAY(models.planning.days.TUESDAY TUESDAY) {
        this.TUESDAY = TUESDAY;
    }

    public models.planning.days.WEDNESDAY getWEDNESDAY() {
        return WEDNESDAY;
    }

    public void setWEDNESDAY(models.planning.days.WEDNESDAY WEDNESDAY) {
        this.WEDNESDAY = WEDNESDAY;
    }

    public models.planning.days.THURSDAY getTHURSDAY() {
        return THURSDAY;
    }

    public void setTHURSDAY(models.planning.days.THURSDAY THURSDAY) {
        this.THURSDAY = THURSDAY;
    }

    public models.planning.days.FRIDAY getFRIDAY() {
        return FRIDAY;
    }

    public void setFRIDAY(models.planning.days.FRIDAY FRIDAY) {
        this.FRIDAY = FRIDAY;
    }

    public models.planning.days.SATURDAY getSATURDAY() {
        return SATURDAY;
    }

    public void setSATURDAY(models.planning.days.SATURDAY SATURDAY) {
        this.SATURDAY = SATURDAY;
    }

    public models.planning.days.SUNDAY getSUNDAY() {
        return SUNDAY;
    }

    public void setSUNDAY(models.planning.days.SUNDAY SUNDAY) {
        this.SUNDAY = SUNDAY;
    }

    @Override
    public String toString() {
        return "Demands{" +
                "MONDAY=" + MONDAY +
                ", TUESDAY=" + TUESDAY +
                ", WEDNESDAY=" + WEDNESDAY +
                ", THURSDAY=" + THURSDAY +
                ", FRIDAY=" + FRIDAY +
                ", SATURDAY=" + SATURDAY +
                ", SUNDAY=" + SUNDAY +
                '}';
    }
}