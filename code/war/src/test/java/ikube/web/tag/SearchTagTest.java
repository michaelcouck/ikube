package ikube.web.tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO Comment me.
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
@Ignore
public class SearchTagTest extends ATagTest {

	private SearchTag searchTag;

	@Before
	public void before() throws Exception {
		super.before();
		searchTag = new SearchTag();
		searchTag.setSearchUrl(searchUrl);
		searchTag.setBodyContent(bodyContent);
		searchTag.setPageContext(pageContext);
	}

	@After
	public void after() {
		// This throws an exception in the ClassReader from ASM. Very
		// strange, however it is not the class reader from ObjectWeb but
		// some copy paste into Mockit. Shouldn't have done that old chap.
		Mockit.tearDownMocks();
	}

	@Test
	public void getContents() {
		// InputStream
		String resultsXml = getResultsXml();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(resultsXml.getBytes());
		ByteArrayOutputStream outputStream = FileUtilities.getContents(inputStream, Integer.MAX_VALUE);
		assertNotNull(outputStream);
		assertEquals(resultsXml, outputStream.toString());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deserialize() {
		String resultsXml = getResultsXml();
		List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(resultsXml);
		assertNotNull(results);
		assertTrue(results.size() > 0);
	}

	@Test
	public void doAfterBody() throws Exception {
		int result = searchTag.doAfterBody();
		assertEquals(Tag.EVAL_PAGE, result);
	}

	@Test
	public void doEndTag() throws Exception {
		int result = searchTag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
	}

	@Test
	@SuppressWarnings("unused")
	public void doStartTag() throws Exception {
		int result = searchTag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);
		Object results = request.getSession().getAttribute(ATag.RESULTS);
		// TODO Fix these
		// assertNotNull(results);
		// assertTrue(List.class.isInstance(results));
		// assertTrue(((List<?>) results).size() >= 1);
	}

}
