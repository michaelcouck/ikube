package ikube.action.index.handler.internet.exchange;

import org.junit.Test;

import com.independentsoft.exchange.ServiceException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class ExchangeClientTest {
    private final Logger log = Logger.getLogger(this.getClass().getName());
    
    private static final SimpleDateFormat FMT_DATE      = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat FMT_DATE_TIME = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    
    private static final String EWS_URL  = "https://outlook.office365.com/ews/exchange.asmx";
    private static final String USERNAME = "administrator@iKube.onmicrosoft.com";
    private static final String PASSWORD = "iKube101";

    private static final String PROXY_HOST      = "proxy.post.bpgnet.net";
    private static final int    PROXY_PORT      = 8080;
    private static final String PROXY_DOMAIN    = "POST";
    private static final String PROXY_USERNAME  = "turleyd";
    private static final String PROXY_PASSWORD  = "12345678";
    
    public static ExchangeClient TEST_CONN = new ExchangeClient(EWS_URL, null, USERNAME, PASSWORD );
    //public static ExchangeClient TEST_CONN = new ExchangeClient(EWS_URL, null, USERNAME, PASSWORD, PROXY_HOST, PROXY_PORT, PROXY_DOMAIN, PROXY_USERNAME, PROXY_PASSWORD );

    @Test
    public void testTestUserMessage() throws Exception {
        getExchangeUsersInboxEmails(TEST_CONN, null, new Date() );
    }

    @Test
    public void testUserMessageBetweenPeriod() throws ParseException, ServiceException {
        getExchangeUsersInboxEmails(TEST_CONN, FMT_DATE.parse("01/07/2015"), new Date());
    }

    private void getExchangeUsersInboxEmails(ExchangeClient exchange, Date from, Date to){
        List<ExchangeClient.UserAccount> users = exchange.getUserAccounts();
        if(users != null && users.size() > 0) {
            for(ExchangeClient.UserAccount user : users){
                ExchangeClient.UserMessages msgs = exchange.getUserMessages(user, to, from );
                while(msgs.hasNext()){
                    IndexableMessage index = msgs.next();
                    //index.toConsole();
                }
            }
        }
    }
    
    @Test
    public void testNextUserMessage() throws Exception {
    	getNextMessage(TEST_CONN, null, new Date(), null, null);
    }    

    @Test
    public void testResumeNextUserMessage() throws Exception {
    	// get 3 messages
    	int count = getNextMessage(TEST_CONN, null, new Date(), "administrator@iKube.onmicrosoft.com", 
    			"AAMkADRkY2U5YmExLWZiYWItNGFhZi05NDgzLWIwMDA4NmI0MzRhNgBGAAAAAABaNy139P83SIf1vuZ5Uq12BwAJ2FDPM8p0T6ve50c06FLjAAAAAAEMAAAJ2FDPM8p0T6ve50c06FLjAAAE5QZnAAA=");
    	assert(count == 3);
    } 

    @Test
    public void testResumeNextUserMessageToDate() throws Exception {
    	// get 1 message - after Mon Jul 28 10:05:55 CEST 2014
    	int count = getNextMessage(TEST_CONN, FMT_DATE_TIME.parse("28/07/2014 10:05:56"), new Date(), "administrator@iKube.onmicrosoft.com", 
    			"AAMkADRkY2U5YmExLWZiYWItNGFhZi05NDgzLWIwMDA4NmI0MzRhNgBGAAAAAABaNy139P83SIf1vuZ5Uq12BwAJ2FDPM8p0T6ve50c06FLjAAAAAAEMAAAJ2FDPM8p0T6ve50c06FLjAAAGIanPAAA=");
    	assert(count == 1);
    } 
    
    private int getNextMessage(ExchangeClient exchange, Date from, Date to, String resumeFromEmail, String resumeFromMessageIdExclusive){
    	int count = 0;
    	ExchangeClient.UsersMessages um;
    	if(resumeFromEmail == null && resumeFromMessageIdExclusive == null)
    		um = exchange.getUsersMessages( from, to);
    	else
    		um = exchange.getUsersMessages( from, to, resumeFromEmail, resumeFromMessageIdExclusive);

        while(um != null){
        	IndexableMessage msg = um.next();
        	if(msg == null){
        		break;
        	}else{
        		count++;
        		log.info(msg.toString());
        	}
        }
        return count;
    }

    public void regEx() throws FileNotFoundException, IOException{
    	String value = readMessageHeaderFile();
		if(value != null){
			String[] fr = value.split("To:.*<(.+)>");
			String[] to = value.split("|^To:(.*)|mi");
			String[] cc = value.split("|^cc:(.*)|mi");
			String[] bc = value.split("|^bcc:(.*)|mi");
		}
    }
    
    public String readMessageHeaderFile() throws FileNotFoundException, IOException{
    	String file = new String("C:/dave/github/metrics/src/java/ikube/action/index/handler/email/exchange/MessageHeader.txt");
    	
    	StringBuilder sb = new StringBuilder();
    	BufferedReader br = new BufferedReader(new FileReader( file ));
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }
        return sb.toString();
    }
}