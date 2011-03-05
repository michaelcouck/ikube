package ikube.web.servlet;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.search.Search;
import ikube.search.SearchSingle;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.SerializationUtilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class SearchServlet extends HttpServlet {

	private Logger logger = Logger.getLogger(this.getClass());
	private String contentType = "text/html";

	@Override
	public void init() throws ServletException {
		ApplicationContextManager.getApplicationContext();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// String indexName, String searchString, String searchField, boolean fragment, int firstResult, int maxResults
		res.setContentType(contentType);
		PrintWriter out = res.getWriter();
		try {
			String indexName = req.getParameter(IConstants.INDEX_NAME);
			String searchStrings = req.getParameter(IConstants.SEARCH_STRINGS);
			String searchFields = req.getParameter(IConstants.SEARCH_FIELDS);
			String sortFields = req.getParameter(IConstants.SORT_FIELDS);
			int firstResult = Integer.parseInt(req.getParameter(IConstants.FIRST_RESULT));
			int maxResults = Integer.parseInt(req.getParameter(IConstants.MAX_RESULTS));
			boolean fragment = Boolean.parseBoolean(req.getParameter(IConstants.FRAGMENT));
			Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
			for (IndexContext indexContext : indexContexts.values()) {
				if (indexContext.getIndexName().equals(indexName)) {
					if (indexContext.getIndex().getMultiSearcher() != null) {
						Search search = new SearchSingle(indexContext.getIndex().getMultiSearcher());
						search.setFirstResult(firstResult);
						search.setFragment(fragment);
						search.setMaxResults(maxResults);
						search.setSearchField(searchFields);
						search.setSearchString(searchStrings);
						search.setSortField(sortFields);
						List<Map<String, String>> results = search.execute();
						String xml = SerializationUtilities.serialize(results);
						logger.info("Results : " + xml);
						out.print(xml);
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception doing search : " + req.getParameterMap(), e);
		}
	}

}