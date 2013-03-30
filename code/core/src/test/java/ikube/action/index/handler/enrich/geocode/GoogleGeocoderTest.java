package ikube.action.index.handler.enrich.geocode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.action.index.handler.enrich.geocode.Coordinate;
import ikube.action.index.handler.enrich.geocode.GoogleGeocoder;
import ikube.mock.FileUtilitiesMock;
import ikube.mock.URLMock;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayOutputStream;
import java.io.File;

import mockit.Mockit;

import org.junit.After;
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

	@After
	public void after() {
	}

	@Test
	public void getCoordinate() throws Exception {
		try {
			File file = FileUtilities.findFileRecursively(new File("."), "address.xml");
			ByteArrayOutputStream contents = FileUtilities.getContents(file, Integer.MAX_VALUE);
			FileUtilitiesMock.setContents(contents);
			URLMock.setContents(contents);
			Mockit.setUpMocks(URLMock.class, FileUtilitiesMock.class);

			GoogleGeocoder geocoder = new GoogleGeocoder();
			geocoder.setSearchUrl("http://maps.googleapis.com/maps/api/geocode/xml");
			Coordinate coordinate = geocoder.getCoordinate("9 avenue road, cape town, south africa");
			assertNotNull("The coordinate can not be null as this is a real address from Google : ", coordinate);
			double lat = coordinate.getLat();
			double lon = coordinate.getLon();
			assertEquals("We know that this address exists and where it is : ", -33.9693580, lat, 1.0);
			assertEquals("We know that this address exists and where it is : ", 18.4622110, lon, 1.0);
		} finally {
			Mockit.tearDownMocks(URLMock.class, FileUtilitiesMock.class);
		}
	}

}
