package ikube.jms;

import ikube.IConstants;
import ikube.jms.connect.ActiveMQConnector;
import ikube.jms.connect.WebSphereConnector;
import ikube.jms.connect.WeblogicConnector;
import ikube.toolkit.FILE;
import ikube.toolkit.XML;
import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
@SuppressWarnings("FieldCanBeLocal")
public class PublisherIntegration extends AbstractIntegration {

    private String headerNames = "bankGroupId:bankName:exchangeConditionExternalId";
    private String headerValues = "Nedbank:Nedbank:EC_ACH_MDT_TT1_INIT";

    @Test
    // @Ignore
    public void publishRemoteWas() throws Exception {
        File pain_9 = FILE.findFileRecursively(new File("."), "pain-9-from-ach-on-us-tt1.xml");

        InputStream pain_9_inputStream = new ByteArrayInputStream(FILE.getContent(pain_9).getBytes());
        Document pain_9_document = XML.getDocument(pain_9_inputStream, IConstants.ENCODING);

        Element pain_9_mesgId = XML.getElement(pain_9_document.getRootElement(), "MsgId");
        Element pain_9_mndtId = XML.getElement(pain_9_document.getRootElement(), "MndtId");
        Element pain_9_mndtReqId = XML.getElement(pain_9_document.getRootElement(), "MndtReqId");

        for (int j = 0; j < 60; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 15; i++) {
                randomNumber(pain_9_mesgId);
                randomString(pain_9_mndtId);
                randomString(pain_9_mndtReqId);
                publishRemoteWas(queue, pain_9_document.asXML());
            }
            sleep(start);
        }
    }

    private void publishRemoteWas(final String queue, final String payload) throws Exception {
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

    @Test
    @Ignore
    public void publishRemoteActiveMQ() throws Exception {
        try {
            JmsUtilities.startServer();
            publisher.publish("userid",
                    "password",
                    "tcp://localhost:61616",
                    "jms/QCF",
                    "jms/InterchangeLoaderQ",
                    "headerName",
                    "headerValue",
                    "payload",
                    ActiveMQConnector.class.getCanonicalName(), 1, null);
        } finally {
            JmsUtilities.stopServer();
        }
    }

    @Test
    @Ignore
    public void publishRemoteWebLogic() throws Exception {
        publisher.publish(
                "qcfuser",
                "passw0rd",
                "t3://be-qa-cs-18.clear2pay.com:8001",
                "jms/QCF",
                "jms/InterchangeLoaderQ",
                "headerName",
                "headerValue",
                "payload",
                WeblogicConnector.class.getName(), 1, null);
    }

}