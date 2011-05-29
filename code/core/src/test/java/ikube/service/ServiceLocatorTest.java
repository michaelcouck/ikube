package ikube.service;

import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;

import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO This test can be removed as the web service publisher test has to test that the services are published, and to see that they are
 * published uses the service locator, which means that the service locator must be operating properly as well.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@Ignore
public class ServiceLocatorTest extends AServiceTest {

	public ServiceLocatorTest() {
		super(ServiceLocatorTest.class);
	}

	@Test
	public void getService() throws Exception {
		webServicePublisher.publish();
		String protocol = "http";
		String host = InetAddress.getLocalHost().getHostAddress();
		String path = ISearcherWebService.PUBLISHED_PATH;
		String nameSpace = ISearcherWebService.NAMESPACE;
		String serviceName = ISearcherWebService.SERVICE;
		ISearcherWebService service = ServiceLocator.getService(ISearcherWebService.class, protocol, host, port, path, nameSpace,
				serviceName);
		assertNotNull(service);
	}

}
