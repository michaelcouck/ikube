package ikube.service;

import static org.junit.Assert.assertNotNull;
import ikube.IConstants;

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
		String protocol = "http";
		String host = InetAddress.getLocalHost().getHostAddress();
		int port = ISearcherWebService.PUBLISHED_PORT;
		String path = ISearcherWebService.PUBLISHED_PATH;
		String nameSpace = ISearcherWebService.NAMESPACE;
		String serviceName = ISearcherWebService.SERVICE;
		ISearcherWebService service = ServiceLocator.getService(ISearcherWebService.class, protocol, host, port, path, nameSpace,
				serviceName);
		assertNotNull(service);
	}

	public static void main(String[] args) {
		// http://ikube.dyndns.org:8081/ikube/service/ISearcherWebService?swdl
		String url = "http://192.168.1.17:8081/ikube/service/ISearcherWebService?wsdl";
		ISearcherWebService webService = ServiceLocator.getService(ISearcherWebService.class, url, ISearcherWebService.NAMESPACE,
				ISearcherWebService.SERVICE);
		String[] searchStrings = { "cape town", "cape town" };
		String[] searchFields = { "name", "asciiname" };
		String results = webService.searchSpacialMulti(IConstants.GEOSPATIAL, searchStrings, searchFields, Boolean.TRUE, 0, 10, 15,
				-33.9693580, 18.4622110);
		System.out.println(results);
	}

}
