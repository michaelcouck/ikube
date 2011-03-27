package ikube.web.tag;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
 * @author Michael Couck
 * @since 04.02.10
 * @version 01.00
 */
public class SearchTag extends ATag {

	protected Logger logger = Logger.getLogger(this.getClass());

	private String searchUrl;

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
		// Set the defaults in the session
		Number firstResult = request.getParameter(FIRST_RESULT) != null ? Integer.parseInt(request.getParameter(FIRST_RESULT)) : 0;
		Number maxResults = request.getParameter(MAX_RESULTS) != null ? Integer.parseInt(request.getParameter(MAX_RESULTS)) : 10;

		session.setAttribute(RESULTS, null);
		session.setAttribute(TOTAL, 0);
		session.setAttribute(FIRST_RESULT, firstResult);
		session.setAttribute(MAX_RESULTS, maxResults);

		StringBuilder builder = new StringBuilder();
		if (searchUrl.indexOf('?') == -1) {
			builder.append('?');
		} else {
			builder.append('&');
		}
		builder.append(request.getQueryString());

		logger.debug(request.getQueryString());
		logger.debug("Do start tag : " + builder.toString());
		// Build the url to the search servlet
		try {
			URL url = new URL(searchUrl + builder.toString());
			logger.debug("Search URL : " + url);
			// Access the search servlet and get the results in xml serialized form
			InputStream inputStream = url.openStream();
			String xml = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
			logger.debug("Results xml : " + xml);

			if (xml != null && !xml.trim().equals("")) {
				// logger.info("Xml results : " + xml);
				// Decode the xml into a list of maps
				List<Map<String, String>> results = null;
				try {
					results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
				} catch (Exception e) {
					logger.warn("No results?", e);
				}
				if (results != null) {
					// Add the list to the page context/parameters/session
					session.setAttribute(RESULTS, results);
					if (results.size() > 0) {
						logger.debug("Results in tag : " + results.toString());
						Map<String, String> statistics = results.get(results.size() - 1);
						String stringTotal = statistics.get(TOTAL);
						String stringDuration = statistics.get(DURATION);
						session.setAttribute(TOTAL, Integer.parseInt(stringTotal));
						session.setAttribute(DURATION, Integer.parseInt(stringDuration));
						results.remove(statistics);
					} else {
						session.setAttribute(TOTAL, 0);
						session.setAttribute(DURATION, 0);
					}
				}
			}
		} catch (MalformedURLException e) {
			logger.error("Exception accessing the search servlet with the query string : " + builder, e);
		} catch (IOException e) {
			logger.error("Exception accessing the search servlet. Is the server running, configuration?", e);
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

	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}

}
