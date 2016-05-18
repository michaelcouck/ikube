package ikube.jms;

import ikube.IConstants;
import ikube.toolkit.FILE;
import ikube.toolkit.XML;
import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
@SuppressWarnings("FieldCanBeLocal")
public class TT3OnUs extends AbstractIntegration {

    private String channelValues = "Nedbank:Nedbank:EC_CHANNEL_1";
    private String postilionValues = "Nedbank:Nedbank:EC_POSTILION";

    @Test
    @Ignore
    public void caaa2ThenPain9() throws Exception {
        // MndtReqId => TxRef
        for (int i = 0; i < 1; i++) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1; j++) {
                caaa2(pain9());
            }
            sleep(start);
        }
        caaa2(pain9());
    }

    private String pain9() throws Exception {
        // Get the message xml
        File pain_9 = FILE.findFileRecursively(new File("."), "pain-9-to-ach-on-us-tt3.xml");
        InputStream pain_9_inputStream = new ByteArrayInputStream(FILE.getContent(pain_9).getBytes());
        Document pain_9_document = XML.getDocument(pain_9_inputStream, IConstants.ENCODING);

        // Change the unique field values
        Element pain_9_mesgId = XML.getElement(pain_9_document.getRootElement(), "MsgId");
        Element pain_9_mndtId = XML.getElement(pain_9_document.getRootElement(), "MndtId");
        Element pain_9_mndtReqId = XML.getElement(pain_9_document.getRootElement(), "MndtReqId");
        randomNumber(pain_9_mesgId);
        randomString(pain_9_mndtId);
        randomString(pain_9_mndtReqId);

        // Publish the message
        publishRemoteWas(queue, channelValues, pain_9_document.asXML());

        return pain_9_mndtReqId.getText();
    }

    private void caaa2(final String MndtReqId) throws Exception {
        File caaa_2 = FILE.findFileRecursively(new File("."), "caaa-2-to-ach-on-us-tt3.xml");
        InputStream caaa_2_inputStream = new ByteArrayInputStream(FILE.getContent(caaa_2).getBytes());
        Document caaa_2_document = XML.getDocument(caaa_2_inputStream, IConstants.ENCODING);

        Element caaa_2_txnRef = XML.getElement(caaa_2_document.getRootElement(), "TxRef");
        caaa_2_txnRef.setText(MndtReqId);

        publishRemoteWas(queue, postilionValues, caaa_2_document.asXML());
    }

}