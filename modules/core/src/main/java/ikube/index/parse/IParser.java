package ikube.index.parse;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is the base interface for all parsers. Parsers that can different content types like xml, Word and so on.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IParser {

	/**
	 * This is the method that accepts the raw data as an input stream, parses it into text and writes it to the output stream/
	 * 
	 * @param inputStream the raw data, must be UTF-8 of course
	 * @param outputStream the output stream where the parsed content will be written
	 * @return the output stream that was passed to the method
	 * @throws Exception
	 */
	public OutputStream parse(InputStream inputStream, OutputStream outputStream) throws Exception;

}
