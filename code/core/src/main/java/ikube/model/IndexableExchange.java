package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableExchange extends Indexable {

    @Column
    @NotNull
    @Pattern(regexp = "^(https?|http?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", message = "The url must be valid")
    @Attribute(field = false, description = "This is the endpoint url for the web service.")
    private String exchangeUrl;

    @Column
    @Size(min = 1, max = 256)
    @Attribute(field = false, description = "This is the userid to login to the server.")
    private String exchangeUserid;

    @Column
    @Size(min = 1, max = 256)
    @Attribute(field = false, description = "This is the password to login to the server.")
    private String exchangePassword;

    @Column
    @Attribute(field = false, description = "This is the date to start indexing all user email messages from " +
            "(message send date). Null indicates start from the beginning of time.")
    private Date   indexFromDate;

    @Column
    @Size(min = 4, max = 64)
    @Attribute(field = false, description = "Resume indexing messages from this user account email onward. Null " +
            "indicates start from the beginning.")
    private String resumeIndexFrom;

    @Column
    @Attribute(field = false, description = "Resume indexing messages from this user account email message onward. " +
            "The message is an unique identifier of last message processed successfully. Null indicates start from the beginning.")
    private String  resumeIndexFromMessage;

    /**
     * Type names and format to be indexed.
     */
    @Column
    @Attribute(field = false, description = "The mailbox owners email address.")
    private String messageMailboxOwnerField;
    @Column
    @Attribute(field = false, description = "The mailbox name for instance Inbox.")
    private String messageMailboxNameField;
    @Column
    @Attribute(field = false, description = "The message unique exchange identifier.")
    private String messageExchangeIdField;
    @Column
    @Attribute(field = false, description = "The message unique internet identifier.")
    private String messageInternetIdField;
    @Column
    @Attribute(field = false, description = "The message unique conversation identifier.")
    private String messageConversationIdField;
    @Column
    @Attribute(field = false, description = "The date and time the message was created.")
    private String messageCreatedDateField;
    @Column
    @Attribute(field = false, description = "The date and time the message was sent to the recipients, which includes the mailbox owner.")
    private String messageSentDateField;
    @Column
    @Attribute(field = false, description = "The date and time the message was received by the mailbox owner.")
    private String messageReceivedDateField;
    @Column
    @Attribute(field = false, description = "Whom the message is from.")
    private String messageFromField;
    @Column
    @Attribute(field = false, description = "The to recipients of the message.")
    private String messageToField;
    @Column
    @Attribute(field = false, description = "The cc recipients of the message.")
    private String messageBccField;
    @Column
    @Attribute(field = false, description = "The bcc recipients of the message.")
    private String messageCcField;
    @Column
    @Attribute(field = false, description = "The message body subject title.")
    private String messageSubjectField;
    @Column
    @Attribute(field = false, description = "The message body text.")
    private String messageBodyField;
    @Column
    @Attribute(field = false, description = "The message body text type - HTML, TEXT, BEST.")
    private String messageBodyTypeField;

    public String getExchangeUrl() {
        return exchangeUrl;
    }

    public void setExchangeUrl(String url) {
        this.exchangeUrl = url;
    }

    public String getExchangeUserid() {
        return exchangeUserid;
    }

    public void setExchangeUserid(String userid) {
        this.exchangeUserid = userid;
    }

    public String getExchangePassword() {
        return exchangePassword;
    }

    public void setExchangePassword(String password) {
        this.exchangePassword = password;
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

    public String getMessageExchangeIdField() {
        return messageExchangeIdField;
    }

    public void setMessageExchangeIdField(String messageMessageIdField) {
        this.messageExchangeIdField = messageMessageIdField;
    }

    public String getMessageInternetIdField() {
        return messageInternetIdField;
    }

    public void setMessageInternetIdField(String messageInternetIdField) {
        this.messageInternetIdField = messageInternetIdField;
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
