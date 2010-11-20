package ikube.index.parse.word;

//import java.io.ByteArrayInputStream;

import ikube.index.parse.IParser;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

// import com.aspose.words.Document;

/**
 * This is the Word parser implementation for the Aspose parsers. An alternative to the POI library which seems to have memory leaks in it,
 * this could be as a result of throwing exceptions and then not cleaning the memory, but in any event not suited to mass data processing
 * requirements.
 *
 * @author Michael Couck
 * @since 17.04.08
 * @version 01.00
 */
public class AsposeWordParser implements IParser {

	/** Logger for the parser class. */
	private Logger LOGGER = Logger.getLogger(AsposeWordParser.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream parse(InputStream inputStream) {
		try {
			// Open the document we want to convert.
			// ByteArrayInputStream inputStream = new ByteArrayInputStream(resource.getBytes());
			// Document document = new Document(inputStream);
			// resource.setBytes(document.getText().getBytes());
		} catch (Exception t) {
			LOGGER.error("Exception parsing Word doc with the Aspose parser", t);
		}
		return null;
	}

}
