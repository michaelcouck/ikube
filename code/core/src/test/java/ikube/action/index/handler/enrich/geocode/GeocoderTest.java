package ikube.action.index.handler.enrich.geocode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.handler.enrich.geocode.Coordinate;
import ikube.action.index.handler.enrich.geocode.Geocoder;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.toolkit.SerializationUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class GeocoderTest extends AbstractTest {

	static ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
	// Add the target result
	static HashMap<String, String> result = new HashMap<String, String>();

	static {
		result.put(IConstants.LATITUDE, "-33.9693580");
		result.put(IConstants.LONGITUDE, "18.4622110");
		results.add(result);
		// Add the statistics result
		result = new HashMap<String, String>();
		results.add(result);
	}

	@MockClass(realClass = HttpClient.class)
	public static class HttpClientMock {

		@Mock()
		public int executeMethod(final HttpMethod method) throws IOException, HttpException {
			return 200;
		}

	}

	@MockClass(realClass = HttpMethodBase.class)
	public static class HttpMethodBaseMock {

		@Mock()
		public String getResponseBodyAsString() throws IOException {
			return SerializationUtilities.serialize(results);
		}

		@Mock()
		public void setQueryString(final NameValuePair[] params) {
			// Do nothing
		}

	}

	public GeocoderTest() {
		super(GeocoderTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class, HttpClientMock.class, HttpMethodBaseMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void getCoordinate() throws Exception {
		Geocoder geocoder = new Geocoder();
		geocoder.setSearchUrl("http://localhost:8080/ikube/service/search/single");
		geocoder.setSearchFields(Arrays.asList("name"));
		geocoder.setUserid("userid");
		geocoder.setPassword("password");
		geocoder.afterPropertiesSet();

		Coordinate coordinate = geocoder.getCoordinate("9 avenue road, cape town, south africa");
		assertNotNull(coordinate);
		double lat = coordinate.getLat();
		double lon = coordinate.getLon();
		assertEquals(-33.9693580, lat, 1.0);
		assertEquals(18.4622110, lon, 1.0);
	}

}
