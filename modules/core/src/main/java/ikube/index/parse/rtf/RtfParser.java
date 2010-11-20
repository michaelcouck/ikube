package ikube.index.parse.rtf;

import ikube.IConstants;
import ikube.index.parse.IParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;

import javax.swing.text.DefaultStyledDocument;
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
	public final OutputStream parse(InputStream inputStream) throws Exception {
		RTFEditorKit kit = new RTFEditorKit();
		Document doc = kit.createDefaultDocument();
		Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
		kit.read(reader, doc, 0);
		byte[] bytes = doc.getText(0, doc.getLength()).getBytes(IConstants.ENCODING);
		OutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(bytes);
		return outputStream;
	}

	/**
	 * This method is from SearchBlox.
	 */
	protected String parseContent(URL url, String string) throws Exception {
		String content = "";
		DefaultStyledDocument defaultstyleddocument = new DefaultStyledDocument();
		RTFEditorKit kit = new RTFEditorKit();
		kit.read(new InputStreamReader(url.openStream(), string), defaultstyleddocument, 0);
		content = defaultstyleddocument.getText(0, defaultstyleddocument.getLength());
		return content;
	}
}