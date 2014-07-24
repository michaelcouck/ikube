package ikube.action.index.handler.internet.exchange;

import java.util.Date;
import java.util.List;

/**
 * IndexableMessage's for indexing by the search engine.
 */
public class IndexableMessage {
    public Email from;
    public List<Email> to, bcc, cc;
    public String subject, body, bodyType; // bodyType HTML, TEXT, BEST;
    public Date created, sent, received;

    public static class Email{
        final String name, displayName, emailAddress;

        public Email(String name, String displayName, String emailAddress){
            this.name = name;
            this.displayName = displayName;
            this.emailAddress = emailAddress;
        }
    }

    public IndexableMessage(
            Email from,
            List<Email> to, List<Email> bcc, List<Email> cc,
            String subject, String body, String bodyType,
            Date created, Date sent, Date received
    ){
        this.from     = from;
        this.to       = to;
        this.bcc      = bcc;
        this.cc       = cc;
        this.subject  = subject;
        this.body     = body;
        this.bodyType = bodyType;
        this.created  = created;
        this.sent     = sent;
        this.received = received;
    }
}
