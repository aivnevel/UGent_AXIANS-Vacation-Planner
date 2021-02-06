package models;

import java.util.Arrays;
import java.util.Map;

public class Member {

    private String id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private boolean activated;
    private boolean organizationAdmin;
    private boolean inUse;
    private Map<String, Integer> absenceRights;
    private Absence[] absences;

    public Member() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isOrganizationAdmin() {
        return organizationAdmin;
    }

    public void setOrganizationAdmin(boolean organizationAdmin) {
        this.organizationAdmin = organizationAdmin;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public Map<String, Integer> getAbsenceRights() {
        return absenceRights;
    }

    public void setAbsenceRights(Map<String, Integer> absenceRights) {
        this.absenceRights = absenceRights;
    }

    public Absence[] getAbsences() {
        return absences;
    }

    public void setAbsences(Absence[] absences) {
        this.absences = absences;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", activated=" + activated +
                ", organizationAdmin=" + organizationAdmin +
                ", inUse=" + inUse +
                ", absenceRights=" + absenceRights +
                ", absences=" + Arrays.toString(absences) +
                '}';
    }
}

