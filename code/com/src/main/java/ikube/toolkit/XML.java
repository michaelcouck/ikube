package ikube.toolkit;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 06-03-2011
 */
public final class XML {

    private static final Logger LOGGER = Logger.getLogger(XML.class);

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
        @SuppressWarnings("unchecked")
        List<Element> elements = node.elements();
        for (final Object element : elements) {
            Element childElement = getElement((Element) element, name);
            if (childElement != null) {
                return childElement;
            }
        }
        return null;
    }

    /**
     * This method will return the element with the specified attribute value. Note that this will return the first,
     * one found, so the attributes should be unique for meaningful results.
     *
     * @param node           the top level or root tag to start looking from
     * @param name           the name of the tag or node
     * @param attributeName  the  name of the attribute to find in the target element
     * @param attributeValue the  value of the attribute to find in the elements
     * @return the node or tag with the specified name or null if no such tag can be found
     */
    public static Element getElement(final Element node, final String name, final String attributeName, final String attributeValue) {
        if (node == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<Element> elements = node.elements();
        for (final Element element : elements) {
            if (element.getName().equals(name)) {
                String elementAttributeValue = getAttributeValue(element, attributeName);
                if (elementAttributeValue != null && elementAttributeValue.equals(attributeValue)) {
                    return element;
                }
            }
            Element childElement = getElement(element, name, attributeName, attributeValue);
            if (childElement != null) {
                return childElement;
            }
        }
        return null;
    }

    /**
     * This method simply returns the attribute value in the element specified.
     *
     * @param element       the element to get the attribute value from
     * @param attributeName the name of the attribute to get the value from
     * @return the attribute value in string form or null if there is no such attribute in the element
     */
    public static String getAttributeValue(final Element element, final String attributeName) {
        Attribute attribute = element.attribute(attributeName);
        if (attribute != null) {
            return attribute.getValue();
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

    /**
     * Singularity.
     */
    private XML() {
        // Documented
    }

}