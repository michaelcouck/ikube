package ikube.index.spatial.geocode;

import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.net.URL;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class GoogleGeocoderTest extends ATest {

	@Test
	public void getCoordinate() throws Exception {
		// TODO Implement me
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
		String content = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
		logger.info(content);
		assertNotNull(content);
	}

}
