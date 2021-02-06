package models;

public class Holiday {
    private String tenant;
    private String id;
    private String desc;
    private String date;
    private String seenAs;
    private int year;
    private boolean holiday;

    public Holiday() {}

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSeenAs() {
        return seenAs;
    }

    public void setSeenAs(String seenAs) {
        this.seenAs = seenAs;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isHoliday() {
        return holiday;
    }

    public void setHoliday(boolean holiday) {
        this.holiday = holiday;
    }

    @Override
    public String toString() {
        return "Holiday{" +
                "tenant='" + tenant + '\'' +
                ", id='" + id + '\'' +
                ", desc='" + desc + '\'' +
                ", date='" + date + '\'' +
                ", seenAs='" + seenAs + '\'' +
                ", year=" + year +
                ", holiday=" + holiday +
                '}';
    }
}
