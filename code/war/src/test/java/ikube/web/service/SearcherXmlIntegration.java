package ikube.web.service;

import java.net.MalformedURLException;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2012
 */
public class SearcherXmlIntegration extends SearcherIntegration {

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getSearchUrl(String path) throws MalformedURLException {
        return getServiceUrl(SearcherXml.SEARCH + SearcherXml.XML + path);
    }

}