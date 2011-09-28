package ikube.integration;

import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * TODO Create this test for integration of the web service for searching.
 * 
 * @author Michael Couck
 * @serial 28.09.2011
 * @version 01.00
 */
public class SearchSpatialIntegration {

	private static final Logger	LOGGER	= Logger.getLogger(SearchSpatialIntegration.class);

	@Test
	public void searchSpatial() {
		try {
			ISearcherWebService searchRemote = ServiceLocator.getService(ISearcherWebService.class, "http", "ikube.dyndns.org",
					ISearcherWebService.PUBLISHED_PORT, ISearcherWebService.PUBLISHED_PATH, ISearcherWebService.NAMESPACE,
					ISearcherWebService.SERVICE);
			String indexName = "patientIndex";
			String[] searchStrings = { "162795~" };
			String[] searchFields = { "id" };

			boolean fragment = Boolean.TRUE;
			int firstResult = 0;
			int maxResults = 10;
			int distance = 1000;

			double antwerpLatitude = 51.216667;
			double antwerpLongitude = 4.416667;
			String results = searchRemote.searchSpacialMulti(indexName, searchStrings, searchFields, fragment, firstResult, maxResults, distance,
					antwerpLatitude, antwerpLongitude);
			LOGGER.error(results);

			results = searchRemote.searchMultiAll(indexName, searchStrings, Boolean.TRUE, firstResult, maxResults);
			LOGGER.error(results);
		} catch (Exception e) {
			LOGGER.error("Exception searching remote service : ", e);
		}
	}

}
