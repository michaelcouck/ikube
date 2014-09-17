package ikube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

import static ikube.IConstants.IKUBE;
import static ikube.IConstants.SEP;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-06-2014
 */
public abstract class Client {

    private static final String SERVICE = "/service";

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getUrl(final String url, final int port, final String service, final String method) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append(SEP);
        builder.append(IKUBE);
        builder.append(SERVICE);
        builder.append(SEP);
        builder.append(service);
        builder.append(SEP);
        builder.append(method);
        return new URL("http", url, port, builder.toString()).toString();
    }

}