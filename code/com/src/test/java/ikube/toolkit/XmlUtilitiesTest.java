package ikube.toolkit;

import ikube.AbstractTest;
import ikube.Constants;
import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static ikube.toolkit.FileUtilities.findFileRecursively;
import static ikube.toolkit.XmlUtilities.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 06-03-2011
 */
public class XmlUtilitiesTest extends AbstractTest {

    @Test
    public void getDocumentElementsAndElement() throws FileNotFoundException {
        File file = findFileRecursively(new File(".").getAbsoluteFile(), 3, "spring.xml");
        InputStream inputStream = new FileInputStream(file);
        Document document = getDocument(inputStream, Constants.ENCODING);
        assertNotNull("This should be the Spring configuration file : ", document);
        List<Element> elements = getElements(document.getRootElement(), "import");
        assertTrue("There are at least one imports in this file : ", elements.size() > 0);
        Element beanElement = getElement(document.getRootElement(), "import");
        assertNotNull("The bean element is the configurer : ", beanElement);
    }

    @Test
    public void getElementNodeNameAttributeValue() throws FileNotFoundException {
        File file = findFileRecursively(new File(".").getAbsoluteFile(), "xml-utilities.xml");
        logger.error("File : " + file);
        InputStream inputStream = new FileInputStream(file);
        Document document = getDocument(inputStream, Constants.ENCODING);

        Element element = getElement(document.getRootElement(), "industry", "id", "851");
        assertNotNull(element);
        assertEquals("851", element.attribute("id").getValue());
    }

}