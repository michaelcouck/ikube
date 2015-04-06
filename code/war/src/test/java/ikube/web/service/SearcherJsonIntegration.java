package ikube.web.service;

import ikube.IConstants;

import java.net.MalformedURLException;
import java.net.URL;

public class SearcherJsonIntegration extends SearcherIntegration {

	@SuppressWarnings("StringBufferReplaceableByString")
    protected String getSearchUrl(final String service) throws MalformedURLException {
		StringBuilder builder = new StringBuilder();
		builder.append(IConstants.SEP);
		builder.append(IConstants.IKUBE);
		builder.append(SERVICE);
		builder.append(SearcherJson.SEARCH);
		builder.append(SearcherJson.JSON);
		builder.append(service);
		return new URL("http", LOCALHOST, SERVER_PORT, builder.toString()).toString();
	}

}