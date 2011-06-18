package ikube.service;

import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;

import org.junit.Test;

public class WebServicePublisherTest extends AServiceTest {

	public WebServicePublisherTest() {
		super(WebServicePublisherTest.class);
	}

	@Test
	public void publish() throws Exception {
		// Verify that the services are published
		int port = ISearcherWebService.PUBLISHED_PORT;
		String searcherWebServiceUrl = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port
				+ ISearcherWebService.PUBLISHED_PATH;
		ISearcherWebService webService = ServiceLocator.getService(ISearcherWebService.class, searcherWebServiceUrl,
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
		assertNotNull("The service must be published : ", webService);
	}

}
