package ikube.action.index.handler.internet.exchange;

import com.independentsoft.exchange.*;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

/**
 * Search Exchange user accounts for mail in
 * inbox folder between specified date range.
 * Folder can be inbox, calendar etc.
 *
 * Created by turleyd on 16/07/2014.
 */
public class ExchangeClient {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Service service;

    public ExchangeClient(Service service){
        this.service = service;
    }

    /**
     * Get Exchange person user accounts with mailboxes, using the unified persona store.
     * Multiple contact items can represent a single individual.
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
    public List<UserAccount> getUserAccounts() {
        List<UserAccount> accounts = new ArrayList<UserAccount>();

        try{
            List<PropertyOrder> propertyOrder = new ArrayList<PropertyOrder>();
            propertyOrder.add( new PropertyOrder(ContactPropertyPath.SURNAME, SortDirection.ASCENDING) );
            propertyOrder.add( new PropertyOrder(ContactPropertyPath.GIVEN_NAME, SortDirection.ASCENDING) );

            FindPeopleResponse response = service.findPeople(StandardFolder.DIRECTORY, "SMTP");
            //FindPeopleResponse response = service.findPeople(
            //        StandardFolder.DIRECTORY,
            //        new PersonaShape(ShapeType.ALL_PROPERTIES),
            //        propertyOrder,
            //        "SMTP");

            for(Persona persona : response.getPersonas()) {
                if (persona.getEmailAddress() != null){

                    accounts.add(
                            new UserAccount(
                                    persona.getPersonaId().getId(),
                                    persona.getPersonaType(),
                                    persona.getDisplayName(),
                                    persona.getGivenName(),
                                    persona.getSurname(),
                                    persona.getEmailAddress().getEmailAddress() ));

                                    //persona.getCompanyName());
                                    //persona.getBusinessAddresses());
                                    //persona.getBusinessPhoneNumbers());
                                    //persona.getLocation());
                                    //persona.getBirthdays());
                                    //persona.getManagers());
                }
            }
        }catch (ServiceException e){
            System.out.println(e.getMessage());
            System.out.println(e.getXmlMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    /**
     * Find all user INBOX messages before a specified data.
     * @param useraccount the user account to retrieve email from.
     * @param before emails send before date time.
     * @param before emails send after date time.
     */
    public UserMessages getUserMessages(UserAccount useraccount, Date before, Date after){
        try{
            And filter = (after == null) ?
                    new And( new IsLessThan(MessagePropertyPath.SENT_TIME, before),
                            new IsEqualTo(MessagePropertyPath.IS_RESEND, false)) :
                    new And( new IsLessThan(MessagePropertyPath.SENT_TIME, before),
                            new IsGreaterThan(MessagePropertyPath.SENT_TIME, after),
                            new IsEqualTo(MessagePropertyPath.IS_RESEND, false));

            Mailbox mailbox = new Mailbox(useraccount.email);
            StandardFolderId inboxFolder = new StandardFolderId(StandardFolder.INBOX, mailbox);

            FindItemResponse response = service.findItem(inboxFolder, MessagePropertyPath.getAllPropertyPaths());
            //FindItemResponse response = service.findItem(inboxFolder, MessagePropertyPath.getAllPropertyPaths(), filter);

            return new UserMessages(response.getItems());

        }catch (ServiceException e){
            System.out.println(e.getMessage());
            System.out.println(e.getXmlMessage());

            e.printStackTrace();
        }
        return new UserMessages();
    }

    public List<Appointment> getAppointments(String email, Date from ){
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
    }

    /**
     * Creates a connection to an Exchange server (2007, 2010, 2013, Office365 Exchange Online).
     */
    public static class Connection{
        private Service service;
        private String exchangeUrl, exchangeDomain, exchangeUsername, exchangePassword;
        private String proxyHostname, proxyUsername, proxyPassword;
        private int proxyPort;

        /**
         * Connect to an Exchange server (2007, 2010, 2013, Office365 Online).
         * @param exchangeUrl       "https://outlook.office365.com/ews/exchange.asmx"
         * @param exchangeDomain    optional
         * @param exchangeUsername  "exchange-username"
         * @param exchangePassword  "exchange-password"
         */
        public Connection( String exchangeUrl, String exchangeDomain, String exchangeUsername, String exchangePassword){
            this.exchangeUrl      = exchangeUrl;
            this.exchangeDomain   = exchangeDomain;
            this.exchangeUsername = exchangeUsername;
            this.exchangePassword = exchangePassword;
        }

        /**
         * Connect to an Exchange server through a HTTP proxy.
         * @param exchangeUrl       "https://outlook.office365.com/ews/exchange.asmx"
         * @param exchangeDomain    optional
         * @param exchangeUsername  "admin-username"
         * @param exchangePassword  "admin-password"
         * @param proxyHostname     "proxy.ibm.net or 10.59.92.01"
         * @param proxyPort         8080
         * @param proxyUsername     "system-username"
         * @param proxyPassword     "system-password"
         */
        public Connection( String exchangeUrl, String exchangeDomain, String exchangeUsername, String exchangePassword,
                           String proxyHostname, int proxyPort, String proxyUsername, String proxyPassword){
            this.exchangeUrl      = exchangeUrl;
            this.exchangeDomain   = exchangeDomain;
            this.exchangeUsername = exchangeUsername;
            this.exchangePassword = exchangePassword;
            this.proxyHostname    = proxyHostname;
            this.proxyPort        = proxyPort;
            this.proxyUsername    = proxyUsername;
            this.proxyPassword    = proxyPassword;
        }

        public Service getService(){
            if(service == null) {
                service = new Service(exchangeUrl, exchangeUsername, exchangePassword, exchangeDomain);

                // optional settings - versioning information that identifies the schema version to target for a request.
                service.setRequestServerVersion(RequestServerVersion.EXCHANGE_2013);

                // optional settings
                if(proxyHostname != null)  //service.setProxy(new HttpHost(proxyHostname, proxyPort)); service.setHttpURLConnectionProxy(Proxy.NO_PROXY);
                    service.setHttpURLConnectionProxy( new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHostname, proxyPort)) );
                if(proxyUsername != null && proxyPassword != null)
                    service.setProxyCredentials(new UsernamePasswordCredentials(proxyUsername, proxyPassword));
            }
            return service;
        }

        private void testInternetConnection(){
            try {
                URL url = new URL("http://java.example.org/");
                URLConnection conn = url.openConnection( new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHostname, proxyPort)) );

                System.setProperty("http.proxyHost",  proxyHostname);
                System.setProperty("http.proxyPort",  Integer.toString(proxyPort));
                System.setProperty("https.proxyHost", proxyHostname);
                System.setProperty("https.proxyPort", Integer.toString(proxyPort));

                // Next connection will be through proxy.
                url = new URL("http://www.google.com/");
                InputStream in = url.openStream();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class UserAccount implements Comparator<UserAccount> {
        public String id, type, displayName, firstName, surname, email;
        public FolderId inbox;

        public UserAccount( String id, String type, String displayName, String firstname, String surname, String email){
            this.id    = id;
            this.type  = type;
            this.displayName  = displayName;
            this.firstName = firstName;
            this.surname = surname;
            this.email = email;
            this.inbox = inbox;
        }

        // order useraccounts by displayname
        // Collections.sort(<UserAccount>, this);
        @Override
        public int compare(UserAccount o1, UserAccount o2) {
            return o1.displayName.compareTo(o2.displayName);
        }
    }

    /**
     * Used to filter the user messages that are indexed.
     */
    public class UserMessages {
        private final Logger log = LoggerFactory.getLogger(this.getClass());
        private List<Item> items = new ArrayList<Item>();

        public UserMessages(){}

        public UserMessages(List<Item> items){
            this.items = items;
        }

        public boolean hasNext(){
            return (items != null && items.iterator() != null && items.iterator().hasNext());
        }

        public IndexMessage getNext(){
            if (!hasNext())
                return null;

            try{
                Item item = items.iterator().next();
                if (item instanceof Message){
                    Message message = (Message) item;
                    toConsole(message);

                    // get complete message with entire Body
                    Message message2 = service.getMessage(message.getItemId());
                    return new IndexMessage(
                            message.getFrom().getName(),
                            message.getSubject(),
                            message.getBodyPlainText(),
                            message.getBody().getType().toString(),
                            toEmails(message.getToRecipients()),
                            toEmails(message.getCcRecipients()),
                            toEmails(message.getCcRecipients()),
                            message.getCreatedTime(),
                            message.getSentTime(),
                            message.getReceivedTime() );
                }
            }catch (ServiceException e){
                System.out.println(e.getMessage());
                System.out.println(e.getXmlMessage());

                e.printStackTrace();
            }
            return null;
        }

        private List<String> toEmails(List<Mailbox> mailboxes){
            List<String> emails = new ArrayList<String>();
            for(Mailbox mailbox : mailboxes){
                if(mailbox.getEmailAddress() != null)
                    emails.add(mailbox.getEmailAddress());
            }
            return emails;
        }

        private void toConsole(Message message){
            log.debug("\nUser Mail Message : " +
                    "\n  from = " + message.getFrom().getName() +
                    "\n  to = " + message.getToRecipients() +
                    "\n  bccs = " + message.getBccRecipients() +
                    "\n  ccs = " + message.getCcRecipients() +
                    "\n  subject = " + message.getSubject() +
                    "\n  sentTime = " + message.getSentTime() +
                    "\n  receivedTime = " + message.getReceivedTime() +
                    "\n  isRead = " + message.isRead() +
                    "\n  body Preview = " + message.getBodyPlainText() +
                    "\n  body Preview = " + message.getBody().getType() +  // HTML, TEXT, BEST, NONE;
                    "\n  body Preview = " + message.getBodyHtmlText() +
                    "\n----------------------------------------------------------------");
        }
    }
}