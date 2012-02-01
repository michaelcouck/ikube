package ikube.web.integration;

import ikube.IConstants;
import ikube.web.Integration;
import ikube.web.toolkit.JspStrategy;
import ikube.web.toolkit.LoadStrategy;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class UiIntegration extends Integration {

	@Test
	public void integration() throws Exception {
		validateJsps();
		loadWebService();
	}

	protected void validateJsps() throws Exception {
		// Test all the jsps
		new JspStrategy(IConstants.SEP + IConstants.IKUBE, 9080).perform();
	}

	protected void loadWebService() throws Exception {
		// Load test the web service
		new LoadStrategy(10000, 10).perform();
	}

}
