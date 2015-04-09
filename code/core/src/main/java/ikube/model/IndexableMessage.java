package ikube.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * IndexableMessage's for indexing by the search engine.
 */
@Entity()
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableMessage extends Indexable {

    // mailbox
    @OneToOne
    public Email mailboxOwner;
    public String mailboxName;

    // message fields
    public String messageExchangeId;
    public String messageInternetId;
    public String conversationId;

    public String subject;
    public String body;
    public String bodyType; // bodyType HTML, TEXT, BEST;
    public Date created;
    public Date sent;
    public Date received;

    @OneToOne
    public Email from;

    @OneToMany(mappedBy = "parent")
    public List<Email> to;
    @OneToMany(mappedBy = "parent")
    public List<Email> cc;
    @OneToMany(mappedBy = "parent")
    public List<Email> bcc;

    public String toString() {
        return "\nUser Mail Message : " +
                "\n  mailboxName:       " + mailboxName +
                "\n  mailboxOwner:      " + mailboxOwner +
                "\n  messageExchangeId: " + messageExchangeId +
                "\n  messageInternetId: " + messageInternetId +
                "\n  from:              " + from +
                "\n  to:                " + to +
                "\n  cc:                " + cc +
                "\n  bcc:               " + bcc +
                "\n  subject:           " + subject +
                "\n  sent:              " + sent +
                "\n  received:          " + received +
                "\n  body type:         " + bodyType +  // HTML, TEXT, BEST, NONE;
                "\n  body text:         " + body +
                "\n----------------------------------------------------------------";
    }
}
