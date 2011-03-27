package ikube.web.tag;

import static org.junit.Assert.assertEquals;

import javax.servlet.jsp.tagext.Tag;

import org.junit.Before;
import org.junit.Test;

/**
 * TODO Comment me.
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
public class ContextTagTest extends ATagTest {

	private ContextTag contextTag;

	@Before
	public void before() throws Exception {
		super.before();
		contextTag = new ContextTag();
		contextTag.setPageContext(pageContext);
	}

	@Test
	public void doEndTag() throws Exception {
		int result = contextTag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
	}

}
