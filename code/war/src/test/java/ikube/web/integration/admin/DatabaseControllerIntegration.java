package ikube.web.integration.admin;

import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.web.Integration;

import java.net.URL;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseControllerIntegration extends Integration {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseControllerIntegration.class);

	private String url;

	@Test
	public void selectWithSort() throws Exception {
		String path = "/ikube/admin/database/sorted.html";
		url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();

		String[] names = { "targetView", "classType", "sortFields", "directionOfSort", IConstants.FIRST_RESULT, IConstants.MAX_RESULTS };
		String[] values = { "/admin/database", "ikube.model.Search", "id", "false", "0", "10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		LOGGER.info("Query string : " + getMethod.getQueryString());
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		assertTrue("We should get something : " + result, actual.length() > 0);
	}

	@Test
	public void selectWithFiltering() throws Exception {
		String path = "/ikube/admin/database/filtered.html";
		url = new URL("http", LOCALHOST, SERVER_PORT, path).toString();

		String[] names = { "targetView", "classType", "fieldsToFilterOn", "valuesToFilterOn", IConstants.FIRST_RESULT,
				IConstants.MAX_RESULTS };
		String[] values = { "/admin/database", "ikube.model.Action", "actionName", "Index", "0", "10" };
		NameValuePair[] params = getNameValuePairs(names, values);

		GetMethod getMethod = new GetMethod(url);
		getMethod.setQueryString(params);
		LOGGER.info("Query string : " + getMethod.getQueryString());
		int result = HTTP_CLIENT.executeMethod(getMethod);
		String actual = getMethod.getResponseBodyAsString();
		assertTrue("We should get something : " + result, actual.length() > 0);
	}

}