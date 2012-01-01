package ikube.integration;

import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO Create this test for integration of the web service for searching.
 * 
 * @author Michael Couck
 * @serial 28.09.2011
 * @version 01.00
 */
@Ignore
public class SearchSpatialIntegration extends AbstractIntegration {

	@Test
	public void searchSpatial() throws UnknownHostException {
		String ip = InetAddress.getLocalHost().getHostAddress();
		ISearcherWebService searchRemote = ServiceLocator.getService(ISearcherWebService.class, "http", ip, 8081,
				"/ikube/service/SearcherWebService?wsdl", ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
		String indexName = "patientIndex";

		String[] searchStrings = { "2018" }; // , "Antwerpen"
		String[] searchFields = { "postCode" }; // , "province"

		boolean fragment = Boolean.TRUE;
		int firstResult = 0;
		int maxResults = 10;
		int distance = 10;

		double antwerpLatitude = 51.216667;
		double antwerpLongitude = 4.416667;
		ArrayList<HashMap<String, String>> results = searchRemote.searchSpacialMulti(indexName, searchStrings, searchFields, fragment,
				firstResult, maxResults, distance, antwerpLatitude, antwerpLongitude);
		logger.info(results);

		searchRemote.searchMultiAll(indexName, searchStrings, Boolean.TRUE, firstResult, maxResults);
		logger.info(results);
	}

}
