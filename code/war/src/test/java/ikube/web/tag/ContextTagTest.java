package ikube.web.tag;

import static org.junit.Assert.assertEquals;

import javax.servlet.jsp.tagext.Tag;

import org.junit.Test;

public class ContextTagTest extends ATagTest {

	@Test
	public void doEndTag() throws Exception {
		ContextTag contextTag = new ContextTag();
		contextTag.setPageContext(pageContext);
		int result = contextTag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
	}

}
