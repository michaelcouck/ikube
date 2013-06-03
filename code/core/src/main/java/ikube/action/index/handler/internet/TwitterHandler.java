package ikube.action.index.handler.internet;

import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.security.WebServiceAuthentication;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

/**
 * @author Michael Couck
 * @since 24.04.13
 * @version 01.00
 */
public class TwitterHandler extends IndexableHandler<IndexableInternet> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableInternet indexableInternet) throws Exception {
		// The start url
		List<Future<?>> futures = new ArrayList<Future<?>>();
		HttpClient httpClient = new HttpClient();
		login(indexableInternet, httpClient);
		HttpMethod httpMethod = new GetMethod(indexableInternet.getBaseUrl());
		int response = httpClient.executeMethod(httpMethod);
		InputStream inputStream = httpMethod.getResponseBodyAsStream();
		List<String> lines = IOUtils.readLines(inputStream);
		logger.info("Response : " + response + ", " + lines);
		return futures;
	}
	
	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final Indexable<?> indexable, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
		return null;
	}

	void login(final IndexableInternet indexableInternet, final HttpClient httpClient) {
		URL loginUrl;
		try {
			loginUrl = new URL(indexableInternet.getLoginUrl());
			String userid = indexableInternet.getUserid();
			String password = indexableInternet.getPassword();
			new WebServiceAuthentication().authenticate(httpClient, loginUrl.getHost(), "80", userid, password);
		} catch (Exception e) {
			logger.error("Exception logging in to site : " + indexableInternet, e);
		}
	}
}