package ikube.action.index.handler.internet.exchange;

import ikube.action.index.handler.internet.exchange.ExchangeClient;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExchangeClientTest {
    private static final String EWS_URL = "https://outlook.office365.com/ews/exchange.asmx";
    private static final String USERNAME = "administrator@iKube.onmicrosoft.com";
    private static final String PASSWORD = "iKube101";

    private static final String PROXY_USERNAME = "turleyd";
    private static final String PROXY_PASSWORD = "proxypassword";
    private static final String PROXY_URL = "proxy.post.bpgnet.net";
    private static final int    PROXY_PORT = 8080;

    public static ExchangeClient.Connection TEST_CONN = new ExchangeClient.Connection(
            EWS_URL, null, USERNAME, PASSWORD );

    public static ExchangeClient.Connection TEST_CONN_PROXY = new ExchangeClient.Connection(
            EWS_URL, null, USERNAME, PASSWORD,
            PROXY_URL, PROXY_PORT, PROXY_USERNAME, PROXY_PASSWORD);

    @Test
    public void testTestUserMessageAll() throws Exception {
        getExchangeUsersInboxEmails(
                new ExchangeClient( TEST_CONN.getService()) ,
                new Date(),
                null );
    }

    @Test
    public void testUserMessageBetweenPeriod() throws ParseException {
        getExchangeUsersInboxEmails(
                new ExchangeClient( TEST_CONN.getService() ),
                new Date(),
                new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2015") );
    }

    private void getExchangeUsersInboxEmails(ExchangeClient exchange, Date before, Date after){
        List<ExchangeClient.UserAccount> userAccounts = exchange.getUserAccounts();
        if(userAccounts != null && userAccounts.size() > 0) {
            for(ExchangeClient.UserAccount userAccount : userAccounts){
                ExchangeClient.UserMessages msgs = exchange.getUserMessages(userAccount, before, after );
                IndexMessage index = msgs.getNext();
            }
        }
    }
}