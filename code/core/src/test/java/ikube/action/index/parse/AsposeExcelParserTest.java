package ikube.action.index.parse;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class AsposeExcelParserTest extends AbstractTest {

	@Test
	public void parse() throws Exception {
		// final InputStream inputStream, final OutputStream outputStream
		AsposeExcelParser asposeExcelParser = new AsposeExcelParser();
		File file = FileUtilities.findFileRecursively(new File("."), "xlsx.xlsx");
		byte[] bytes = FileUtilities.getContents(file, Integer.MAX_VALUE).toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		asposeExcelParser.parse(inputStream, outputStream);
		String text = outputStream.toString();
		String fragment = "Third party management";
		assertTrue("Document contains fragment : " + fragment, text.contains(fragment));
	}

}
