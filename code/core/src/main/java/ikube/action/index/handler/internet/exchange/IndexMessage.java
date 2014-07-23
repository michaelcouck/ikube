package ikube.action.index.handler.internet.exchange;

import java.util.Date;
import java.util.List;

/**
 * IndexMessage's that are ready to be indexed by the search engine.
 */
public class IndexMessage {
    public String from, subject, body, bodyType; // bodyType HTML, TEXT, BEST;
    public List<String> to, bcc, cc;
    public Date created, sent, received;

    public IndexMessage(
            String from, String subject, String body, String bodyType,
            List<String> to, List<String> bcc, List<String> cc,
            Date created, Date sent, Date received
    ){
        this.from     = from;
        this.subject  = subject;
        this.body     = body;
        this.bodyType = bodyType;
        this.to       = to;
        this.bcc      = bcc;
        this.cc       = cc;
        this.created  = created;
        this.sent     = sent;
        this.received = received;
    }
}
