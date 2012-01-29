package ikube.service;

import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;

import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebServicePublisherTest extends ATest {

	private int port = 8081;
	private WebServicePublisher webServicePublisher;
	private String path = "/ikube/service/SearcherWebService?wsdl";
	private SearcherWebService searcherWebService;

	public WebServicePublisherTest() {
		super(WebServicePublisherTest.class);
	}

	@Before
	public void before() throws Exception {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
		webServicePublisher = new WebServicePublisher();
		searcherWebService = new SearcherWebService();
		Deencapsulation.setField(searcherWebService, port);
		Deencapsulation.setField(searcherWebService, path);
		Deencapsulation.setField(webServicePublisher, Arrays.asList(searcherWebService));
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void publish() throws Exception {
		webServicePublisher.publish();
		// Verify that the services are published
		String host = InetAddress.getLocalHost().getHostAddress();
		URL url = new URL("http", host, port, path);
		String searcherWebServiceUrl = url.toString();
		ISearcherWebService webService = ServiceLocator.getService(ISearcherWebService.class, searcherWebServiceUrl,
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
		assertNotNull("The service must be published : ", webService);
	}

}
