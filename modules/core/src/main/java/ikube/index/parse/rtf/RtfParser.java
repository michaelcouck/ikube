package ikube.index.parse.rtf;

import ikube.index.parse.IParser;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
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
	public final String parse(String string) throws Exception {
		StringBuilder content = new StringBuilder();
		RTFEditorKit kit = new RTFEditorKit();
		Document doc = kit.createDefaultDocument();
		kit.read(new ByteArrayInputStream(string.getBytes()), doc, 0);
		content.append(doc.getText(0, doc.getLength()));
		return content.toString();
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