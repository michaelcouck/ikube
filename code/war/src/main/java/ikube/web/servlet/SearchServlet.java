package ikube.web.servlet;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.search.Search;
import ikube.search.SearchMulti;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.SerializationUtilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Persistence;
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
		Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_NAME).createEntityManager();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// String indexName, String searchString, String searchField, boolean fragment, int firstResult, int maxResults
		response.setContentType(contentType);
		PrintWriter out = response.getWriter();
		String indexName = null;
		try {
			indexName = request.getParameter(IConstants.INDEX_NAME);
			String searchStrings = getParameter(request, IConstants.SEARCH_STRINGS, "default");
			String searchFields = getParameter(request, IConstants.SEARCH_FIELDS, "content");
			String sortFields = getParameter(request, IConstants.SORT_FIELDS, "sortFields");
			int firstResult = Integer.parseInt(getParameter(request, IConstants.FIRST_RESULT, "0"));
			int maxResults = Integer.parseInt(getParameter(request, IConstants.MAX_RESULTS, "10"));
			boolean fragment = Boolean.parseBoolean(getParameter(request, IConstants.FRAGMENT, "true"));
			Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
			for (IndexContext indexContext : indexContexts.values()) {
				if (indexContext.getIndexName().equals(indexName)) {
					if (indexContext.getIndex().getMultiSearcher() != null) {
						Search search = getSearch(indexContext, indexName, searchStrings, searchFields, sortFields, firstResult,
								maxResults, fragment);
						List<Map<String, String>> results = search.execute();
						String xml = SerializationUtilities.serialize(results);
						out.print(xml);
						return;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception doing search : " + getParameters(request), e);
			try {
				out.print("Exception searching with parameters : " + getParameters(request));
			} catch (Exception ex) {
				logger.error("Exception writing the exception message to the client : " + getParameters(request), ex);
			}
		}

		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		Map<String, String> notification = new HashMap<String, String>();
		notification.put(IConstants.ID, "No index defined for name : " + indexName);
		notification.put(IConstants.CONTENTS, "There was no index found, or the index was not built or an exception was thrown.");
		notification.put(IConstants.SCORE, "Are the parameters correct?" + getParameters(request));
		results.add(notification);
		out.print(SerializationUtilities.serialize(results));
	}

	@SuppressWarnings("unchecked")
	private String getParameters(HttpServletRequest request) {
		StringBuilder builder = new StringBuilder();
		Map<String, String[]> parameters = request.getParameterMap();
		for (String name : parameters.keySet()) {
			builder.append("[");
			builder.append(name);
			builder.append(":");
			builder.append(Arrays.asList(parameters.get(name)));
			builder.append("]");
			builder.append("<br>");
		}
		return builder.toString();
	}

	private Search getSearch(IndexContext indexContext, String indexName, String searchStrings, String searchFields, String sortFields,
			int firstResult, int maxResults, boolean fragment) {
		Search search = new SearchMulti(indexContext.getIndex().getMultiSearcher());
		search.setFirstResult(firstResult);
		search.setFragment(fragment);
		search.setMaxResults(maxResults);
		search.setSearchField(searchFields);
		search.setSearchString(searchStrings);
		search.setSortField(sortFields);
		return search;
	}

	private String getParameter(HttpServletRequest request, String name, String defaultValue) {
		return request.getParameter(name) == null ? defaultValue : request.getParameter(name);
	}

}