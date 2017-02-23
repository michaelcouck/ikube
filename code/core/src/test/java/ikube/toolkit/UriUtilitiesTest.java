package ikube.toolkit;

import ikube.AbstractTest;
import org.junit.Test;

import java.net.URI;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12-10-2010
 */
public class UriUtilitiesTest extends AbstractTest {

    @Test
    public void url() throws Exception {
        URI baseUri = new URI("http://www.ikokoon.eu/ikokoon/index.html?language=russian");
        String reference = "/ikokoon/info/about.html;jsessionid=96069DDCEF2D6525AA946B529817214E?language=russian";
        String resolved = ikube.toolkit.URI.resolve(baseUri, reference);
        logger.info("Resolved : " + resolved);

        String string = baseUri.getAuthority();
        logger.info("Auth        : " + string);
        string = baseUri.getFragment();
        logger.info("Frag        : " + string);
        string = baseUri.getHost();
        logger.info("Host        : " + string);
        string = baseUri.getPath();
        logger.info("Path        : " + string);
        string = baseUri.getQuery();
        logger.info("Query        : " + string);
        string = baseUri.getScheme();
        logger.info("Sche        : " + string);
        string = baseUri.getSchemeSpecificPart();
        logger.info("Spec        : " + string);
        string = baseUri.getUserInfo();
        logger.info("User_        : " + string);
        string = Integer.toString(baseUri.getPort());
        logger.info("Port        : " + string);

        URI uri = new URI(baseUri.getScheme(), baseUri.getUserInfo(), baseUri.getHost(), baseUri.getPort(), baseUri.getPath(), baseUri.getQuery(),
                baseUri.getFragment());
        logger.info("Uri : " + uri);

        reference = "http://www.ikokoon.eu/ikokoon/info/about.html;jsessionid=96069DDCEF2D6525AA946B529817214E?language=russian";
        resolved = ikube.toolkit.URI.resolve(baseUri, reference);
        logger.info("Resolved : " + resolved);

        // ;jsessionid=(.*)
        // (.*);jsessionid=(.*)$
        // (.*);jsessionid=(.*)(\&amp;|\&amp;amp;)
        // ([;_]?((?i)l|j|bv_)?((?i)sid|phpsessid|sessionid)=.*?)(\?|&amp;|#|$)
        // "(?i)^(.*);jsessionid=\\w+(.*)"

        Pattern pattern = Pattern.compile(";jsessionid=(.*)");
        String replaced = pattern.matcher(reference).replaceAll("replacement");
        logger.info("Replaced 1 : " + replaced);

        pattern = Pattern.compile("(.*);jsessionid=(.*)$");
        replaced = pattern.matcher(reference).replaceAll("replacement");
        logger.info("Replaced 2 : " + replaced);

        pattern = Pattern.compile("(.*);jsessionid=(.*)(\\&amp;|\\&amp;amp;)");
        replaced = pattern.matcher(reference).replaceAll("replacement");
        logger.info("Replaced 3 : " + replaced);

        pattern = Pattern.compile("([;_]?((?i)l|j|bv_)?((?i)sid|phpsessid|sessionid)=.*?)(\\?|&amp;|#|$)");
        replaced = pattern.matcher(reference).replaceAll("?");
        logger.info("Replaced 4 : " + replaced);
    }

    @Test
    public void strip() {
        String url = "http://www.ikokoon.eu/ikokoon/index.html#anchor";
        String stripped = ikube.toolkit.URI.stripAnchor(url, "");
        logger.info("Stripped : " + stripped);
        assertFalse(stripped.contains("#"));

        url = "http://www.ikokoon.eu/ikokoon/index.html?query=string#anchor";
        stripped = ikube.toolkit.URI.stripAnchor(url, "");
        logger.info("Stripped : " + stripped);
        assertFalse(stripped.contains("#"));

        url = "http://www.ikokoon.eu/ikokoon/#anchor?can=the&query=string&be=here";
        stripped = ikube.toolkit.URI.stripAnchor(url, "");
        logger.info("Stripped : " + stripped);
        assertFalse(stripped.contains("#"));
    }

    @Test
    public void isExcluded() {
        String string = "JavaScript:";
        assertTrue(ikube.toolkit.URI.isExcluded(string));

        string = "javascript:¨var someVar = null;";
        assertTrue(ikube.toolkit.URI.isExcluded(string));

        string = "not a javascript link";
        assertFalse(ikube.toolkit.URI.isExcluded(string));
    }

    @Test
    public void resolve() {
        URI baseURI = URI.create("http://www.ikokoon.eu/ikokoon/info/about.html;jsessionid=96069DD?language=russian");
        String reference = "/ikokoon/software/free.html;jsessionid=96069DD?language=english";
        String resolved = ikube.toolkit.URI.resolve(baseURI, reference);
        logger.info("Resolved : " + resolved);
        assertEquals("http://www.ikokoon.eu/ikokoon/software/free.html;jsessionid=96069DD?language=english", resolved.toString());

        reference = "../free.html;jsessionid=96069DD?language=english";
        resolved = ikube.toolkit.URI.resolve(baseURI, reference);
        logger.info("Resolved : " + resolved);
        assertEquals("http://www.ikokoon.eu/ikokoon/free.html;jsessionid=96069DD?language=english", resolved.toString());
    }

    @Test
    public void stripBlanks() {
        String url = "http://www.google.be/support/googleanalytics/bin/answer.py?answer=81977&cbid=1ervhd4u13k8d&src=cb&lev= index";
        String expected = "http://www.google.be/support/googleanalytics/bin/answer.py?answer=81977&cbid=1ervhd4u13k8d&src=cb&lev=index";
        String stripped = ikube.toolkit.URI.stripBlanks(url);
        assertEquals(expected, stripped);
    }

    @Test
    public void stripCarriageReturn() {
        String string = "\r\nAnd the string. \n\r";
        String stripped = ikube.toolkit.URI.stripCarriageReturn(string);
        logger.debug("Stripped : " + stripped);
        assertEquals("And the string. ", stripped);
    }

    @Test
    public void removeDotSegments() throws Exception {
        String string = "http://www.google.com/ikube/./ikube/./svn/./java";
        URI uri = new URI(string);
        URI strippedUri = ikube.toolkit.URI.removeDotSegments(uri);
        logger.info("Stripped uri : " + strippedUri);
        assertEquals("http://www.google.com/ikube/ikube/svn/java", strippedUri.toString());

        string = "http://www.google.com/ikube/../ikube/./svn/../java";
        uri = new URI(string);
        strippedUri = ikube.toolkit.URI.removeDotSegments(uri);
        logger.info("Stripped uri : " + strippedUri);
        assertEquals("http://www.google.com/ikube/java", strippedUri.toString());
    }

    @Test
    public void getIp() throws Exception {
        String ipAddress = ikube.toolkit.URI.getIp();
        assertFalse(ipAddress.equals("127.0.0.1"));
        assertFalse(ipAddress.equals("127.0.1.1"));
    }

    @Test
    public void pattern() {
        String stringPattern = ".*(serenity/source).*";
        Pattern pattern = Pattern.compile(stringPattern);
        boolean matches = pattern.matcher("/usr/serenity/source").matches();
        assertTrue(matches);
    }
}
