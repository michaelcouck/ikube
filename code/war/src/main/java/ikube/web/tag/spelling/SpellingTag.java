package ikube.web.tag.spelling;

import ikube.web.tag.ATag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 10.02.10
 * @version 01.00
 */
public class SpellingTag extends ATag {

	protected Logger logger = Logger.getLogger(this.getClass());
	private static CheckerExt checker = new CheckerExt();

	public int doStartTag() {
		try {
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			String searchString = request.getParameter(SEARCH_STRING);
			if (searchString == null) {
				return SKIP_BODY;
			}

			String checkedSearchString = (String) pageContext.getAttribute(CHECKED_SEARCH_STRING);
			if (checkedSearchString == null) {
				checkedSearchString = checker.checkWords(searchString);
				if (checkedSearchString == null) {
					return SKIP_BODY;
				}
				pageContext.setAttribute(CHECKED_SEARCH_STRING, checkedSearchString);
			}

			return EVAL_BODY_INCLUDE;
		} catch (Exception e) {
			logger.error("Exception writing the content out", e);
		}
		return SKIP_BODY;
	}

	public int doEndTag() throws JspException {
		if (this.bodyContent != null) {
			try {
				this.bodyContent.writeOut(getPreviousOut());
			} catch (Exception e) {
				logger.error("Exception writing to the page", e);
			}
		}
		return EVAL_PAGE;
	}

}
