package models;

public class ProfileSettings {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String preferredLocale;
    private boolean oncallIcsUrl;

    public ProfileSettings() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPreferredLocale() {
        return preferredLocale;
    }

    public void setPreferredLocale(String preferredLocale) {
        this.preferredLocale = preferredLocale;
    }

    public boolean isOncallIcsUrl() {
        return oncallIcsUrl;
    }

    public void setOncallIcsUrl(boolean oncallIcsUrl) {
        this.oncallIcsUrl = oncallIcsUrl;
    }

    @Override
    public String toString() {
        return "ProfileSettings{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", preferredLocale='" + preferredLocale + '\'' +
                ", oncallIcsUrl=" + oncallIcsUrl +
                '}';
    }
}

