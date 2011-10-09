package ikube.web.tag;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;

import org.apache.log4j.Logger;

/**
 * TODO Document me!
 * 
 * @author Michael Couck
 * @since 04.02.10
 * @version 01.00
 */
@SuppressWarnings("serial")
public class SearchTag extends ATag {

	protected transient Logger	logger	= Logger.getLogger(this.getClass());
	private String				searchUrl;

	public void setSearchUrl(String searchUrl) {
		this.searchUrl = searchUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		HttpSession session = request.getSession();
		// Check to see if there are already results in the session
		List<Map<String, String>> results = (List<Map<String, String>>) session.getAttribute(RESULTS);
		try {
			if (results == null) {
				results = doSearch(request);
			}
			if (results != null) {
				// Add the list to the page context/parameters/session
				session.setAttribute(RESULTS, results);
				if (results.size() > 0) {
					Map<String, String> statistics = results.get(results.size() - 1);
					String stringTotal = statistics.get(TOTAL);
					String stringDuration = statistics.get(DURATION);
					session.setAttribute(TOTAL, stringTotal != null ? Integer.parseInt(stringTotal) : 0);
					session.setAttribute(DURATION, stringDuration != null ? Integer.parseInt(stringDuration) : 0);
					results.remove(statistics);
				} else {
					session.setAttribute(TOTAL, 0);
					session.setAttribute(DURATION, 0);
				}
			}
		} catch (Exception e) {
			logger.error("Exception accessing the search servlet.", e);
		}
		return EVAL_BODY_BUFFERED;
	}

	/**
	 * {@inheritDoc}
	 */
	public int doAfterBody() {
		return EVAL_PAGE;
	}

	/**
	 * {@inheritDoc}
	 */
	public int doEndTag() throws JspException {
		try {
			BodyContent body = getBodyContent();
			if (body != null) {
				JspWriter out = body.getEnclosingWriter();
				out.print(body.getString());
			}
		} catch (IOException e) {
			logger.error("Exception writing the content out", e);
		}
		return EVAL_PAGE;
	}

	/**
	 * This method should be called if the search servlet is being used and there are no results in the request or the
	 * session.
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected List<Map<String, String>> doSearch(HttpServletRequest request) throws IOException {
		HttpSession session = request.getSession();
		// Set the defaults in the session
		Number firstResult = request.getParameter(FIRST_RESULT) != null ? Integer.parseInt(request.getParameter(FIRST_RESULT)) : 0;
		Number maxResults = request.getParameter(MAX_RESULTS) != null ? Integer.parseInt(request.getParameter(MAX_RESULTS)) : 10;
		session.setAttribute(RESULTS, null);
		session.setAttribute(TOTAL, 0);
		session.setAttribute(FIRST_RESULT, firstResult);
		session.setAttribute(MAX_RESULTS, maxResults);
		// Build the url to the search servlet
		StringBuilder builder = new StringBuilder();
		String queryString = request.getQueryString();
		if (queryString != null) {
			if (searchUrl.indexOf('?') == -1) {
				builder.append('?');
			} else {
				builder.append('&');
			}
			builder.append(queryString);
		}
		URL url = new URL(searchUrl + builder.toString());
		// Access the search servlet and get the results in xml serialized form
		InputStream inputStream = url.openStream();
		String xml = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
		List<Map<String, String>> results = null;
		if (xml != null && !xml.trim().equals("")) {
			results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}

}
