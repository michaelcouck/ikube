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

    /**
     * Type names and format to be indexed.
     */
    @Column
    @Attribute(field = false, description = "?")
    private String messageMailboxOwnerField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageMailboxNameField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageMessageIdField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageConversationIdField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageCreatedDateField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageSentDateField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageReceivedDateField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageFromField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageToField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageBccField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageCcField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageSubjectField;
    @Column
    @Attribute(field = false, description = "?")
    private String messageBodyField;
    @Column
    @Attribute(field = false, description = "bodyType HTML, TEXT, BEST.")
    private String messageBodyTypeField;



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


    public String getMessageMailboxOwnerField() {
        return messageMailboxOwnerField;
    }

    public void setMessageMailboxOwnerField(String messageMailboxOwnerField) {
        this.messageMailboxOwnerField = messageMailboxOwnerField;
    }

    public String getMessageMailboxNameField() {
        return messageMailboxNameField;
    }

    public void setMessageMailboxNameField(String messageMailboxNameField) {
        this.messageMailboxNameField = messageMailboxNameField;
    }

    public String getMessageMessageIdField() {
        return messageMessageIdField;
    }

    public void setMessageMessageIdField(String messageMessageIdField) {
        this.messageMessageIdField = messageMessageIdField;
    }

    public String getMessageConversationIdField() {
        return messageConversationIdField;
    }

    public void setMessageConversationIdField(String messageConversationIdField) {
        this.messageConversationIdField = messageConversationIdField;
    }

    public String getMessageCreatedDateField() {
        return messageCreatedDateField;
    }

    public void setMessageCreatedDateField(String messageCreatedDateField) {
        this.messageCreatedDateField = messageCreatedDateField;
    }

    public String getMessageSentDateField() {
        return messageSentDateField;
    }

    public void setMessageSentDateField(String messageSentDateField) {
        this.messageSentDateField = messageSentDateField;
    }

    public String getMessageReceivedDateField() {
        return messageReceivedDateField;
    }

    public void setMessageReceivedDateField(String messageReceivedDateField) {
        this.messageReceivedDateField = messageReceivedDateField;
    }

    public String getMessageFromField() {
        return messageFromField;
    }

    public void setMessageFromField(String messageFromField) {
        this.messageFromField = messageFromField;
    }

    public String getMessageToField() {
        return messageToField;
    }

    public void setMessageToField(String messageToField) {
        this.messageToField = messageToField;
    }

    public String getMessageBccField() {
        return messageBccField;
    }

    public void setMessageBccField(String messageBccField) {
        this.messageBccField = messageBccField;
    }

    public String getMessageCcField() {
        return messageCcField;
    }

    public void setMessageCcField(String messageCcField) {
        this.messageCcField = messageCcField;
    }

    public String getMessageSubjectField() {
        return messageSubjectField;
    }

    public void setMessageSubjectField(String messageSubjectField) {
        this.messageSubjectField = messageSubjectField;
    }

    public String getMessageBodyField() {
        return messageBodyField;
    }

    public void setMessageBodyField(String messageBodyField) {
        this.messageBodyField = messageBodyField;
    }

    public String getMessageBodyTypeField() {
        return messageBodyTypeField;
    }

    public void setMessageBodyTypeField(String messageBodyTypeField) {
        this.messageBodyTypeField = messageBodyTypeField;
    }
}
