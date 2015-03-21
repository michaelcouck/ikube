package ikube.action.index.parse;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is the base interface for all parsers. Parsers that can different content types like xml, Word and so on.
 * 
 * @author Michael Couck
 * @since 21-11-2010
 * @version 01.00
 * @deprecated Switched to the {@link org.apache.tika.Tika} framework for content extraction
 */
@Deprecated
public interface IParser {

	/**
	 * This is the method that accepts the raw data as an input stream, parses it into text and writes it to the output stream/
	 * 
	 * @param inputStream the raw data, must be UTF-8 of course
	 * @param outputStream the output stream where the parsed content will be written
	 * @return the output stream that was passed to the method
	 * @throws Exception
	 */
	OutputStream parse(InputStream inputStream, OutputStream outputStream) throws Exception;

}
