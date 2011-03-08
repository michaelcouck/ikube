package ikube.index.parse.xml;

import ikube.IConstants;
import ikube.index.parse.IParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

/**
 * @author Michael Couck
 * @since 03.09.10
 * @version 01.00
 */
public class XMLParser implements IParser {

	@Override
	public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
		InputSource inputSource = new InputSource(reader);

		SAXReader saxReader = new SAXReader();
		saxReader.setValidation(false);
		saxReader.setIgnoreComments(false);
		saxReader.setIncludeExternalDTDDeclarations(false);
		saxReader.setIncludeInternalDTDDeclarations(false);

		Document doc = saxReader.read(inputSource);
		Element root = doc.getRootElement();
		StringWriter writer = new StringWriter();
		visit(root, writer);
		outputStream.write(writer.toString().getBytes(IConstants.ENCODING));
		return outputStream;
	}

	/**
	 * Visits each tag up and down the tree recursively getting the text content from the tag.
	 * 
	 * @param parent
	 *            the parent tag to start recursing
	 * @param content
	 *            the content buffer to accumulate the text in
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private final void visit(final Element parent, final Writer writer) throws IOException {
		String text = parent.getTextTrim();
		if (text != null && !text.equals("")) {
			writer.append(' ').append(text).append(' ');
		}
		List<Element> children = parent.elements();
		for (Element child : children) {
			visit(child, writer);
		}
	}

}