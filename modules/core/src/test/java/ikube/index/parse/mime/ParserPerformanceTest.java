package ikube.index.parse.mime;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.parse.html.HtmlParser;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.ByteArrayInputStream;
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
		// final HtmlParser htmlParser = new HtmlParser();
		final HtmlParser jerichoParser = new HtmlParser();

		InputStream inputStream = ParserPerformanceTest.class.getResourceAsStream("/index.html");
		final byte[] bytes = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toByteArray();

		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() {
				try {
					// htmlParser.parse(bytes);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "HTML Parser : ", iterations);
		// assertTrue(executionsPerSecond > 0);

		executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() {
				try {
					jerichoParser.parse(new ByteArrayInputStream(bytes));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "Jericho Parser : ", iterations);
		assertTrue(executionsPerSecond > 100);
	}

	@Test
	public void pattern() throws Exception {
		InputStream inputStream = this.getClass().getResourceAsStream("/index.html");
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
		assertTrue(executionsPerSecond > 100);
	}

}