package ikube.web.tag.spelling;

import static org.junit.Assert.assertEquals;
import ikube.web.tag.ATag;
import ikube.web.tag.ATagTest;

import javax.servlet.jsp.tagext.Tag;

import org.junit.Before;
import org.junit.Test;

public class SpellingTagTest extends ATagTest {

	private SpellingTag spellingTag;
	private String searchString = "wrongk";
	private String correctedString = "wrongs";

	@Before
	public void before() {
		spellingTag = new SpellingTag();
		spellingTag.setPageContext(pageContext);
		spellingTag.setBodyContent(bodyContent);
		request.getParameterMap().put(ATag.SEARCH_STRING, new String[] { searchString });
	}

	@Test
	public void doStartTag() throws Exception {
		int result = spellingTag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);
		assertEquals(correctedString, pageContext.getAttribute(ATag.CHECKED_SEARCH_STRING));
	}

	@Test
	public void doEndTag() throws Exception {
		int result = spellingTag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
	}

}
