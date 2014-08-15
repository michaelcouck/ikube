package ikube.toolkit;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 06-03-2011
 */
public final class XmlUtilities {

    private static final Logger LOGGER = Logger.getLogger(XmlUtilities.class);

    /**
     * Singularity.
     */
    private XmlUtilities() {
        // Documented
    }

    /**
     * Finds the node in the xml with the specified name recursively iterating through the elements in the document.
     * This method will return the first tag with the name specified.
     *
     * @param node the top level or root tag to start looking from
     * @param name the name of the tag or node
     * @return the node or tag with the specified name or null if no such tag can be found
     */
    public static Element getElement(final Element node, final String name) {
        if (node.getName().equals(name)) {
            return node;
        }
        List<?> elements = node.elements();
        for (final Object element : elements) {
            Element childElement = getElement((Element) element, name);
            if (childElement != null) {
                return childElement;
            }
        }
        return null;
    }

    /**
     * Returns all the nodes with the specified name from the parent tag one level down.
     *
     * @param parent the tag from which to start looking
     * @param name   the name of the tag to look for
     * @return a list of all the tags with the name, can be empty
     */
    @SuppressWarnings("unchecked")
    public static List<Element> getElements(final Element parent, final String name) {
        ArrayList<Element> elements = new ArrayList<>();
        if (parent == null) {
            return elements;
        }
        List<Element> children = parent.elements();
        for (final Element child : children) {
            if (child.getName().equals(name)) {
                elements.add(child);
            }
        }
        return elements;
    }

    /**
     * Generates a document that can be parsed form an xml string.
     *
     * @param inputStream the input stream to the xml data
     * @return the XML document from the xml string
     */
    public static Document getDocument(final InputStream inputStream, final String encoding) {
        Document document = null;
        try {
            SAXReader reader = new SAXReader(false);
            reader.setEncoding(encoding);
            reader.setValidation(false);
            document = reader.read(inputStream);
        } catch (Exception e) {
            LOGGER.error("Exception reading xml and generating a document : ", e);
        } finally {
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

    public static void parse(File file) throws Exception {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser parser = parserFactory.newSAXParser();
        parser.parse(file, new DefaultHandler() {
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                LOGGER.error("Uri : " + uri);
                LOGGER.error("Local name : " + localName);
                LOGGER.error("QName : " + qName);
                LOGGER.error("Attributes : " + attributes);
            }
        });
    }

}