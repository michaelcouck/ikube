package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * IndexableMessage's for indexing by the search engine.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Email extends Indexable {

    @Column(length = 64)
    @Attribute(field = false, description = "...")
    String firstname;
    @Column(length = 64)
    @Attribute(field = false, description = "...")
    String surname;
    @Column(length = 64)
    @Attribute(field = false, description = "...")
    String displayName;
    @Column(length = 64)
    @Attribute(field = false, description = "...")
    String emailAddress;

    public Email() {
    }

    public Email(String firstname, String surname, String displayName, String emailAddress) {
        if (emailAddress == null || !emailAddress.contains("@"))
            throw new IllegalArgumentException("Invalid Email.emailAddress [" + emailAddress + "], does not contain @ symbol.");
        this.firstname = firstname;
        this.surname = surname;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    public String toString() {
        return ((firstname == null) ? "" : firstname + " ") +
                ((surname == null) ? "" : surname + " ") +
                ((emailAddress == null) ? "<UNKNOWN>" : "<" + emailAddress + ">");
    }

}
