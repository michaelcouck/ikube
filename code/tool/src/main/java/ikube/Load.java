package ikube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-06-2014
 */
public abstract class Load {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getUrl(final String url, final int port, final String path) {
        try {
            return new URL("http", url, port, path).toString();
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
