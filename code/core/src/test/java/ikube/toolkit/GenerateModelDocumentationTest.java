package ikube.toolkit;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;

import org.junit.Test;

public class GenerateModelDocumentationTest extends AbstractTest {

	@Test
	public void main() {
		String html = new GenerateModelDocumentation().createEntityFieldTable();
		logger.info(html);
		assertTrue(html.contains("<table><tr><th>Name</th><th>Property</th><th>Lucene field</th><th>Description</th></tr><tr><td>Indexable</td><td>"
				+ "name<br>address<br>stored<br>analyzed<br>vectored<br>maxExceptions<br>threads"));
	}

}
