package models;

public class Absence {
    private String id;
    private String dailyStartTime;
    private String dailyEndTime;
    private AuditInfo auditInfo;
    private String type;
    private Interval interval;

    public Absence() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDailyStartTime() {
        return dailyStartTime;
    }

    public void setDailyStartTime(String dailyStartTime) {
        this.dailyStartTime = dailyStartTime;
    }

    public String getDailyEndTime() {
        return dailyEndTime;
    }

    public void setDailyEndTime(String dailyEndTime) {
        this.dailyEndTime = dailyEndTime;
    }

    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfo auditInfo) {
        this.auditInfo = auditInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "Absence{" +
                "id='" + id + '\'' +
                ", dailyStartTime='" + dailyStartTime + '\'' +
                ", dailyEndTime='" + dailyEndTime + '\'' +
                ", auditInfo=" + auditInfo +
                ", type=" + type +
                ", interval=" + interval +
                '}';
    }
}
