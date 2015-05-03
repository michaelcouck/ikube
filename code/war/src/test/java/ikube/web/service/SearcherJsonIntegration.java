package ikube.web.service;

import java.net.MalformedURLException;

public class SearcherJsonIntegration extends SearcherIntegration {

	@SuppressWarnings("StringBufferReplaceableByString")
    protected String getSearchUrl(String path) throws MalformedURLException {
        return getServiceUrl(SearcherJson.SEARCH + SearcherJson.JSON + path);
	}

}