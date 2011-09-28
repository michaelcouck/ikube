package ikube.integration;

import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.Logging;

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

	static {
		Logging.configure();
	}

	private static final Logger	LOGGER	= Logger.getLogger(SearchSpatialIntegration.class);

	@Test
	public void searchSpatial() {
		try {
			ISearcherWebService searchRemote = ServiceLocator.getService(ISearcherWebService.class, "http", "ikube.dyndns.org",
					ISearcherWebService.PUBLISHED_PORT, ISearcherWebService.PUBLISHED_PATH, ISearcherWebService.NAMESPACE,
					ISearcherWebService.SERVICE);
			String indexName = "patientIndex";

			// String[] searchStrings = { "16279506~" };
			// String[] searchFields = { "id" };

			// String[] searchStrings = { "capitoline" };
			// String[] searchFields = { "lastName" };

			String[] searchStrings = { "2018" }; // , "Antwerpen"
			String[] searchFields = { "postCode" }; // , "province"

			boolean fragment = Boolean.TRUE;
			int firstResult = 0;
			int maxResults = 10;
			int distance = 10;

			double antwerpLatitude = 51.216667;
			double antwerpLongitude = 4.416667;
			String results = searchRemote.searchSpacialMulti(indexName, searchStrings, searchFields, fragment, firstResult, maxResults, distance,
					antwerpLatitude, antwerpLongitude);
			LOGGER.error(results);

			results = searchRemote.searchMultiAll(indexName, searchStrings, Boolean.TRUE, firstResult, maxResults);
			// LOGGER.error(results);
		} catch (Exception e) {
			LOGGER.error("Exception searching remote service : ", e);
		}
	}

}
