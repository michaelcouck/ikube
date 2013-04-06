package ikube.action.index.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.action.index.parse.TextParser;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class TextParserTest extends AbstractTest {

	private TextParser textParser;
	private String russian = "бронежилет";
	private String japanese = "も楽しめる";
	private String arabic = "لِلتَّضْحِيَةِ";
	private String carriageReturn = "Michael and\nEva are getting a new life\rpossibly in another country";

	public TextParserTest() {
		super(TextParserTest.class);
	}

	@Before
	public void before() {
		textParser = new TextParser();
	}

	@Test
	public void parse() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), new String[] { "txt.txt" });
		InputStream inputStream = new FileInputStream(file);
		OutputStream outputStream = textParser.parse(inputStream, new ByteArrayOutputStream());
		String string = outputStream.toString();

		assertTrue(string.contains(russian));
		assertTrue(string.contains(japanese));
		assertTrue(string.contains(arabic));

		InputStream input = new ByteArrayInputStream(carriageReturn.getBytes());
		outputStream = textParser.parse(input, new ByteArrayOutputStream());
		string = outputStream.toString();
		assertEquals("Carriage returns stripped : ", "Michael and Eva are getting a new life possibly in another country", string);
	}

	@Test
	public void parseSpaces() throws Exception {
		String string = "there is  some text.with a dot and some/non$characters";
		InputStream input = new ByteArrayInputStream(string.getBytes());
		OutputStream outputStream = textParser.parse(input, new ByteArrayOutputStream());
		String output = outputStream.toString();
		assertEquals("Text sans the non characters and double spaces : ", "there is some text with a dot and some non characters", output);
	}

	@Test
	public void parseArabic() throws Exception {
		InputStream input = new ByteArrayInputStream(arabic.getBytes());
		OutputStream outputStream = textParser.parse(input, new ByteArrayOutputStream());
		String output = outputStream.toString();
		assertTrue(output.contains(arabic));
	}

}
