package ikube.service;

import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.IConstants;
import ikube.cluster.ClusterManager;
import ikube.cluster.cache.Cache;
import ikube.mock.ClusterManagerMock;

import java.net.InetAddress;
import java.net.URL;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebServicePublisherTest extends ATest {

	protected WebServicePublisher webServicePublisher;

	public WebServicePublisherTest() {
		super(WebServicePublisherTest.class);
	}

	@Before
	public void before() throws Exception {
		Mockit.setUpMocks(ClusterManagerMock.class);
		webServicePublisher = new WebServicePublisher(new ClusterManager(new Cache()));
		webServicePublisher.postProcessAfterInitialization(new MonitorWebService(), MonitorWebService.class.getSimpleName());
		webServicePublisher.postProcessAfterInitialization(new SearcherWebService(), SearcherWebService.class.getSimpleName());
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void publish() throws Exception {
		// Verify that the services are published
		String host = InetAddress.getLocalHost().getHostAddress();
		int port = ISearcherWebService.PUBLISHED_PORT;
		String path = ISearcherWebService.PUBLISHED_PATH;
		URL url = new URL("http", host, port, path);
		String searcherWebServiceUrl = url.toString();
		ISearcherWebService webService = ServiceLocator.getService(ISearcherWebService.class, searcherWebServiceUrl,
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
		assertNotNull("The service must be published : ", webService);
	}

	public static void main(String[] args) {
		// http://ikube.dyndns.org:8081/ikube/service/ISearcherWebService?swdl
		String url = "http://192.168.1.17:8081/ikube/service/ISearcherWebService?wsdl";
		ISearcherWebService webService = ServiceLocator.getService(ISearcherWebService.class, url, ISearcherWebService.NAMESPACE,
				ISearcherWebService.SERVICE);
		String[] searchStrings = { "cape town", "cape town" };
		String[] searchFields = { "name" };
		String results = webService.searchSpacialMulti(IConstants.GEOSPATIAL, searchStrings, searchFields, Boolean.TRUE, 0, 10, 15,
				-33.9693580, 18.4622110);
		System.out.println(results);
	}

}
