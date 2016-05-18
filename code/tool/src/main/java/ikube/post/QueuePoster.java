package ikube.post;

import ikube.jms.Publisher;
import ikube.jms.connect.WebSphereConnector;
import ikube.toolkit.FILE;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class QueuePoster {

    private static Publisher publisher;

    public static void main(final String[] args) throws Exception {
        File xsl = FILE.findFileRecursively(new File("."), "pain.009.001.03.Nedbank.xsl");
        Source source = new StreamSource();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        publisher.publish(
                "admin",
                "password",
                "iiop://192.168.1.12:2809",
                "cell/nodes/app1/servers/OPFClusterApp1/jms/QCF",
                "cell/nodes/app1/servers/OPFClusterApp1/jms/InterchangeLoaderQ",
                "headerName",
                "headerValue",
                "Hello world",
                WebSphereConnector.class.getName(), 1, null);
    }

}
