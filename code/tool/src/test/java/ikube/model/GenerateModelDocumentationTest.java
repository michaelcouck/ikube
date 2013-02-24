package ikube.model;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

public class GenerateModelDocumentationTest {

	private Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void main() {
		String html = new GenerateModelDocumentation().createEntityFieldTable();
		logger.info("Html : " + html);
		assertTrue(html.contains("<tr><td></td><td>address</td><td>false</td><td>Whether this is a geospatial address field</td></tr>"));
	}

}
