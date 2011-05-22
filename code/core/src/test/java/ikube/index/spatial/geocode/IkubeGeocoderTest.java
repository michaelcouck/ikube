package ikube.index.spatial.geocode;

import ikube.ATest;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class IkubeGeocoderTest extends ATest {

	public IkubeGeocoderTest() {
		super(IkubeGeocoderTest.class);
	}

	@Test
	public void getCoordinate() throws Exception {
		// TODO When the index is created on the server then un-comment this
		// IkubeGeocoder geocoder = (IkubeGeocoder) ApplicationContextManager.getBean(IGeocoder.class);
		// Coordinate coordinate = geocoder.getCoordinate("9 avenue road, cape town, south africa");
		// assertNotNull(coordinate);
		// double lat = coordinate.getLat();
		// double lon = coordinate.getLon();
		// assertEquals(-33.9693580, lat, 1.0);
		// assertEquals(18.4622110, lon, 1.0);
	}

}
