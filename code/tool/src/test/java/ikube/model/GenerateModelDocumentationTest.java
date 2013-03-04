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
		assertTrue(html
				.contains("<tr><td>Indexable</td><td>name<br>address<br>stored<br>analyzed<br>vectored<br>maxExceptions<br></td>"
						+ "<td>false<br>false<br>false<br>false<br>false<br>false<br></td><td nowrap=\"nowrap\">The name of this indexable<br>Whether "
						+ "this is a geospatial address field<br>Whether this value should be stored in the index<br>Whether this field should be analyzed for "
						+ "stemming and so on<br>Whether this field should be vectored in the index<br>This is the maximum exceptions during indexing before "
						+ "the indexing is stopped<br></td></tr>"));
	}

}
