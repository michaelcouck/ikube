package ikube.web.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This tag is nested in the pager tag. Each iteration over the pager tag a page number is made available to the children. This tag then
 * accesses the next page number from the pager tag and prints it to the page.
 * 
 * @author Michael Couck
 * @since 12.12.08
 * @version 01.00
 */
public class PageTag extends ATag {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			PagerTag pagerTag = (PagerTag) TagSupport.findAncestorWithClass(this, PagerTag.class);
			if (pagerTag == null) {
				return EVAL_PAGE;
			}
			Integer page = pagerTag.getPage();
			pagerTag.getBodyContent().print(page);
		} catch (Exception e) {
			logger.error("Exception writing the url to the page", e);
		}
		return EVAL_PAGE;
	}
}
