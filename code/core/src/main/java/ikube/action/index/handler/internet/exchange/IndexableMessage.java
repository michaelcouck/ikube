package ikube.action.index.handler.internet.exchange;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * IndexableMessage's for indexing by the search engine.
 */
public class IndexableMessage {

    public Email  mailboxOwner;
    public String mailboxName;
    public String messageId;
    public String internetMessageId;
    public String conversationId;
    public Email from;
    public List<Email> to, bcc, cc;
    public String subject, body, bodyType; // bodyType HTML, TEXT, BEST;
    public Date created, sent, received;

    public static class Email{
        final String firstname, surname, displayName, emailAddress;

        public Email(String firstname, String surname, String displayName, String emailAddress){
        	if(emailAddress == null || !emailAddress.contains("@"))
        		throw new IllegalArgumentException("Invalid Email.emailAddress ["+emailAddress+"], does not contain @ symbol.");
        	this.firstname = firstname;
        	this.surname = surname;
            this.displayName = displayName;
            this.emailAddress = emailAddress;
        }

        public String toString(){ return ((firstname==null) ? "" : firstname + " ") + 
        								 ((surname==null) ? "" : surname + " ") + 
        								 ((emailAddress==null) ? "<UNKNOWN>" : "<" + emailAddress + ">"); }
    }

    public IndexableMessage(){}

    public String toString(){
        return "\nUser Mail Message : " +
        	"\n  mailboxName:       " + mailboxName +
        	"\n  mailboxOwner:      " + mailboxOwner.toString() +
        	"\n  messageId:         " + messageId +
        	"\n  internetMessageId: " + internetMessageId +
        	"\n  from:              " + from.toString() +
            "\n  to:                " + to.toString() +
            "\n  cc:                " + cc.toString() +
            "\n  bcc:               " + bcc.toString() +
            "\n  subject:           " + subject +
            "\n  sent:              " + sent +
            "\n  received:          " + received +
            "\n  body type:         " + bodyType +  // HTML, TEXT, BEST, NONE;
            "\n  body text:         " + body +  
            "\n----------------------------------------------------------------";
    }
}
