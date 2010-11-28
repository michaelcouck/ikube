package ikube.index.parse.excel;

import ikube.index.parse.IParser;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This parser needs to be implemented. The documentation to the Aspose Excel parser is
 * http://www.aspose.com/documentation/product-suites/index.html
 *
 * @author Michael Couck
 * @since 22.05.08
 * @version 01.00
 */
public class AsposeExcelParser implements IParser {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream parse(InputStream inputStream, OutputStream outputStream) throws Exception {
		return outputStream;
	}

}
