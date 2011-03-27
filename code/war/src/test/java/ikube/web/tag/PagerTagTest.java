package ikube.web.tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;

import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO Comment me.
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
public class PagerTagTest extends ATagTest {

	@Test
	@Ignore
	public void doLifeCycle() throws Exception {
		// TODO Fix this
		int result = pagerTag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);
		do {
			result = pagerTag.doAfterBody();
			Number page = pagerTag.getPage();
			String url = pagerTag.getUrl();
			// logger.log(Level.SEVERE, "Url : " + url + ", page : " + page);
			assertNotNull(page);
			assertNotNull(url);
			StringBuilder builder = new StringBuilder(baseUrl).append("?");
			builder.append(anotherParameter).append("=").append(anotherParameterValue).append("&");
			builder.append(aParameter).append("=").append(aParameterValue).append("&");
			builder.append(ATag.FIRST_RESULT).append("=").append(page).append("&");
			builder.append(ATag.MAX_RESULTS).append("=").append(maxResults);
			assertEquals(builder.toString(), url);
		} while (result == IterationTag.EVAL_BODY_AGAIN);
		result = pagerTag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
	}

}
