package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * IndexableMessage's for indexing by the search engine.
 */
@Entity()
@Inheritance(strategy = InheritanceType.JOINED)
public class Email extends Indexable {

    String firstname;
    String surname;
    String displayName;
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

    public String toString() {
        return ((firstname == null) ? "" : firstname + " ") +
                ((surname == null) ? "" : surname + " ") +
                ((emailAddress == null) ? "<UNKNOWN>" : "<" + emailAddress + ">");
    }

}
