package ikube.service;

import static org.junit.Assert.assertNotNull;
import ikube.BaseTest;

import java.net.InetAddress;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class ServiceLocatorTest extends BaseTest {

	public ServiceLocatorTest() {
		super(ServiceLocatorTest.class);
	}

	@Test
	public void getService() throws Exception {
		String protocol = "http";
		String host = InetAddress.getLocalHost().getHostAddress();
		int port = 8081;
		String path = ISearcherWebService.PUBLISHED_PATH;
		String nameSpace = ISearcherWebService.NAMESPACE;
		String serviceName = ISearcherWebService.SERVICE;
		ISearcherWebService service = ServiceLocator.getService(ISearcherWebService.class, protocol, host, port, path, nameSpace,
				serviceName);
		assertNotNull(service);
	}

}
