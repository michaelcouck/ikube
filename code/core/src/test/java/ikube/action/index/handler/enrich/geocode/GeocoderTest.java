package ikube.action.index.handler.enrich.geocode;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.handler.strategy.geocode.Geocoder;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.Coordinate;
import ikube.model.Search;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 06.03.11
 */
public class GeocoderTest extends AbstractTest {

	@MockClass(realClass = HttpClient.class)
	public static class HttpClientMock {
		@Mock()
		@SuppressWarnings({ "UnusedDeclaration", "DuplicateThrows" })
		public <T> T execute(final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler)
				throws IOException, ClientProtocolException {
			return null;
		}
	}

	@SuppressWarnings("UnusedDeclaration")
	@MockClass(realClass = HttpRequestBase.class)
	public static class HttpMethodBaseMock {
		@Mock()
		public String getResponseBodyAsString() throws IOException {
			ArrayList<HashMap<String, String>> results = new ArrayList<>();
			HashMap<String, String> result = new HashMap<>();
			// Add the results that we are looking for
			result.put(IConstants.LATITUDE, "-33.9693580");
			result.put(IConstants.LONGITUDE, "18.4622110");
			results.add(result);
			// Add the statistics too
			result = new HashMap<>();
			results.add(result);

			Search search = new Search();
			search.setSearchResults(results);
			return IConstants.GSON.toJson(search);
		}
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class, HttpClientMock.class, HttpMethodBaseMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class, HttpClientMock.class, HttpMethodBaseMock.class);
	}

	@Test
	public void getCoordinate() throws Exception {
		Geocoder geocoder = new Geocoder();
		geocoder.setSearchUrl("http://localhost:8080/ikube/service/search/json");
		geocoder.setSearchField("name");
		geocoder.setUserid("userid");
		geocoder.setPassword("password");
		geocoder.afterPropertiesSet();

		Coordinate coordinate = geocoder.getCoordinate("9 avenue road, cape town, south africa");
		assertNotNull(coordinate);
		double lat = coordinate.getLatitude();
		double lon = coordinate.getLongitude();
		assertEquals(-33.9693580, lat, 1.0);
		assertEquals(18.4622110, lon, 1.0);
	}

}
