package ikube.service;

import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

public class WebServicePublisherTest extends ATest {

	public WebServicePublisherTest() {
		super(WebServicePublisherTest.class);
	}

	@Test
	public void publish() throws Exception {
		ApplicationContextManager.getApplicationContext();
		// Verify that the services are published
		String searcherWebServiceUrl = "http://192.168.56.1:8081/ikube/service/ISearcherWebService?wsdl";
		String monitoringWebServiceUrl = "http://192.168.56.1:8082/ikube/service/IMonitoringService?wsdl ";
		ISearcherWebService webService = ServiceLocator.getService(ISearcherWebService.class, searcherWebServiceUrl,
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
		assertNotNull("The service must be published : ", webService);
		IMonitoringService monitoringWebService = ServiceLocator.getService(IMonitoringService.class, monitoringWebServiceUrl,
				IMonitoringService.NAMESPACE, IMonitoringService.SERVICE);
		assertNotNull("The service must be published : ", monitoringWebService);
	}

}
