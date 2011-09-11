package ikube.index.parse.word;

import ikube.index.parse.IParser;

import java.io.InputStream;
import java.io.OutputStream;

import com.aspose.words.Document;

/**
 * Parser for the Word format.
 * 
 * @author Michael Couck
 * @since 12.05.04
 * @version 01.00
 */
public class AsposeWordParser implements IParser {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		Document document = new Document(inputStream);
		outputStream.write(document.getText().trim().getBytes());
		return outputStream;
	}
}