package ikube.web.tag;

import static org.junit.Assert.assertEquals;

import javax.servlet.jsp.tagext.Tag;

import org.junit.Test;

public class PageTagTest extends ATagTest {

	private PageTag pageTag = new PageTag();

	@Test
	@SuppressWarnings("unused")
	public void doEndTag() throws Exception {
		pagerTag.doStartTag();
		pageTag.setParent(pagerTag);
		int result = pageTag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
		String output = pagerTag.getBodyContent().getString();
		// assertEquals("0", output);

		pagerTag.doAfterBody();
		result = pageTag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
		output = pagerTag.getBodyContent().getString();
		// assertEquals("010", output);
	}

}
