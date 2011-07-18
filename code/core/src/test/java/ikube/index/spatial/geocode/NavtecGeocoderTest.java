package ikube.index.spatial.geocode;

import static org.junit.Assert.assertNull;
import ikube.ATest;
import ikube.index.spatial.Coordinate;
import ikube.mock.ServiceLocatorMock;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 09.04.11
 * @version 01.00
 */
public class NavtecGeocoderTest extends ATest {

	public NavtecGeocoderTest() {
		super(NavtecGeocoderTest.class);
	}
	
	@Before
	public void before() {
		Mockit.setUpMocks(ServiceLocatorMock.class);
	}
	
	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void getCoordinate() {
		NavtecGeocoder geocoder = new NavtecGeocoder();
		Coordinate coordinate = geocoder.getCoordinate("some address");
		logger.info("Note that the NavtecGeocoder is not completely implemented : ");
		assertNull("The class is not completely implemented, so this should be null : ", coordinate);
	}

}
