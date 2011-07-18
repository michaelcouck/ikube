package ikube.toolkit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;

import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.Test;

public class XmlUtilitiesTest extends ATest {

	public XmlUtilitiesTest() {
		super(XmlUtilitiesTest.class);
	}

	@Test
	public void getDocumentElementsAndElement() {
		InputStream inputStream = getClass().getResourceAsStream("/META-INF/spring.xml");
		Document document = XmlUtilities.getDocument(inputStream, IConstants.ENCODING);
		assertNotNull("This should be the Spring configuration file : ", document);
		List<Element> elements = XmlUtilities.getElements(document.getRootElement(), "import");
		assertTrue("There are at least one imports in this file : ", elements.size() > 0);
		Element beanElement = XmlUtilities.getElement(document.getRootElement(), "bean");
		assertNotNull("The bean element is the configurer : ", beanElement);
	}

}
