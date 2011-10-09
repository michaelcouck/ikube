package ikube.web.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;

/**
 * Just prints the context to the page for images and such.
 * 
 * @author Michael Couck
 * @since 19.03.10
 * @version 01.00
 */
@SuppressWarnings("serial")
public class ContextTag extends ATag {

	protected transient Logger	logger	= Logger.getLogger(this.getClass());

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			String context = request.getContextPath();
			pageContext.getOut().print(context);
		} catch (Exception e) {
			logger.error("Exception writing the content out", e);
		}
		return EVAL_PAGE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}

}
