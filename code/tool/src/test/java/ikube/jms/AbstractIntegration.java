package ikube.jms;

import ikube.AbstractTest;
import ikube.jms.connect.WebSphereConnector;
import ikube.toolkit.THREAD;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.mockito.Spy;

import java.util.Random;

public abstract class AbstractIntegration extends AbstractTest {

    protected String userid = "admin"; // admin, qcfuser
    protected String password = "password"; // password, qcfuser
    protected String url = "iiop://192.168.1.16:2809";
    protected String connectionFactory = "cell/nodes/app1/servers/OPFClusterApp1/jms/QCF";
    protected String destinationPrefix = "cell/nodes/app1/servers/OPFClusterApp1/jms/";
    protected String queueSuffix = "_OPFCluster";
    protected String connectorType = WebSphereConnector.class.getName();

    protected String headerNames = "bankGroupId:bankName:exchangeConditionExternalId";

    @Spy
    protected Publisher publisher;

    protected String[] queues = new String[] {
            "BFMJMSAPIQueue", // 0
            "BFMJMSCallbackQueue", // 1
            "BFMJMSReplyQueue", // 2
            "BFMSOAPJMSAPIQueue", // 3
            "BFMSOAPJMSReplyQueue", // 4
            "BPEHldQueue", // 5
            "BPEIntQueue", // 6
            "BPELInvokerQ", // 7
            "BPERetQueue", // 8
            "BillingEventsQ", //9
            "COBAXTDataImportQ", // 10
            "CoralBOEQ", // 11
            "DataImportQ", // 12
            "DewarehousingQ", // 13
            "FromSWIFTAckQ", // 14
            "FromSWIFTQ", // 15
            "HTMHldQueue", // 16
            "HTMIntQueue", // 17
            "IntegrationLayerRequestQ", // 18
            "InterchangeLoaderAckQ", // 19
            "InterchangeLoaderQ", // 20
            "InterfacingHeraldQ", // 21
            "InterfacingReplyQ", // 22
            "InterfacingRequestQ", // 23
            "ManualReconciliationQ", // 24
            "NotificationQ", // 25
            "OPFNotificationQ", // 26
            "OPFOnlineParserQ", // 27
            "OutgoingInitiationQ", // 28
            "ParserQ", // 29
            "PublishRateQ", // 30
            "SOInitiationQ", // 31
            "SenderFallbackQ", // 32
            "SenderQ", // 33
            "StartOfDayQ", // 34
            "StatusReportQ", // 35
            "TestNotificationQ", // 36
            "TimeoutHandlerQ", // 37
            "TransportReceiptQ", // 38
            "Urgency8BPELInvokerQ", // 39
            "Urgency8InterchangeLoaderQ", // 40
            "Urgency8ParserQ", // 41
            "XMLReportQ" // 42
    };
    // _SYSTEM.Exception.Destination.OPFCluster.000-BPM.De1.Bus

    protected String queue = queues[20];

    protected void publishRemoteWas(final String queue, final String headerValues, final String payload) throws Exception {
        publisher.publish(
                userid, // userid
                password, // password
                url, // url
                connectionFactory, // connection factory
                destinationPrefix + queue, // destination name
                headerNames, // header names
                headerValues, // header values
                payload,
                connectorType, 1, null);
    }

    protected void randomNumber(final Element element) {
        String numbers = "1234567890";
        String text = element.getText();
        int lastIndeOfForwardSlash = text.lastIndexOf('/') + 1;
        String number = RandomStringUtils.random(6, numbers);
        element.setText(text.substring(0, lastIndeOfForwardSlash) + number);
    }

    protected void randomString(final Element element) {
        String alphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String text = element.getText();
        String randomText = RandomStringUtils.random(text.length(), alphaNumeric);
        element.setText(randomText);
    }

    protected void sleep(final long start) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - start;
        long period = 60000;
        if (duration < period) {
            long sleep = period - duration;
            logger.info("Sleeping for : " + sleep);
            THREAD.sleep(sleep);
        }
    }

}
