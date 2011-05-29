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
		webServicePublisher.publish();
		// Verify that the services are published
		String searcherWebServiceUrl = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port
				+ "/ikube/service/ISearcherWebService?wsdl";
		ISearcherWebService webService = ServiceLocator.getService(ISearcherWebService.class, searcherWebServiceUrl,
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
		webService.setSearchDelegate(new SearchDelegate());
		assertNotNull("The service must be published : ", webService);
	}

}
