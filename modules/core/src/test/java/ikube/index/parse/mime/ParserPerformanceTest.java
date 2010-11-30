package ikube.index.parse.mime;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.parse.html.HtmlParser;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Performance testing for the parsers.
 *
 * @author Michael Couck
 * @since 04.04.10
 * @version 01.00
 */
public class ParserPerformanceTest extends ATest {

	private int iterations = 100;

	@Test
	public void htmlParserJerichoCompare() throws Exception {
		final HtmlParser htmlParser = new HtmlParser();
		File file = FileUtilities.findFile(new File("."), new String[] { "html.html" });
		byte[] bytes = FileUtilities.getContents(file).toByteArray();
		final InputStream inputStream = new ByteArrayInputStream(bytes);
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() {
				try {
					htmlParser.parse(inputStream, new ByteArrayOutputStream());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "HTML Parser : ", iterations);
		assertTrue(executionsPerSecond > 10);
	}

	@Test
	public void pattern() throws Exception {
		File file = FileUtilities.findFile(new File("."), new String[] { "html.html" });
		byte[] bytes = FileUtilities.getContents(file).toByteArray();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		final String string = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
		// (@)?(href=')?(HREF=')?(HREF=\")?(href=\")?
		// This pattern will extract the urls form the log so we can check them
		final Pattern pattern = Pattern
				.compile("(http://|https://)[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() {
				Matcher matcher = pattern.matcher(string);
				while (matcher.find()) {
					matcher.group();
				}
			}
		}, "Pattern matcher : ", iterations);
		assertTrue(executionsPerSecond > 10);
	}

}