package ikube.web.service;

import java.net.MalformedURLException;

public class SearcherJsonIntegration extends SearcherIntegration {

	@SuppressWarnings("StringBufferReplaceableByString")
    protected String getSearchUrl(String path) throws MalformedURLException {
        return getUrl(SearcherJson.SEARCH + SearcherJson.JSON + path);
	}

}