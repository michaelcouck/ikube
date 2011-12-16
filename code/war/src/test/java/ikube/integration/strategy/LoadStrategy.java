package ikube.integration.strategy;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ikube.IConstants;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.PerformanceTester;

import java.net.InetAddress;
import java.net.URL;

import org.apache.log4j.Logger;

public class LoadStrategy implements IStrategy {

	private static final Logger LOGGER = Logger.getLogger(LoadStrategy.class);

	private int iterations;
	private double minimumSearchesPerSecond;

	public LoadStrategy(final int iterations, final double minimumSearchesPerSecond) {
		this.iterations = iterations;
		this.minimumSearchesPerSecond = minimumSearchesPerSecond;
	}

	@Override
	public void perform() throws Exception {
		String host = InetAddress.getLocalHost().getHostAddress();
		int port = 8081;
		String path = "/ikube/service/SearcherWebService?wsdl";
		URL url = new URL("http", host, port, path);
		String searcherWebServiceUrl = url.toString();
		final ISearcherWebService searcherWebService = ServiceLocator.getService(ISearcherWebService.class, searcherWebServiceUrl,
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
		final String[] searchStrings = { "cape AND town" };
		final String[] searchFields = { "name" };
		final boolean fragment = Boolean.TRUE;
		final int firstResult = 0;
		final int maxResults = 10;
		final int distance = 10;
		final double latitude = -33.9693580;
		final double longitude = 18.4622110;
		LOGGER.info("Executing load test : " + searcherWebServiceUrl);
		if (searcherWebService == null) {
			fail("The searcher web service is not available : ");
		}
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				searcherWebService.searchSpacialMulti(IConstants.GEOSPATIAL, searchStrings, searchFields, fragment, firstResult,
						maxResults, distance, latitude, longitude);
			}
		}, "Load test of the web service : ", iterations, Boolean.TRUE);
		LOGGER.info("Finished load test : " + iterations + ", " + searcherWebServiceUrl);
		assertTrue("This is the minimum searches per second : ", executionsPerSecond > minimumSearchesPerSecond);
	}

}
