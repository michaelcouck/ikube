package ikube.action.index.parse;

import ikube.IConstants;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.List;

/**
 * This class will extract text data from an xml file. It will completely ignore dtd files,
 * going so far as to return dummy files so it doesn't validate against a non existing file and
 * crash.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 03-09-2010
 */
public class XMLParser implements IParser {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
		InputSource inputSource = new InputSource(reader);

		SAXReader saxReader = new SAXReader();
		saxReader.setValidation(Boolean.FALSE);
		saxReader.setIgnoreComments(Boolean.FALSE);
		saxReader.setIncludeExternalDTDDeclarations(Boolean.FALSE);
		saxReader.setIncludeInternalDTDDeclarations(Boolean.FALSE);
		saxReader.setStripWhitespaceText(Boolean.FALSE);
		// We completely turn off the dtd resolution
		saxReader.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(final String arg0, final String arg1) throws SAXException, IOException {
				InputStream in = getClass().getResourceAsStream("/empty.dtd");
				return new InputSource(in);
			}
		});

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
	 * @param parent the parent tag to start recursing
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void visit(final Element parent, final Writer writer) throws IOException {
		writer.append(' ').append(parent.getName());
		String text = parent.getTextTrim();
		if (text != null && !text.equals("")) {
			writer.append(' ').append(text).append(' ');
		}
		// Get all the attributes as well
		List<Attribute> attributes = parent.attributes();
		if (attributes != null) {
			for (final Attribute attribute : attributes) {
				String name = attribute.getName();
				String value = attribute.getValue();
				if (name != null && !name.trim().equals("")) {
					writer.append(' ').append(name);
				}
				if (value != null && !value.trim().equals("")) {
					writer.append(' ').append(value);
				}
			}
		}
		List<Element> children = parent.elements();
		for (final Element child : children) {
			visit(child, writer);
		}
	}

}