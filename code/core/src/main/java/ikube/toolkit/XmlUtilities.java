package ikube.toolkit;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class XmlUtilities {

	private static Logger LOGGER = Logger.getLogger(XmlUtilities.class);

	/**
	 * Finds the node in the xml with the specified name recursively iterating through the elements in the document. This method will return
	 * the first tag with the name specified.
	 * 
	 * @param node
	 *            the top level or root tag to start looking from
	 * @param name
	 *            the name of the tag or node
	 * @return the node or tag with the specified name or null if no such tag can be found
	 */
	public static Element getElement(Element node, String name) {
		if (node.getName().equals(name)) {
			return node;
		}
		List<?> children = node.elements();
		for (int i = 0; i < children.size(); i++) {
			Element child = getElement((Element) children.get(i), name);
			if (child != null) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Returns all the nodes with the specified name from the parent tag one level down.
	 * 
	 * @param parent
	 *            the tag from which to start looking
	 * @param name
	 *            the name of the tag to look for
	 * @return a list of all the tags with the name, can be empty
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<Element> getElements(Element parent, String name) {
		ArrayList<Element> elements = new ArrayList<Element>();
		if (parent == null) {
			return elements;
		}
		List<Element> children = parent.elements();
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			if (child.getName().equals(name)) {
				elements.add(child);
			}
		}
		return elements;
	}

	/**
	 * Generates a document that can be parsed form an xml string.
	 * 
	 * @param inputStream
	 *            the input stream to the xml data
	 * @return the XML document from the xml string
	 */
	public static Document getDocument(InputStream inputStream, String encoding) {
		ByteArrayOutputStream bos = null;
		Document document = null;
		try {
			SAXReader reader = new SAXReader(false);
			reader.setEncoding(encoding);
			reader.setValidation(false);
			document = reader.read(inputStream);
		} catch (Exception e) {
			LOGGER.error("Exception reading xml and generating a document : " + bos, e);
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (Exception e) {
					LOGGER.error("Exception", e);
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					LOGGER.error("Exception", e);
				}
			}
		}
		return document;
	}

}
