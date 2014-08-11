package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import ikube.action.index.handler.internet.exchange.IndexableMessage;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
@Entity
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableExchange extends Indexable {

    private transient String subject;
    private transient String body;
    private transient String sender;
    private transient String recipient;

    @Column
    @NotNull
    @Pattern(regexp = "^(https?|http?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", message = "The url must be valid")
    @Attribute(field = false, description = "This is the endpoint url for the web service")
    private String url;

    @Column
    @Size(min = 1, max = 256)
    @Attribute(field = false, description = "This is the userid to login to the server")
    private String userid;

    @Column
    @Size(min = 1, max = 256)
    @Attribute(field = false, description = "This is the password to login to the server")
    private String password;

    private IndexableMessage indexableMessage;

    @Column
    @Size(min = 4, max = 60)
    @Attribute(field = false, description = "Resume indexing messages from this user account email onward. Null indicates start from the beginning.")
    private String resumeIndexFrom;

    @Column
    @Attribute(field = false, description = "Resume indexing messages from this user account email message onward. " +
            "The message is an unique identifier of last message processed successfully. Null indicates start from the beginning.")
    private String  resumeIndexFromMessage;

    @Column
    @Attribute(field = false, description = "This is the date to start indexing all user email messages from (message send date). Null indicates start from the beginning of time.")
    private Date   indexFromDate;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public IndexableMessage getIndexableMessage() {
        return indexableMessage;
    }

    public void setIndexableMessage(IndexableMessage indexableMessage) {
        this.indexableMessage = indexableMessage;
    }

    public Date   getIndexFromDate(){ return indexFromDate; }

    public String getResumeIndexFrom(){ return resumeIndexFrom; }

    public String getResumeIndexFromMessage(){ return resumeIndexFromMessage; }

    public void setIndexFromDate(Date indexFromDate){ this.indexFromDate = indexFromDate; }

    public void setResumeIndexFrom(String emailAddress){ this.resumeIndexFrom = emailAddress; }

    public void setResumeIndexFromMessage(String emailMessageId){ this.resumeIndexFromMessage = emailMessageId; }
}
