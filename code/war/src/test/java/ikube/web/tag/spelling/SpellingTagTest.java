package ikube.web.tag.spelling;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import ikube.web.tag.ATag;
import ikube.web.tag.ATagTest;

import javax.servlet.jsp.tagext.Tag;

import org.junit.Before;
import org.junit.Test;

/**
 * TODO Comment me.
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
public class SpellingTagTest extends ATagTest {

	private SpellingTag spellingTag;
	private String searchString = "wrongk";
	@SuppressWarnings("unused")
	private String correctedString = "wrongs";

	@Before
	public void before() throws Exception {
		super.before();
		spellingTag = new SpellingTag();
		spellingTag.setPageContext(pageContext);
		spellingTag.setBodyContent(bodyContent);

		when(request.getParameter(ATag.SEARCH_STRINGS)).thenReturn(searchString);
	}

	@Test
	public void doStartTag() throws Exception {
		int result = spellingTag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);
		// assertEquals(correctedString, pageContext.getAttribute(ATag.CHECKED_SEARCH_STRING));
	}

	@Test
	public void doEndTag() throws Exception {
		int result = spellingTag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
	}

}
