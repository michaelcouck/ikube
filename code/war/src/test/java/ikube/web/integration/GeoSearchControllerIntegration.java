package ikube.web.integration;

import ikube.IConstants;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;

import java.net.InetAddress;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class GeoSearchControllerIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeoSearchControllerIntegration.class);

	@Test
	public void doSearch() throws Exception {
		int port = 8081;
		String path = "/ikube/service/SearcherWebService?wsdl";
		String host = InetAddress.getLocalHost().getHostAddress();
		URL url = new URL("http", host, port, path);
		String searcherWebServiceUrl = url.toString();
		ISearcherWebService webService = ServiceLocator.getService(ISearcherWebService.class, searcherWebServiceUrl,
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);

		String indexName = IConstants.GEOSPATIAL;
		String[] searchStrings = new String[] { "berlin", "berlin", "germany", "berlin" };
		String[] searchFields = new String[] { IConstants.ASCIINAME, IConstants.CITY, IConstants.COUNTRY, IConstants.NAME };
		boolean fragment = Boolean.TRUE;
		int firstResult = 0;
		int maxResults = 10;
		int distance = 20;
		double latitude = 52.52274;
		double longitude = 13.4166;

		// String results = webService.searchSpacialMulti(indexName, searchStrings, searchFields, fragment, 0, 10, distance, latitude,
		// longitude);
		// LOGGER.info("Results : " + results);
		//
		// results = webService.searchMulti(indexName, searchStrings, searchFields, fragment, firstResult, maxResults);
		// LOGGER.info("Results : " + results);
		//
		// String stringResults = webService.searchMulti(indexName, searchStrings, searchFields, fragment, firstResult, maxResults);
		// LOGGER.info("Results : " + stringResults);
		//
		// stringResults = webService.searchSpacialMulti(indexName, searchStrings, searchFields, fragment, firstResult, maxResults,
		// distance,
		// latitude, longitude);
		// LOGGER.info("Results : " + stringResults);
		// First log in
		// Go to the GeoSpatial search page
		// Do a search
		// Verify the results in the page
	}

}
