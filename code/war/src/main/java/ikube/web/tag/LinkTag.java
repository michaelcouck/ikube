package ikube.web.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

/**
 * This tag is nested in the pager tag. Each iteration over the pager tag a url is made available to the children. This tag then accesses
 * the next url from the pager tag and prints it to the page.
 * 
 * @author Michael Couck
 * @since 12.12.08
 * @version 01.00
 */
public class LinkTag extends ATag {

	protected transient Logger logger = Logger.getLogger(this.getClass());

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			// Get the pager tag
			PagerTag pagerTag = (PagerTag) TagSupport.findAncestorWithClass(this, PagerTag.class);
			if (pagerTag == null) {
				return EVAL_PAGE;
			}
			// Get the next url
			String url = pagerTag.getUrl();
			// Print to the parent body
			pagerTag.getBodyContent().print(url);
		} catch (IOException e) {
			logger.error("Exception writing the url to the page", e);
		}
		return EVAL_PAGE;
	}

}
