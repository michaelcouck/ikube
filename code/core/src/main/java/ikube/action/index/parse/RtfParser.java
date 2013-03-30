package ikube.action.index.parse;

import ikube.IConstants;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

/**
 * Parser for the RTF format.
 * 
 * @author Michael Couck
 * @since 12.05.04
 * @version 01.00
 */
public class RtfParser implements IParser {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		RTFEditorKit kit = new RTFEditorKit();
		Document doc = kit.createDefaultDocument();
		Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
		kit.read(reader, doc, 0);
		byte[] bytes = doc.getText(0, doc.getLength()).getBytes(IConstants.ENCODING);
		outputStream.write(bytes);
		return outputStream;
	}

}