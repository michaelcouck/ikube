package ikube.index.parse.xml;

import ikube.index.parse.IParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author Michael Couck
 * @since 03.09.10
 * @version 01.00
 */
public class XMLParser implements IParser {

	private SAXReader saxReader;

	public XMLParser() {
		this.saxReader = new SAXReader();
		this.saxReader.setValidation(false);
		this.saxReader.setIgnoreComments(false);
		this.saxReader.setIncludeExternalDTDDeclarations(false);
		this.saxReader.setIncludeInternalDTDDeclarations(false);
	}

	@Override
	public final OutputStream parse(InputStream inputStream) throws Exception {
		Document doc = saxReader.read(inputStream);
		Element root = doc.getRootElement();
		StringWriter writer = new StringWriter();
		visit(root, writer);
		OutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(writer.toString().getBytes());
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
	private final void visit(Element parent, Writer writer) throws IOException {
		String text = parent.getTextTrim();
		if (text != null && !text.equals("")) {
			writer.append(" ").append(text).append(" ");
		}
		List<Element> children = parent.elements();
		for (Element child : children) {
			visit(child, writer);
		}
	}

}