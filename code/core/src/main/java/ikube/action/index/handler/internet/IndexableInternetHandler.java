package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.security.WebServiceAuthentication;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-06-2013
 */
@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "SpringJavaAutowiringInspection"})
public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private InternetResourceHandler internetResourceHandler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ForkJoinTask<?> handleIndexableForked(
	  final IndexContext indexContext,
	  final IndexableInternet indexableInternet)
	  throws Exception {
		authenticate(indexableInternet);
		IResourceProvider resourceProvider = new InternetResourceProvider(indexableInternet, dataBase);
		return getRecursiveAction(indexContext, indexableInternet, resourceProvider);
	}

	private void authenticate(final IndexableInternet indexableInternet) throws MalformedURLException {
		String url = indexableInternet.getBaseUrl();
		int port = new URL(indexableInternet.getUrl()).getPort();
		String userid = indexableInternet.getUserid();
		String password = indexableInternet.getPassword();
		if (!StringUtils.isEmpty(url) && !StringUtils.isEmpty(userid) && !StringUtils.isEmpty(password)) {
			new WebServiceAuthentication().authenticate(new AutoRetryHttpClient(), url, port, userid, password);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected List<Url> handleResource(
	  final IndexContext indexContext,
	  final IndexableInternet indexableInternet,
	  final Object resource) {
		Url url = (Url) resource;
		try {
			if (url.getRawContent() != null) {
				internetResourceHandler.handleResource(indexContext, indexableInternet, new Document(), url);
			}
		} catch (final Exception e) {
			handleException(indexableInternet, e, "Exception indexing site : " + indexableInternet.getName());
		} finally {
			url.setRawContent(null);
			url.setParsedContent(null);
		}
		return Collections.EMPTY_LIST;
	}


}