package ikube.service;

import ikube.IConstants;

import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

public class ServiceLocatorTest {

	@Test
	@Ignore
	public void search() throws Exception {
		ServiceLocatorTest.main(new String[0]);
	}

	public static void main(String[] args) throws Exception {
		// http://192.168.1.39:8084/ikube/service/ISearcherWebService?wsdl
		String host = "192.168.1.39"; // "81.82.213.177" ; // InetAddress.getLocalHost().getHostAddress();
		// int port = ISearcherWebService.PUBLISHED_PORT;
		String path = ISearcherWebService.PUBLISHED_PATH;
		int[] ports = { 8081 };
		String[] indexNames = { IConstants.GEOSPATIAL /* , IConstants.IKUBE, IConstants.DEFAULT */};
		for (int port : ports) {
			URL url = new URL("http", host, port, path);
			String searcherWebServiceUrl = url.toString();
			ISearcherWebService searcherWebService = ServiceLocator.getService(ISearcherWebService.class, searcherWebServiceUrl,
					ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
			if (searcherWebService != null) {
				String[] searchStrings = { "cape AND town OR microgrammes characterism chalco ikube" };
				boolean fragment = Boolean.TRUE;
				int firstResult = 0;
				int maxResults = 10;
				for (String indexName : indexNames) {
					String xml = searcherWebService.searchSingle(indexName, searchStrings[0], "name", fragment, firstResult, maxResults);
					System.out.println(xml);
					// File file = FileUtilities.getFile("./results.xml", Boolean.FALSE);
					// FileUtilities.setContents(file.getAbsolutePath(), xml.getBytes(IConstants.ENCODING));
				}
			}
		}
	}

}