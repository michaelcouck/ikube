package ikube.web.tag;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.toolkit.Logging;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;

import mockit.Cascading;
import mockit.Mockit;

import org.apache.log4j.Logger;
import org.junit.Before;

/**
 * TODO Comment me.
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
public abstract class ATagTest {

	static {
		Logging.configure();
		Mockit.setUpMocks();
	}

	/** TODO When the server is running then this can change to the static Ikube instance. */
	protected static String searchUrl = "http://ikube.ikube.cloudbees.net/SearchServlet?indexName=ikube&searchStrings=lucene";

	protected Logger logger = Logger.getLogger(this.getClass());

	@Cascading()
	protected PagerTag pagerTag;
	protected PageContext pageContext = mock(PageContext.class);
	protected JspWriter jspWriter = mock(JspWriter.class);
	protected JspWriter writer = mock(JspWriter.class);
	protected BodyContent bodyContent = mock(BodyContent.class);
	protected HttpServletRequest request = mock(HttpServletRequest.class);
	protected HttpSession httpSession = mock(HttpSession.class);

	protected String baseUrl = "http://www.ikokoon.eu/search-war-1.0";

	protected int maxResults = 10;
	protected int totalResults = 101;
	protected String aParameter = "aParameter";
	protected String aParameterValue = "aParameterValue";
	protected String anotherParameter = "anotherParameter";
	protected String anotherParameterValue = "anotherParameterValue";

	@Before
	public void before() throws Exception {
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		parameterMap.put(aParameter, new String[] { aParameterValue });
		parameterMap.put(anotherParameter, new String[] { anotherParameterValue });

		when(request.getParameterMap()).thenReturn(parameterMap);
		when(request.getSession()).thenReturn(httpSession);

		when(httpSession.getAttribute(ATag.TOTAL)).thenReturn(totalResults);
		when(httpSession.getAttribute(ATag.MAX_RESULTS)).thenReturn(maxResults);

		when(pageContext.getRequest()).thenReturn(request);
		when(pageContext.getOut()).thenReturn(jspWriter);
	}

	protected String getResultsXml() {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		Map<String, String> result = new HashMap<String, String>();
		result.put("index", "1");
		result.put("id", "1");
		result.put("title", "title");
		result.put("score", "1.0");
		result.put("fragment", "fragment");
		results.add(result);

		Map<String, String> statistics = new HashMap<String, String>();
		statistics.put(ATag.TOTAL, Long.toString(10001));
		statistics.put(ATag.DURATION, Long.toString(10001));
		results.add(statistics);
		return serialize(results);
	}

	public String serialize(Object object) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(byteArrayOutputStream);
		xmlEncoder.writeObject(object);
		xmlEncoder.flush();
		xmlEncoder.close();
		try {
			return byteArrayOutputStream.toString(ATag.ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error("Unsupported encoding", e);
		}
		return null;
	}

}