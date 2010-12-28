package ikube.service;

import static org.junit.Assert.assertNotNull;
import ikube.BaseTest;

import java.net.InetAddress;

import org.junit.Test;

public class ServiceLocatorTest extends BaseTest {

	@Test
	public void getService() throws Exception {
		String protocol = "http";
		String host = InetAddress.getLocalHost().getHostAddress();
		int port = 8081;
		String path = "/service/ISearcherWebService?wsdl";
		String nameSpace = ISearcherWebService.NAMESPACE;
		String serviceName = ISearcherWebService.SERVICE;
		ISearcherWebService service = ServiceLocator.getService(ISearcherWebService.class, protocol, host, port, path, nameSpace,
				serviceName);
		assertNotNull(service);
	}

}
