package ikube.web.tag.spelling;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import ikube.web.tag.ATag;
import ikube.web.tag.ATagTest;

import javax.servlet.jsp.tagext.Tag;

import org.junit.Before;
import org.junit.Test;

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

		when(request.getParameter(ATag.SEARCH_STRING)).thenReturn(searchString);
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
