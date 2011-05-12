package ikube.index.parse.html;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 10.02.10
 * @version 01.00
 */
public class HtmlParserTest extends ATest {

	public HtmlParserTest() {
		super(HtmlParserTest.class);
	}

	/**
	 * TODO Ignored while the internet is still not connected
	 */
	@Test
	@Ignore
	public void parse() throws Exception {
		URL url = new URL("http://www.oki.com");
		HtmlParser parser = new HtmlParser();
		byte[] bytes = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toByteArray();
		OutputStream parsed = parser.parse(new ByteArrayInputStream(bytes), new ByteArrayOutputStream());
		assertTrue(parsed.toString().indexOf("Oki") > -1);
	}

}
