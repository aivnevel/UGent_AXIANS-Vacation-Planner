package models;

public class Organization {
    private String name;
    private String id;
    private Address address;
    private String telephoneNumber;
    private OnCallLicense onCallLicense;
    private OnDayLicense onDayLicense;
    private String defaultLocale;
    private String organizationType;
    private String remarks;
    private boolean activated;

    public Organization() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public OnCallLicense getOnCallLicense() {
        return onCallLicense;
    }

    public void setOnCallLicense(OnCallLicense onCallLicense) {
        this.onCallLicense = onCallLicense;
    }

    public OnDayLicense getOnDayLicense() {
        return onDayLicense;
    }

    public void setOnDayLicense(OnDayLicense onDayLicense) {
        this.onDayLicense = onDayLicense;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    @Override
    public String toString() {
        return "Organization{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", address=" + address +
                ", telephoneNumber='" + telephoneNumber + '\'' +
                ", onCallLicense=" + onCallLicense +
                ", onDayLicense=" + onDayLicense +
                ", defaultLocale='" + defaultLocale + '\'' +
                ", organizationType='" + organizationType + '\'' +
                ", remarks='" + remarks + '\'' +
                ", activated=" + activated +
                '}';
    }
}
