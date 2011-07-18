package ikube.index.spatial.geocode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.index.spatial.Coordinate;
import ikube.toolkit.FileUtilities;

import java.net.URL;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class GoogleGeocoderTest extends ATest {

	public GoogleGeocoderTest() {
		super(GoogleGeocoderTest.class);
	}

	@Test
	public void getCoordinate() throws Exception {
		GoogleGeocoder geocoder = new GoogleGeocoder();
		geocoder.setSearchUrl("http://maps.googleapis.com/maps/api/geocode/xml");
		Coordinate coordinate = geocoder.getCoordinate("9 avenue road, cape town, south africa");
		assertNotNull("The coordinate can not be null as this is a real address from Gogole : ", coordinate);
		double lat = coordinate.getLat();
		double lon = coordinate.getLon();
		assertEquals("We know that this address exists and where it is : ", -33.9693580, lat, 1.0);
		assertEquals("We know that this address exists and where it is : ", 18.4622110, lon, 1.0);
	}

	@Test
	public void apiVerification() throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append("http://maps.googleapis.com/maps/api/geocode/xml");
		builder.append("?");
		builder.append(GoogleGeocoder.ADDRESS);
		builder.append("=");
		builder.append("9a%20avenue%20road,%20cape%20town,%20south%20africa");
		builder.append("&");
		builder.append(GoogleGeocoder.SENSOR);
		builder.append("=");
		builder.append("true");
		URL url = new URL(builder.toString());
		logger.info("Url : " + builder.toString());
		String content = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
		logger.info(content);
		assertNotNull(content);
	}

}
