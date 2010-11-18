package ikube.index.parse.word;

import ikube.index.parse.IParser;

import java.io.ByteArrayInputStream;

import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Parser for the Word format.
 *
 * @author Michael Couck
 * @since 12.05.04
 * @version 01.00<br>
 *          ----------------------------------
 * @version 01.1
 * @since 22.08.08<br>
 *        Changed the access to the text from the classes from Ackly chap to the POI WordExtractor class.
 */
public class MSWordParser implements IParser {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String parse(String string) throws Exception {
		StringBuilder content = new StringBuilder();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes());
		POIFSFileSystem fileSystem = new POIFSFileSystem(inputStream);
		WordExtractor extractor = new WordExtractor(fileSystem);
		content.append(extractor.getText().trim());
		return content.toString();
	}
}