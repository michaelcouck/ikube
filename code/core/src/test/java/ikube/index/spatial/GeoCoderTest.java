package ikube.index.spatial;

import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.net.URL;

import org.junit.Test;

public class GeoCoderTest extends ATest {

	@Test
	public void geoCode() throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append(IConstants.GEO_CODE_API);
		builder.append("?");
		builder.append(IConstants.ADDRESS);
		builder.append("=");
		builder.append("9a%20avenue%20road,%20cape%20town,%20south%20africa");
		builder.append("&");
		builder.append(IConstants.sensor);
		builder.append("=");
		builder.append("true");
		URL url = new URL(builder.toString());
		String content = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
		logger.info(content);
		assertNotNull(content);
	}

}
