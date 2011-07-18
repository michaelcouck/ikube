package ikube.index.spatial.geocode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.spatial.Coordinate;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.mock.ServiceLocatorMock;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class GeocoderTest extends ATest {

	public GeocoderTest() {
		super(GeocoderTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class, ServiceLocatorMock.class);
		ISearcherWebService searcherWebService = ServiceLocator.getService(ISearcherWebService.class, "searchUrl",
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		// Add the target result
		Map<String, String> result = new HashMap<String, String>();
		result.put(IConstants.LATITUDE, "-33.9693580");
		result.put(IConstants.LONGITUDE, "18.4622110");
		results.add(result);
		// Add the statistics result
		result = new HashMap<String, String>();
		results.add(result);
		// Serialize the results and force the mock to return it
		String xml = SerializationUtilities.serialize(results);
		when(searcherWebService.searchMulti(anyString(), any(String[].class), any(String[].class), anyBoolean(), anyInt(), anyInt()))
				.thenReturn(xml);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void getCoordinate() throws Exception {
		Geocoder geocoder = new Geocoder();
		geocoder.setSearchUrl("searchUrl");
		geocoder.setSearchFields(Arrays.asList("searchField"));

		Coordinate coordinate = geocoder.getCoordinate("9 avenue road, cape town, south africa");
		assertNotNull(coordinate);
		double lat = coordinate.getLat();
		double lon = coordinate.getLon();
		assertEquals(-33.9693580, lat, 1.0);
		assertEquals(18.4622110, lon, 1.0);
	}

}
