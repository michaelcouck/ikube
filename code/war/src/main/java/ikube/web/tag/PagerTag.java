package ikube.web.tag;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;

import org.apache.log4j.Logger;

/**
 * This is the pager tag for any application. It could be reused for other applications. What is required for this tag to function are three
 * parameters that should be set in the session, the names of the attributes in the session are in this class:<br>
 * 
 * 1) total - The total count for all the results as a Number<br>
 * 2) firstResult - The index of the first result that you will need.<br>
 * 3) maxResults - The maximum results that should be shown.<br>
 * 
 * All the parameters from the request are added to the url. Urls are generated for each page. So for example if you have results from 0 to
 * 17 and the max results that you want to see on the page are 10 and the action for your url is /myapp/myaction.action then the generated
 * urls will be the following:<br>
 * 
 * 1) url=/myapp/myaction.action?firstResult=0&maxResults=10, page=1<br>
 * 2) url=/myapp/myaction.action?firstResult=10&maxResults=10, page=2<br>
 * 
 * The link tag can then access the next url in the list and print it to the page. The page tag can access the page number and print that to
 * the page. Nesting the tags should look something like:<br>
 * 
 * <pre>
 * 		Show items per &lt;ccff:pagerTag&gt;&lt;a href=&quot;&lt;ccff:linkTag /&gt;&quot;&gt;&lt;ccff:pageTag /&gt;&lt;/a&gt; &lt;/ccff:pagerTag&gt;
 * </pre>
 * 
 * 
 * @author Michael Couck
 * @since 12.12.08
 * @version 01.00
 */
public class PagerTag extends ATag {

	protected Logger logger = Logger.getLogger(this.getClass());

	/** The url to the search page. */
	private String searchUrl;
	/** The next url in the list. */
	private Url url;
	/** The list of generated urls. */
	private LinkedList<Url> urls;

	public void setSearchUrl(String searchUrl) {
		this.searchUrl = searchUrl;
		logger.debug("Search url : " + searchUrl);
	}

	/**
	 * {@inheritDoc}
	 */
	public int doStartTag() throws JspException {
		urls = new LinkedList<Url>();
		try {
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			String baseUrl = buildUrl(request);
			buildUrls(request, baseUrl);
		} catch (MalformedURLException e) {
			logger.error("Exception generating the url? Configuration problem?", e);
			return SKIP_BODY;
		}
		if (urls == null || urls.size() == 0) {
			return SKIP_BODY;
		}
		url = urls.getFirst();
		return EVAL_BODY_BUFFERED;
	}

	/**
	 * {@inheritDoc}
	 */
	public int doAfterBody() {
		if (url != null) {
			int index = urls.lastIndexOf(url);
			if (++index < urls.size() && index < MAX_PAGED_RESULTS) {
				url = urls.get(index);
				return EVAL_BODY_AGAIN;
			}
		}
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
	 * Access to the next url for the children.
	 * 
	 * @return the real url for the next block of results
	 */
	protected String getUrl() {
		return url.url;
	}

	/**
	 * Access to the page item for the next block of results.
	 * 
	 * @return the page number for the next block of results
	 */
	protected Integer getPage() {
		return url.page;
	}

	/**
	 * This method builds the url for the next page. We take all the parameters from the request and add them to the url. This forms the
	 * base url for the paging.
	 * 
	 * @param request
	 *            the request to access the parameters from
	 * @return the base url that will be used to generate the paging urls
	 * @throws MalformedURLException
	 */
	@SuppressWarnings("unchecked")
	protected String buildUrl(HttpServletRequest request) throws MalformedURLException {
		StringBuilder builder = new StringBuilder();
		// Add the action to the path
		builder.append(searchUrl);
		builder.append("?");
		// Get all the parameters and add them to the url
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			Object parameter = request.getParameter(name);
			// We don't want to add the results parameters to the url yet
			if (name.equals(MAX_RESULTS) || name.equals(FIRST_RESULT)) {
				continue;
			}
			builder.append(name);
			builder.append("=");
			builder.append(parameter);
			if (names.hasMoreElements()) {
				builder.append("&");
			}
		}
		// We end up with: /search/search.html?name=name&searchString=searchString
		return builder.toString();
	}

	/**
	 * Builds the paging urls. In the above method the base url is built, this method builds several urls each one increments the first
	 * result parameter, effectively setting the scene for the next page. In your action you then access the first result and max results
	 * parameters and do your paging logic to ensure that you have the next set of results for your page.
	 * 
	 * @param request
	 *            the request to get the session from
	 * @param baseUrl
	 *            the base url that should already be built
	 * @throws JspException
	 */
	protected void buildUrls(HttpServletRequest request, String baseUrl) throws JspException {
		HttpSession session = request.getSession();
		// Get the total results and the max results from the session. These must already have been set by the action of course
		Number total = (Number) session.getAttribute(TOTAL);
		Number maxResults = (Number) session.getAttribute(MAX_RESULTS);
		if (total == null || maxResults == null) {
			logger.error("No total and max result set in the session");
			return;
		}
		for (int resultIndex = 0; resultIndex < total.intValue(); resultIndex++) {
			if (resultIndex % maxResults.intValue() == 0) {
				// We start with: /search/search.html?name=name&searchString=searchString
				StringBuilder builder = new StringBuilder(baseUrl);
				if (!baseUrl.endsWith("&")) {
					builder.append("&");
				}
				builder.append(FIRST_RESULT);
				builder.append("=");
				builder.append(resultIndex);
				builder.append("&");
				builder.append(MAX_RESULTS);
				builder.append("=");
				builder.append(maxResults);
				Url url = new Url(builder.toString(), resultIndex);
				// We end up with:
				// /search/search.html?name=name&searchString=searchString&firstResult=firstResult&maxResults=maxResults
				urls.add(url);
			}
		}
	}

}
