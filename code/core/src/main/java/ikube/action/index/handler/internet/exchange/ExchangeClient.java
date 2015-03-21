package ikube.action.index.handler.internet.exchange;

import com.independentsoft.exchange.*;
import ikube.model.Email;
import ikube.model.IndexableMessage;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;
import java.util.logging.Logger;
/**
 * Search Exchange user accounts for mail in a specified FOLDER between specified date range.
 * Folder can be Inbox, Draft, Private, Calendar etc.
 *
 * Created by turleyd on 16/07/2014.
 * 
 * TODO:
 * 1)   class UserMessages
 * 1.1) Optimize message indexing - do not re-index message body sub-messages that have already been indexed. 
 * 1.2) Optimize retrieval of messages from Exchange - find optimal size for USER_MESSAGES_PAGING_SIZE. 
 * 1.3) Optimize retrieval of messages from Exchange and memory usage in function getMessages() 
 *      a) looping a lighter user message list, not List<Item> items = findResponse.getItems();
 *      b) OR BETTER, get List<Item> items = findResponse.getItems() to return all message details, so not have to use 
 *         service.getMessage( message.getItemId(), Search.FIELDS.MESSAGE_READ ) - currently used to avoid null email body.
 * 2) 
 */
public class ExchangeClient {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    private static final int USER_MESSAGES_PAGING_SIZE = 100;  // 100;
    private static final StandardFolder FOLDER = StandardFolder.INBOX;
    
    public enum Search {
    	FIELDS;
    	
    	public final List<PropertyOrder> ORDER_USERS_BY     = new ArrayList<PropertyOrder>();
    	public final List<PropertyOrder> ORDER_MESSAGES_BY  = new ArrayList<PropertyOrder>();
    	public final List<PropertyPath>  MESSAGE_READ       = new ArrayList<PropertyPath>();
    	public final List<PropertyPath>  MESSAGE_READ_ITEM  = new ArrayList<PropertyPath>();

    	private Search(){    
    		ORDER_USERS_BY.add( new PropertyOrder(ContactPropertyPath.SURNAME, SortDirection.ASCENDING) );
    		ORDER_USERS_BY.add( new PropertyOrder(ContactPropertyPath.GIVEN_NAME, SortDirection.ASCENDING) );
            
    		ORDER_MESSAGES_BY.add( new PropertyOrder(MessagePropertyPath.SENT_TIME));
    		ORDER_MESSAGES_BY.add( new PropertyOrder(ItemPropertyPath.ITEM_ID));

            MESSAGE_READ_ITEM.add(MessagePropertyPath.ITEM_ID);
            MESSAGE_READ_ITEM.add(MessagePropertyPath.SENT_TIME);
            MESSAGE_READ_ITEM.add(MessagePropertyPath.START_DATE);
        
            MESSAGE_READ.add(MessagePropertyPath.INTERNET_MESSAGE_ID);
            MESSAGE_READ.add(MessagePropertyPath.SUBJECT);
            MESSAGE_READ.add(ItemPropertyPath.CONVERSATION_ID);
            MESSAGE_READ.add(ItemPropertyPath.CREATED_TIME);
            MESSAGE_READ.add(MessagePropertyPath.SENT_TIME);
            MESSAGE_READ.add(MessagePropertyPath.RECEIVED_TIME);
            MESSAGE_READ.add(MessagePropertyPath.MESSAGE_FLAGS); // like READ, UNMODIFIED
            MESSAGE_READ.add(MessagePropertyPath.TEXT_BODY);
            MESSAGE_READ.add(MapiPropertyTag.PR_TRANSPORT_MESSAGE_HEADERS);
            MESSAGE_READ.add(MessagePropertyPath.FROM);
            MESSAGE_READ.add(MessagePropertyPath.TO_RECIPIENTS);
            MESSAGE_READ.add(MessagePropertyPath.CC_RECIPIENTS);
            MESSAGE_READ.add(MessagePropertyPath.BCC_RECIPIENTS);
            //MESSAGE_READ.add(MapiPropertyTag.PR_MAIL_PERMISSION); 
    	}
    }
    
    private String 	exchangeUrl, exchangeDomain, exchangeUsername, exchangePassword;
    private String 	proxyDomain, proxyHost, proxyUsername, proxyPassword;
    private int 	proxyPort;
    private Service service;

    /**
     * Connect to an Exchange server (2007, 2010, 2013, Office365 Online).
     * @param exchangeUrl       required "https://outlook.office365.com/ews/exchange.asmx"
     * @param exchangeDomain    optional. The MS Exchange domain name.
     * @param exchangeUsername  required "exchange-username"
     * @param exchangePassword  required "exchange-password"
     */
    public ExchangeClient( String exchangeUrl, String exchangeDomain, String exchangeUsername, String exchangePassword){
    	this( exchangeUrl, exchangeDomain, exchangeUsername, exchangePassword, null, 0, null, null, null );
    }

    /**
     * Connect to an Exchange server through a local HTTP proxy.
     * 
     * new ExchangeClient("https://outlook.office365.com/ews/exchange.asmx",
     *                    null,                    // the MS Exchange domain name
     *                    "emailUsername@CompanyX.onmicrosoft.com",
     *                    "emailPassword",
     *                    "proxy.companyX.net",    // proxy host like "proxy.ibm.net"
     *                    8080,
     *                    "myLocalProxyDomain",    // proxy domain like "POST" for bpost.be
     *                    "myLocalProxyUsername",  // proxy username like "johnsmith"
     *                    "myLocalProxyPassword"); // proxy password like "12345678"
     *                    
     * Example: 
     * @param exchangeUrl       required "https://outlook.office365.com/ews/exchange.asmx"
     * @param exchangeDomain    optional. This is the MS Exchange domain name.
     * @param exchangeUsername  required "admin-username"
     * @param exchangePassword  required "admin-password"
     * @param proxyHost         required "proxy.ibm.net or 10.59.92.01"
     * @param proxyPort         required 8080
     * @param proxyDomain       optional. For Windows/NT domains use the domain name of the proxy, like "POST" for bpost.be
     * @param proxyUsername     optional "system-username" where proxy does not require a username.
     * @param proxyPassword     optional "system-password" where proxy does not require a password.
     */
    public ExchangeClient( String exchangeUrl, String exchangeDomain, String exchangeUsername, String exchangePassword,
    					   String proxyHost, int proxyPort, String proxyDomain, String proxyUsername, String proxyPassword){
        this.exchangeUrl      = exchangeUrl;
        this.exchangeDomain   = exchangeDomain;
        this.exchangeUsername = exchangeUsername;
        this.exchangePassword = exchangePassword;
        this.proxyHost        = proxyHost;
        this.proxyPort        = proxyPort;
        this.proxyDomain      = proxyDomain;
        this.proxyUsername    = proxyUsername;
        this.proxyPassword    = proxyPassword;

        getService();
    }

    protected Service getService() {
        if(service == null) {
            service = new Service(exchangeUrl, exchangeUsername, exchangePassword);
            setRequestVersion(RequestServerVersion.EXCHANGE_2013);
            setProxy();
            connect();
        }
        return service;
    }

    /** versioning information that identifies the schema version to target for a request */
    public void setRequestVersion(RequestServerVersion version){
        if(service != null)
            service.setRequestServerVersion( version );
    }

    private void setProxy() {
        if(proxyHost != null)
            service.setProxy( new HttpHost( proxyHost, proxyPort ) );
        if(proxyUsername != null){  // password can be optional for some proxy's
        	
        	CredentialsProvider credentials = new BasicCredentialsProvider();
            
        	// Windows Proxy Authentication
            credentials.setCredentials( AuthScope.ANY, new NTCredentials(proxyUsername, proxyPassword, null, proxyDomain) );
            service.setProxyCredentials( credentials.getCredentials(AuthScope.ANY) );
         
            // Non-Windows Proxy Authentication
            /*
            //AuthScope proxyScope = new AuthScope(proxyHost, proxyPort);
            //credentials.setCredentials(proxyScope, new UsernamePasswordCredentials( proxyDomain + "/" + proxyUsername, proxyPassword ));
            //service.setProxyCredentials( credentials.getCredentials(proxyScope) );
            service.setProxyCredentials(new UsernamePasswordCredentials( proxyDomain + "/" + proxyUsername, proxyPassword ));
            */
        }
    }

    /**
     * Verify a connection can be established with the Exchange server
     */
    private void connect(){
        try {
            // we call service.getRules() just to verify a connection can be established with the Exchange server
			GetRulesResponse r = service.getRules();
			List<Rule> rules = r.getRules();
		} catch (ServiceException e) {
        	if("Proxy Authentication Required".equals(e.getMessage())){
                throw new IllegalArgumentException(
                		"ExchangeClient cannot access the Exchange server because local internet proxy authentication is required."
                        + "Verify new ExchangeClient() constructor has specified the proxy [host, port, domain, username and password]."
                        + "\nIf the underlying ExchangeClient org.apache.http.client.protocol.RequestProxyAuthentication process "
                        + "throw an exception message "
                        + "'Proxy authentication error: Credentials cannot be used for NTLM authentication: org.apache.http.auth.UsernamePasswordCredentials' "
                        + "then the ExchangeClient.setProxy() should replace credentials provider "
                        + "org.apache.http.auth.UsernamePasswordCredentials with org.apache.http.auth.NTCredentials. "
                        + "\nNTCredentials NTLM authentication is required to connect to the local proxy over a Win NT+ network. "
                        + "NTCredentials allows you to specifty the proxy domain. UsernamePasswordCredentials does not."
                        , e);
        	}else
        		throw new IllegalArgumentException(
                        "ExchangeClient cannot access the Exchange server: " + exchangeUrl + ". " + e.getMessage(), e);
		}
    }
    
    /**
     * Get Exchange user accounts that have a mailbox. Users are retrieved from the unified persona store.
     * Multiple contact items can represent a single individual persona.
     * Exchange uses a persona to bring an individuals multiple contact together as a unified contact store.
     * Contact sources for an individual persona can be one or more of the following contact items
     * <ul>
     *   <li>Outlook (DisplayName, EmailAddress...)</li>
     *   <li>RecipientCache (DisplayName, EmailAddress...)</li>
     *   <li>Lync (Outlook Quick Contacts - DisplayName, EmailAddress...)</li>
     *   <li>Global Address List (DisplayName, EmailAddress...)</li>
     *   <li>Contacts folder,</li>
     *   <li>a company's directory service (Active Directory or Azus AD),</li>
     *   <li>Third-party sources (DisplayName, EmailAddress...)</li>
     *   <ul><li>a Hotmail account,</li>
     *       <li>a LinkedIn account,</li></ul>
     * </ul>
     *
     * Contacts are items in Exchange that store information about an individual, group, or organization.
     * Contacts can include names and email addresses, and other information, including IM addresses,
     * physical addresses, birthdays, family information, and a photo or image that represents the contact.
     * Contact information is stored in one of two places:
     * <ul>
     *     <li>Active Directory Domain Services (AD DS), if the contact is within the organization.</li>
     *     <li>The Contacts folder or another folder in a user's mailbox, if the contact is outside the organization.</li>
     * </ul>
     * @return one or more Exchange user accounts
     */
    protected List<UserAccount> getUserAccounts() {
        List<UserAccount> accounts = new ArrayList<UserAccount>();

        try{
            FindPeopleResponse response = getService().findPeople(
                    StandardFolder.DIRECTORY,
                    new PersonaShape(ShapeType.DEFAULT),
                    Search.FIELDS.ORDER_USERS_BY,
                    "SMTP");

            for(Persona persona : response.getPersonas()) {
                if (persona.getEmailAddress() != null){

                    accounts.add(
                        new UserAccount(
                            persona.getPersonaId().getId(),
                            persona.getPersonaType(),
                            persona.getDisplayName(),
                            persona.getGivenName(),
                            persona.getSurname(),
                            persona.getEmailAddress().getEmailAddress(),
                            persona.getLocation(),
                            persona.getCompanyName()));
		                    //persona.getBusinessAddresses());
		                    //persona.getBusinessPhoneNumbers());
		                    //persona.getBirthdays());
		                    //persona.getManagers());
                }
            }
        }catch (ServiceException e){
        	
        	if("Proxy Authentication Required".equals(e.getMessage())){
                throw new IllegalArgumentException("getUserAccounts request validation failed. " +
                        "ExchangeClient cannot access internet because local proxy authentication is required."
                        + "Verify new ExchangeClient() constructor has specified the proxy [host, port, domain, username and password]."
                        + "If the ExchangeClient client is running over a Win NT+ network, make sure the ExchangeClient code "
                        + "is using org.apache.http.auth NTCredentials instead of UsernamePasswordCredentials. "
                        + "NTCredentials allows you to specifty the proxy domain. UsernamePasswordCredentials does not.");
        	}
        	
            if("The request failed schema validation.".equals(e.getMessage()))
                throw new IllegalArgumentException("getUserAccounts request validation failed. " +
                        "Verify the Exchange user " + service.getUsername() + " is granted " +
                        "permission in Exchange server to view StandardFolder.DIRECTORY.");
            System.out.println(e.getMessage());
            System.out.println(e.getXmlMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    /**
     *  Get email messages from the mailbox of a user account. 
     *  
     * @param userAccount
     * @param beforeTo
     * @param afterFrom
     * @return
     */
    protected UserMessages getUserMessages(UserAccount userAccount, Date afterFrom, Date beforeTo){
        return new ExchangeClient.UserMessages(userAccount, afterFrom, beforeTo);
    }

    /*
    protected List<Appointment> getAppointments(String email, Date from ){
        List<Appointment> appointments = new ArrayList<Appointment>();

        try{
            Mailbox account = new Mailbox( email ); // "John@mydomain.com"

            StandardFolderId calendar = new StandardFolderId(StandardFolder.CALENDAR, account);

            FindItemResponse response = service.findItem(calendar, AppointmentPropertyPath.getAllPropertyPaths());

            for (int i = 0; i < response.getItems().size(); i++){
                if (response.getItems().get(i) instanceof Appointment){
                    Appointment appointment = (Appointment) response.getItems().get(i);
                    appointments.add( appointment );

                    System.out.println("Subject = " + appointment.getSubject());
                    System.out.println("StartTime = " + appointment.getStartTime());
                    System.out.println("EndTime = " + appointment.getEndTime());
                    System.out.println("Body Preview = " + appointment.getBodyPlainText());
                    System.out.println("----------------------------------------------------------------");
                }
            }
        }catch (ServiceException e){
            System.out.println(e.getMessage());
            System.out.println(e.getXmlMessage());
            e.printStackTrace();
        }
        return appointments;
    }*/

    private static class ProxyAuth extends Authenticator {
        private PasswordAuthentication auth;

        private ProxyAuth(String user, String password) {
            auth = new PasswordAuthentication(user, password == null ? new char[]{} : password.toCharArray());
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return auth;
        }
    }

    protected class UserAccount implements Comparator<UserAccount> {
        public String id, type, displayname, firstname, surname, email, location, companyName;

        public UserAccount( String id, String type, String displayname, String firstname, String surname, String email,
                            String location, String companyName ){
            this.id    = id;
            this.type  = type;
            this.displayname  = displayname;
            this.firstname = firstname;
            this.surname = surname;
            this.email = email;
            this.location = //persona.getLocation());
            this.companyName = companyName;
        }

        /** order useraccounts by displayname Collections.sort(<UserAccount>, this); */
        public int compare(UserAccount o1, UserAccount o2) {
            return o1.displayname.compareTo(o2.displayname);
        }
    }

    
    /**
     * Used to filter the user messages that are indexed.
     * hasNext() and getNext() iterate a users email messages in from, to date order.
     */
    protected class UserMessages {
        private final Logger log = Logger.getLogger(this.getClass().getName());

        private UserAccount useraccount;
        private Date beforeTo, afterFrom;

        private int findOffset = 0;
        private FindItemResponse findResponse;
        private Iterator<Item> messageIterator;

        /**
         * Get all user FOLDER messages between a specified from to date times.
         *
         * Constructor for UserMessages.
         * @param useraccount
         * @param afterFrom
         * @param beforeTo
         */
        protected UserMessages(UserAccount useraccount, Date afterFrom, Date beforeTo) throws IllegalArgumentException {
            this.useraccount = useraccount;
            this.afterFrom = afterFrom;
            this.beforeTo  = beforeTo;

            getMessages(0);
        }
        
        /**
         * Find all user FOLDER messages before a specified data.
         * @param offset 0 means from the beginning of the Paged messages returned
         */
        private boolean getMessages(int offset) throws IllegalArgumentException {
            IndexedPageView view = new IndexedPageView(offset, IndexBasePoint.BEGINNING, USER_MESSAGES_PAGING_SIZE);

            try{
                Restriction filter = (afterFrom == null) ? // new IsEqualTo(MessagePropertyPath.IS_RESEND, false)
                        new IsLessThan(MessagePropertyPath.SENT_TIME, beforeTo ) :
                        new And( new IsLessThan(MessagePropertyPath.SENT_TIME, beforeTo),
                                new IsGreaterThan(MessagePropertyPath.SENT_TIME, afterFrom));

                Mailbox mailbox = new Mailbox(useraccount.email);
                StandardFolderId folder = new StandardFolderId( FOLDER, mailbox);

                findResponse = service.findItem(folder, 
                		Search.FIELDS.MESSAGE_READ_ITEM, filter, 
                		Search.FIELDS.ORDER_MESSAGES_BY, view );

                List<Item> items = findResponse.getItems();
                
                if(items != null && items.size() > 0){
                    messageIterator = items.iterator();
                    return true;
                }

            }catch (ServiceException e){
                log.warning("Unable to find " + FOLDER + " messages for user account [" + 
                		useraccount.email + "] " + " from [" + 
                		((afterFrom == null )? "INFINITY" : afterFrom) + "]. " +
                		" to [" + beforeTo + "]" +
                        "Verify the Exchange user " + service.getUsername() + " has permission " +
                        "to read messages in user accounts " + FOLDER + " folder. " +
                        "EWS throw a " + e.getClass().getName() + " error message: " + e.getMessage() );
            }
            return false;
        }

        private boolean getMoreMessages(){
            if (findResponse.getIndexedPagingOffset() < findResponse.getTotalItemsInView()){
                findOffset = findResponse.getIndexedPagingOffset();
                return getMessages(findOffset);
            }
            return false;
        }

        /**
         * True if there is an next message to iterate too,
         * otherwise false if all messages were iterated too or 
         * if there is no message to iterate at all.
         * A users email messages are iterated in from, to date order.
         * @return true if there is a next message to iterate too, otherwise false.
         */
        protected boolean hasNext(){
            if(messageIterator != null){
                if(messageIterator.hasNext())
                    return true;
                else
                    return getMoreMessages(); // try get more messages
            }
            return false;
        }

        /**
         * get in next user message in date-time order - date message was sent to the user.
         * A users email messages are iterated in from, to date order.
         * @return a indexable meta data content of the message
         */
        protected IndexableMessage next(){
            if (!hasNext())
                return null;

            try{
                Item item = messageIterator.next();
                if (item instanceof Message){
                    Message message = (Message) item;                   
                    return createIndexableMessage( service.getMessage( message.getItemId(), Search.FIELDS.MESSAGE_READ ) );
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        private IndexableMessage createIndexableMessage(Message message){
            // NOTE: com.independentsoft.exchange.Message aN=From aG=To aH=Cc aI=Bcc R=getTextBody()
            IndexableMessage im = new IndexableMessage();

            im.mailboxName       = FOLDER.name();
            im.mailboxOwner      = new Email(useraccount.firstname, useraccount.surname, useraccount.displayname, useraccount.email);
            im.messageExchangeId = message.getItemId().getId();
            im.messageInternetId = message.getInternetMessageId();
            im.conversationId    = message.getConversationId().getId(); 
            
            im.from              = toEmail(message.getFrom());
            im.to                = toEmails(message.getToRecipients());
            im.cc                = toEmails(message.getCcRecipients());
            im.bcc               = toEmails(message.getBccRecipients());
            
            im.created  	     = message.getCreatedTime();
            im.sent     	     = message.getSentTime();
            im.received 	     = message.getReceivedTime();
            im.subject  	     = message.getSubject();
            im.body     	     = getBodyText(message.getTextBody());
            im.bodyType 	     = (message.getBody() != null) ? message.getBody().getType().toString() : null;
            
            return im;
        }
        
        private String getBodyText(Body body){
            if(body != null && body.getText() != null && !body.getText().isEmpty()){
	            switch(body.getType()){
	            case HTML: 
	            	// remove HTML Development formatting
	            	String firstPass = body.getText().replaceAll("\\<.*?\\>", "").trim(); // 
	                // replace line breaks with space because browsers inserts space
	                String secondPass = firstPass.replace("\r", " ");
	                // replace line breaks with space because browsers inserts space
	                String thirdPass = secondPass.replace("\n", " ");
	                // remove step-formatting
	                String forthPass = thirdPass.replace("\t", "");
	            	return forthPass.replace("&nbsp;", "");
	            case TEXT:
	            case BEST:
	            case NONE: return body.getText().trim();
	            }
            }
            return null;
        }

        private Email toEmail(Mailbox mailbox){
        	return (mailbox == null) ? null :
        		new Email(null, mailbox.getName(), mailbox.getOriginalDisplayName(), mailbox.getEmailAddress());
        }

        private List<Email> toEmails(List<Mailbox> mailboxes){
            List<Email> emails = new ArrayList<Email>();
            for(Mailbox mailbox : mailboxes){
                if(mailbox.getEmailAddress() != null)
                    emails.add( toEmail( mailbox ) );
            }
            return emails;
        }
        
        /**
         * Use javax.mail module to extracts Message Header information
         * From: David Turley &lt;davidturley@iKube.onmicrosoft.com&gt;
         * To: administrator &lt;administrator@iKube.onmicrosoft.com&gt;
         * Subject: RE: test message 1
         * @param message
         * @return
         * import com.independentsoft.msg.ExtendedProperty;
         * import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
         * import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.InternetHeaders;
         *
        private IndexableMessage getMessageRecipients(Message message){
        	IndexableMessage data = new IndexableMessage();
        	//Regex regex = new Regex("To:.*<(.+)>");
        	ExtendedProperty p = message.getExtendedProperty(MapiPropertyTag.PR_TRANSPORT_MESSAGE_HEADERS);
        	
        	String value = p.getValue();
			try {
				InternetHeaders headers = new InternetHeaders(
						new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
				data.from    = toEmail(getFirst(headers.getHeader("From")));
				data.to      = toEmails(headers.getHeader("To"));
				data.cc      = toEmails(headers.getHeader("Cc"));
				data.bcc     = toEmails(headers.getHeader("Bcc"));
				data.subject = getFirst(headers.getHeader("Subject"));
			} catch (MessagingException e) {}
        	return data;
        } 
        
        private String getFirst(String[] values){
        	if(values != null && values.length > 0)
        		return values[0];
        	return null;
        }

        private IndexableMessage.Email toEmail(String mailbox){
        	if(mailbox == null) return null;

        	String displayName = null, email = null;
        	StringTokenizer st = new StringTokenizer(mailbox, "<>");
        	if(st.hasMoreTokens())
        		displayName = st.nextToken();
        	if(st.hasMoreTokens())
        		email = st.nextToken();
        	return (email == null) ? null :
        		new IndexableMessage.Email(null, displayName, displayName, email);
        }
        
        private List<IndexableMessage.Email> toEmails(String[] mailboxes){
            List<IndexableMessage.Email> emails = new ArrayList<IndexableMessage.Email>();
            if(mailboxes != null){
	            for(String mailbox : mailboxes){
	            	IndexableMessage.Email email = toEmail(mailbox);
	                if(email != null)
	                    emails.add( email );
	            }
            }
            return emails;
        }
		*/
    }

	/**
     * @See UsersMessages(final ExchangeClient client, final Date fromAfter, final Date toBefore)
	 */
    public UsersMessages getUsersMessages(Date fromAfter, Date toBefore){
    	return new ExchangeClient.UsersMessages(this, fromAfter, toBefore);
    }

	/**
     * @See UsersMessages(final ExchangeClient client, final Date fromAfter, final Date toBefore, final String resumeFromEmail, final String resumeFromMessageIdExclusive)
	 */
    public UsersMessages getUsersMessages(Date fromAfter, Date toBefore, String resumeFromEmail, String resumeFromMessageIdExclusive){
    	return new UsersMessages(this, fromAfter, toBefore, resumeFromEmail, resumeFromMessageIdExclusive);
    }
    
    public class UsersMessages {
    	private final   ExchangeClient client;
    	private Date    afterFrom, beforeTo;
    	
    	private boolean isResume = false;
    	private String  resumeFromEmail, resumeFromMessageIdExclusive;
    	
    	private List<ExchangeClient.UserAccount> users;
    	private Iterator<ExchangeClient.UserAccount> usersIterator;   
    	
    	private ExchangeClient.UserAccount currentUser;
    	private ExchangeClient.UserMessages currentUserMessages;
    	
        /**
         * Get FOLDER messages of all users ordered by person email username ascending and message sent date descending.
         * Iterate through emails of persons by email username alphabetically, and messages from oldest to newest sent date.
         * hasNext() and getNext() iterate a users email messages from oldest to newest.
         *
    	 * @param afterFrom       - optional - from the specified email address sent date (oldest) or null back to the big bang.
    	 * @param beforeTo         - required - to the latest email message sent date (newest).
         */
    	private UsersMessages(final ExchangeClient client, final Date afterFrom, final Date beforeTo){
    		this( client, afterFrom,  beforeTo, null,  (String)null );
    	}

    	/**
         * Get FOLDER messages of all users ordered by person email username ascending and message sent date descending.
         * Iterate through emails of persons by email username alphabetically, and messages from oldest to newest sent date.
         * hasNext() and getNext() iterate a users email messages from oldest to newest.
         *
    	 * @param afterFrom       - optional - from the specified email address sent date (oldest) or null back to the big bang.
    	 * @param beforeTo        - required - to the latest email message sent date (newest).
    	 * @param resumeFromEmail - optional - start processing from this email address in alphabetic email username order.
    	 * @param resumeFromMessageIdExclusive - optional - start processing from this email message exchange id.
    	*/
    	private UsersMessages(final ExchangeClient client, final Date afterFrom, final Date beforeTo, 
    			final String resumeFromEmail, final String resumeFromMessageIdExclusive) {
    		
    		this.client = client;
    		this.afterFrom = afterFrom;
    		this.beforeTo  = beforeTo;
    		this.resumeFromEmail = resumeFromEmail;
    		this.resumeFromMessageIdExclusive = resumeFromMessageIdExclusive;
    		
    		if(client == null || beforeTo == null)
    			throw new IllegalArgumentException(this.getClass().getName() + " constructer failed, required attributes client and toSentDate are empty.");
    		
    		if(afterFrom != null && beforeTo != null && afterFrom.after(beforeTo))
    			throw new IllegalArgumentException(this.getClass().getName() + " constructer failed, fromSentDate cannot be after toSentDate.");
    		if( (resumeFromEmail != null && resumeFromMessageIdExclusive == null) || (resumeFromEmail == null && resumeFromMessageIdExclusive != null) )
    			throw new IllegalArgumentException(this.getClass().getName() + " constructer failed, if one optional attributes resumeFromEmail or resumeFromMessageIdExclusive is specified, then both must be specfied. Cannot resume.");
    			
    		isResume = (resumeFromEmail != null && resumeFromMessageIdExclusive != null);
    	}

    	/**
    	 * Get the next message in sequence, grouped by user account ascending, user message sent date descending order. 
    	 * @return the next message or null if no more messages are available.
    	 */
        public IndexableMessage next(){
        	return getNextMessage();
        }

        /**
         * next user with messages
         * @return
         */
        private ExchangeClient.UserAccount getNextUser(){
        	// get the first user (from the beginning or a resume point) 
        	currentUser = null;
        	if(users == null)
        		return getFirstUser();
        	else
        		return getNextUser2();
        }
        
        /** 
         * get the next message of users in sequence, 
         * going from one user to the next until all user messages are exhausted */
        private IndexableMessage getNextMessage(){
        	if(currentUser == null)
        		getNextUser();

        	if(currentUser != null){
            	if(isResume)
            		resumeToUserMessage();

            	if(currentUserMessages == null)
            		currentUserMessages = client.getUserMessages(currentUser, afterFrom, beforeTo ); 

            	if( currentUserMessages != null && currentUserMessages.hasNext() )
            		return currentUserMessages.next();
            	else
            		currentUserMessages = null;

            	getNextUser();
            	return getNextMessage();
        	}
        	return null;
        }

        private ExchangeClient.UserAccount getFirstUser(){
    		users = client.getUserAccounts();
            if(users != null && users.size() > 0){
            	usersIterator = users.iterator();
            	if(isResume){
            		return resumeToUser();
            	}
            	currentUser = usersIterator.next();
            	return currentUser;
            }
            return null;
        }
        
        private ExchangeClient.UserAccount getNextUser2(){
        	if(usersIterator != null && usersIterator.hasNext()){
        		currentUser = usersIterator.next();
        		return currentUser;
        	}
        	return null;        	
        }
        
        private ExchangeClient.UserAccount resumeToUser(){
    		while(usersIterator.hasNext()){
    			currentUser = usersIterator.next();
    			if(resumeFromEmail.equals(currentUser.email))
    				return currentUser;
        	}
    		throw new IllegalArgumentException("Unable to find resume message as mailbox " + resumeFromEmail + " cannot be found by username " + 
    				client.getService().getUsername() + ". Verify the mailbox still exists, and the username still has access rights to read this mailbox.");
        }

        private void resumeToUserMessage(){
    		isResume = false;
    		currentUserMessages = client.getUserMessages(currentUser, afterFrom, beforeTo );
    		while( currentUserMessages != null && currentUserMessages.hasNext() ){
    			IndexableMessage currentMsg = currentUserMessages.next();
    			if(currentMsg != null && currentMsg.messageExchangeId != null && currentMsg.messageExchangeId.equals(resumeFromMessageIdExclusive))
    				break;  // resumed complete, were back to where we left off (last processed user message found).
        	}	
        }
    }
}