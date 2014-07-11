package ikube.integration.strategy;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.web.servlet.SearchServlet;

import java.net.URL;
import java.net.URLEncoder;

@SuppressWarnings("deprecation")
public class ServletStrategy extends AStrategy {

	private int iterations;
	private String[] indexNames;

	public ServletStrategy(String context, int port, int iterations, String... indexNames) {
		super(context, port);
		this.iterations = iterations;
		this.indexNames = indexNames;
	}

	@Override
	public void perform() throws Exception {
		String host = "localhost";
		for (String indexName : indexNames) {
			final StringBuilder urlString = new StringBuilder("http://");
			urlString.append(host);
			urlString.append(":");
			urlString.append(port);
			urlString.append(context);
			urlString.append(IConstants.SEP + SearchServlet.class.getSimpleName());

			// Add the index name and index fields with come words
			// http://192.168.1.109:80/ikube/SearchServlet?indexName=index&searchString=ikube
			String searchString = "quick brown fox jumped over the lazy dog";

			urlString.append("?");
			urlString.append(IConstants.INDEX_NAME);
			urlString.append("=");
			urlString.append(indexName);
			urlString.append("&");
			urlString.append(IConstants.SEARCH_STRINGS);
			urlString.append("=");
			urlString.append(URLEncoder.encode(searchString, IConstants.ENCODING));

			logger.info("Executing servlet : " + urlString);

			double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
				public void execute() throws Exception {
					URL url = new URL(urlString.toString());
					String content = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
					assertNotNull(content);
				}
			}, "Servlet performance test : ", iterations, Boolean.FALSE);

			assertTrue(executionsPerSecond > 3);

		}
	}

}
