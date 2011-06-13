package ikube.web.tag.spelling;

import ikube.web.tag.ATag;

import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * TODO Comment me!
 * 
 * @author Michael Couck
 * @since 10.02.10
 * @version 01.00
 */
public class SpellingTag extends ATag {

	protected Logger logger = Logger.getLogger(this.getClass());
	private static CheckerExt checker = new CheckerExt();
	/** The field parameter names are the fields in the index being searched. */
	private String[] fieldParameterNames;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int doStartTag() {
		try {
			String searchString = null;
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			Set<String> fieldParameterValues = new TreeSet<String>();
			if (fieldParameterNames != null) {
				// First try all the fields defined in the Jsp. These are the fields that were
				// used to do the search on the index, we concatenate all these, removing the
				// duplicates of course
				for (String fieldParameterName : fieldParameterNames) {
					String fieldParameterValue = request.getParameter(fieldParameterName);
					if (fieldParameterValue != null && !"".equals(fieldParameterValue)) {
						boolean added = fieldParameterValues.add(fieldParameterValue);
						if (!added) {
							logger.debug("Duplicate string : " + fieldParameterValue);
						}
					}
				}
				searchString = fieldParameterValues.toString();
				searchString = StringUtils.strip(searchString, "[]{},");
			} else {
				// No fields defined in the Jsp so we'll try the default fields
				searchString = request.getParameter(SEARCH_STRINGS);
				if (searchString == null) {
					searchString = request.getParameter(CONTENT);
				}
			}
			// logger.info("Search strings : " + searchString);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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

	public void setFieldParameterNames(String fieldParameterNames) {
		this.fieldParameterNames = StringUtils.split(fieldParameterNames, ",");
	}

}
