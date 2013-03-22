package ikube.index.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.parse.text.TextParser;
import ikube.index.parse.xml.XMLParser;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ParserProviderTest extends ATest {

	public ParserProviderTest() {
		super(ParserProviderTest.class);
	}

	@Test
	public void getParser() {
		String mimeTypeString = ".txt";
		byte[] bytes = "hello world".getBytes();

		IParser parser = ParserProvider.getParser(mimeTypeString, bytes);
		parser = ParserProvider.getParser(mimeTypeString, bytes);
		assertNotNull("Text parser can never be null : ", parser);
		assertEquals("This should be the text parser : ", TextParser.class, parser.getClass());

		mimeTypeString = ".xml";
		File file = FileUtilities.findFileRecursively(new File("."), IConstants.SPRING_XML);
		bytes = FileUtilities.getContents(file, Integer.MAX_VALUE).toByteArray();

		parser = ParserProvider.getParser(mimeTypeString, bytes);
		assertNotNull("Xml parser can never be null : ", parser);
		assertEquals("This should be the Xml parser : ", XMLParser.class, parser.getClass());
		
		mimeTypeString = "dtd";
		parser = ParserProvider.getParser(mimeTypeString, bytes);
		assertNotNull("Text parser can never be null : ", parser);
		assertEquals("This should be the text parser : ", TextParser.class, parser.getClass());
	}

	@Test
	public void getParserPerformance() {
		int iterations = 100;
		final String mimeTypeString = ".txt";
		final byte[] bytes = "hello world".getBytes();
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				ParserProvider.getParser(mimeTypeString, bytes);
			}
		}, "Get parser performance : ", iterations, Boolean.FALSE);
		assertTrue("This function must be fast : " + executionsPerSecond, executionsPerSecond > 10);
	}

}
