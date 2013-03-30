package ikube.action.index.handler.enrich.geocode;

import static org.junit.Assert.assertNull;
import ikube.ATest;
import ikube.action.index.handler.enrich.geocode.Coordinate;
import ikube.action.index.handler.enrich.geocode.NavtecGeocoder;

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

	@Test
	public void getCoordinate() {
		NavtecGeocoder geocoder = new NavtecGeocoder();
		Coordinate coordinate = geocoder.getCoordinate("some address");
		logger.info("Note that the NavtecGeocoder is not completely implemented : ");
		assertNull("The class is not completely implemented, so this should be null : ", coordinate);
	}

}
