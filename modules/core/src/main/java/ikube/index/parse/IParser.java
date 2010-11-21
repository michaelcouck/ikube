package ikube.index.parse;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IParser {

	public OutputStream parse(InputStream inputStream) throws Exception;

}
