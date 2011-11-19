package ikube.index.parse.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.kahadb.util.ByteArrayInputStream;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class TextParserTest extends ATest {

	private String russian = "бронежилет";
	private String japanese = "も楽しめる";
	private String arabic = "للتضحية";
	private String carriageReturn = "Michael and\nEva are getting a new life\rpossibly in another country";

	public TextParserTest() {
		super(TextParserTest.class);
	}

	@Test
	public void parse() throws Exception {
		TextParser textParser = new TextParser();
		File file = FileUtilities.findFileRecursively(new File("."), new String[] { "txt.txt" });
		InputStream inputStream = new FileInputStream(file);
		OutputStream outputStream = textParser.parse(inputStream, new ByteArrayOutputStream());
		String string = outputStream.toString();

		assertTrue(string.contains(russian));
		assertTrue(string.contains(japanese));
		assertTrue(string.contains(arabic));

		outputStream = textParser.parse(new ByteArrayInputStream(carriageReturn.getBytes()), new ByteArrayOutputStream());
		string = outputStream.toString();
		logger.info("Parsed : " + string);
		assertEquals("", "Michael and Eva are getting a new life possibly in another country", string);
	}

}
