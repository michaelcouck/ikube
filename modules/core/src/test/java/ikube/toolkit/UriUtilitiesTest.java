package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;

import java.net.URI;
import java.util.regex.Pattern;

import org.junit.Test;

public class UriUtilitiesTest extends ATest {

	@Test
	public void url() throws Exception {
		URI baseUri = new URI("http://www.ikokoon.eu/ikokoon/index.html?language=russian");
		String reference = "/ikokoon/info/about.html;jsessionid=96069DDCEF2D6525AA946B529817214E?language=russian";
		String resolved = UriUtilities.resolve(baseUri, reference);
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
		logger.info("User        : " + string);
		string = Integer.toString(baseUri.getPort());
		logger.info("Port        : " + string);

		URI uri = new URI(baseUri.getScheme(), baseUri.getUserInfo(), baseUri.getHost(), baseUri.getPort(), baseUri.getPath(),
				baseUri.getQuery(), baseUri.getFragment());
		logger.info("Uri : " + uri);

		reference = "http://www.ikokoon.eu/ikokoon/info/about.html;jsessionid=96069DDCEF2D6525AA946B529817214E?language=russian";
		resolved = UriUtilities.resolve(baseUri, reference);
		String resolvedString = resolved.toString();
		logger.info("Resolved : " + resolvedString);

		// ;jsessionid=(.*)
		// (.*);jsessionid=(.*)$
		// (.*);jsessionid=(.*)(\&amp;|\&amp;amp;)
		// ([;_]?((?i)l|j|bv_)?((?i)sid|phpsessid|sessionid)=.*?)(\?|&amp;|#|$)
		// "(?i)^(.*);jsessionid=\\w+(.*)"

		Pattern pattern = Pattern.compile(";jsessionid=(.*)");
		String replaced = pattern.matcher(reference).replaceAll("replacement");
		logger.info("Replaced : " + replaced);

		pattern = Pattern.compile("(.*);jsessionid=(.*)$");
		replaced = pattern.matcher(reference).replaceAll("replacement");
		logger.info("Replaced : " + replaced);

		pattern = Pattern.compile("(.*);jsessionid=(.*)(\\&amp;|\\&amp;amp;)");
		replaced = pattern.matcher(reference).replaceAll("replacement");
		logger.info("Replaced : " + replaced);

		pattern = Pattern.compile("([;_]?((?i)l|j|bv_)?((?i)sid|phpsessid|sessionid)=.*?)(\\?|&amp;|#|$)");
		replaced = pattern.matcher(reference).replaceAll("?");
		logger.info("Replaced : " + replaced);
	}

	@Test
	public void strip() {
		String url = "http://www.ikokoon.eu/ikokoon/index.html#anchor";
		String stripped = UriUtilities.stripAnchor(url, "");
		logger.info("Stripped : " + stripped);
		assertFalse(stripped.contains("#"));

		url = "http://www.ikokoon.eu/ikokoon/index.html?query=string#anchor";
		stripped = UriUtilities.stripAnchor(url, "");
		logger.info("Stripped : " + stripped);
		assertFalse(stripped.contains("#"));

		url = "http://www.ikokoon.eu/ikokoon/#anchor?can=the&query=string&be=here";
		stripped = UriUtilities.stripAnchor(url, "");
		logger.info("Stripped : " + stripped);
		assertFalse(stripped.contains("#"));
	}

	@Test
	public void isExcluded() {
		String string = "JavaScript:";
		assertTrue(UriUtilities.isExcluded(string));

		string = "javascript:¨var someVar = null;";
		assertTrue(UriUtilities.isExcluded(string));

		string = "not a javascript link";
		assertFalse(UriUtilities.isExcluded(string));
	}

	@Test
	public void resolve() {
		URI baseURI = URI
				.create("http://www.ikokoon.eu/ikokoon/info/about.html;jsessionid=96069DDCEF2D6525AA946B529817214E?language=russian");
		String reference = "/software/free.html;jsessionid=96069DDCEF2D6525AA946B529817214E?language=english";
		String resolved = UriUtilities.resolve(baseURI, reference);
		logger.debug("Resolved : " + resolved);
	}

	@Test
	public void stripBlanks() {
		String url = "http://www.google.be/support/googleanalytics/bin/answer.py?answer=81977&cbid=1ervhd4u13k8d&src=cb&lev= index";
		String expected = "http://www.google.be/support/googleanalytics/bin/answer.py?answer=81977&cbid=1ervhd4u13k8d&src=cb&lev=index";
		String stripped = UriUtilities.stripBlanks(url);
		assertEquals(expected, stripped);
	}

	@Test
	public void stripCarriageReturn() {
		String string = "\r\nAnd the string. \n\r";
		String stripped = UriUtilities.stripCarriageReturn(string);
		logger.debug("Stripped : " + stripped);
		assertEquals("And the string. ", stripped);
	}
}
