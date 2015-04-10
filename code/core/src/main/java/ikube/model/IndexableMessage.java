package ikube.model;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.List;

/**
 * IndexableMessage's for indexing by the search engine.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableMessage extends Indexable {

    @Min(value = 1)
    @Max(value = 64)
    @Attribute(field = false, description = "...")

    @PrimaryKeyJoinColumn
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Email mailboxOwner;

    @PrimaryKeyJoinColumn
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Email from;

    @Column
    @Min(value = 1)
    @Max(value = 64)
    @Attribute(field = false, description = "...")
    public String mailboxName;

    @Column
    @Min(value = 1)
    @Max(value = 64)
    @Attribute(field = false, description = "...")
    public String messageExchangeId;

    @Column
    @Min(value = 1)
    @Max(value = 64)
    @Attribute(field = false, description = "...")
    public String messageInternetId;

    @Column
    @Min(value = 1)
    @Max(value = 64)
    @Attribute(field = false, description = "...")
    public String conversationId;

    @Column
    @Min(value = 1)
    @Max(value = 64)
    @Attribute(field = false, description = "...")
    public String subject;

    @Lob
    @Column
    @Attribute(field = false, description = "...")
    public String body;

    @Column
    @Min(value = 1)
    @Max(value = 64)
    @Attribute(field = false, description = "...")
    public String bodyType; // bodyType HTML, TEXT, BEST;

    @Column
    @Attribute(field = false, description = "...")
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date created;

    @Column
    @Attribute(field = false, description = "...")
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date sent;

    @Column
    @Attribute(field = false, description = "...")
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date received;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    public List<Email> to;
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    public List<Email> cc;
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    public List<Email> bcc;

    public Email getMailboxOwner() {
        return mailboxOwner;
    }

    public void setMailboxOwner(final Email mailboxOwner) {
        this.mailboxOwner = mailboxOwner;
    }

    public Email getFrom() {
        return from;
    }

    public void setFrom(final Email from) {
        this.from = from;
    }

    public String getMailboxName() {
        return mailboxName;
    }

    public void setMailboxName(final String mailboxName) {
        this.mailboxName = mailboxName;
    }

    public String getMessageExchangeId() {
        return messageExchangeId;
    }

    public void setMessageExchangeId(final String messageExchangeId) {
        this.messageExchangeId = messageExchangeId;
    }

    public String getMessageInternetId() {
        return messageInternetId;
    }

    public void setMessageInternetId(final String messageInternetId) {
        this.messageInternetId = messageInternetId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(final String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(final String bodyType) {
        this.bodyType = bodyType;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(final Date sent) {
        this.sent = sent;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(final Date received) {
        this.received = received;
    }

    public List<Email> getTo() {
        return to;
    }

    public void setTo(final List<Email> to) {
        this.to = to;
    }

    public List<Email> getCc() {
        return cc;
    }

    public void setCc(final List<Email> cc) {
        this.cc = cc;
    }

    public List<Email> getBcc() {
        return bcc;
    }

    public void setBcc(final List<Email> bcc) {
        this.bcc = bcc;
    }

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
