package ikube.web.service;

import ikube.IConstants;

import java.net.MalformedURLException;
import java.net.URL;

public class SearcherXmlIntegration extends SearcherIntegration {

	protected String getUrl(String path) throws MalformedURLException {
		StringBuilder builder = new StringBuilder();
		builder.append(IConstants.SEP);
		builder.append(IConstants.IKUBE);
		builder.append(SERVICE);
		builder.append(SearcherXml.SEARCH);
		builder.append(SearcherXml.XML);
		builder.append(path);
		return new URL("http", LOCALHOST, SERVER_PORT, builder.toString()).toString();
	}

}