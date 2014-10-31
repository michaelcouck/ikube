package ikube.action.index.parse;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.parse.HtmlParser;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 10.02.10
 * @version 01.00
 */
public class HtmlParserTest extends AbstractTest {

	@Test
	public void parse() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "html.html");
		byte[] bytes = FileUtilities.getContents(file, IConstants.ENCODING).getBytes();
		// URL url = new URL("http://www.oki.com");
		HtmlParser parser = new HtmlParser();
		// byte[] bytes = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toByteArray();
		OutputStream parsed = parser.parse(new ByteArrayInputStream(bytes), new ByteArrayOutputStream());
		assertTrue(parsed.toString().indexOf("nodethirtythree") > -1);
	}

}
