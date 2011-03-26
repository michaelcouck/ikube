package ikube.web.tag;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
			// Access the search servlet and get the results in xml serialised form
			InputStream inputStream = url.openStream();
			String xml = getContents(inputStream).toString();
			logger.debug("Results xml : " + xml);

			if (xml != null && !xml.trim().equals("")) {
				// Decode the xml into a list of maps
				ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) deserialize(xml);
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

	protected Object deserialize(String xml) {
		byte[] bytes = new byte[0];
		try {
			bytes = xml.getBytes(ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error("Unsupported encoding", e);
		}
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		XMLDecoder xmlDecoder = new XMLDecoder(byteArrayInputStream);
		return xmlDecoder.readObject();
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
	 * Reads the contents of the file and returns the contents in a byte array form.
	 * 
	 * @param inputStream
	 *            the file to read the contents from
	 * @return the stream contents in a byte array output stream
	 * @throws Exception
	 */
	protected ByteArrayOutputStream getContents(InputStream inputStream) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (inputStream == null) {
			return bos;
		}
		try {
			byte[] bytes = new byte[1024];
			int read;
			while ((read = inputStream.read(bytes)) > -1) {
				bos.write(bytes, 0, read);
			}
		} catch (Exception e) {
			logger.error("Exception accessing the stream contents", e);
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				logger.error("Exception closing input stream " + inputStream, e);
			}
		}
		return bos;
	}

	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}

}
