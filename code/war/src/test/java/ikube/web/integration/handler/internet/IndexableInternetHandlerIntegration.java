package ikube.web.integration.handler.internet;

import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.database.jpa.DataBaseJpa;
import ikube.index.IndexManager;
import ikube.index.handler.internet.IndexableInternetHandler;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.web.Integration;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import mockit.Deencapsulation;

import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 20.05.12
 * @version 01.00
 */
public class IndexableInternetHandlerIntegration extends Integration {

	private IndexContext<?> indexContext;
	private IndexableInternet indexableInternet;
	private IndexableInternetHandler indexableInternetHandler;

	@Before
	@SuppressWarnings("rawtypes")
	public void before() throws Exception {
		indexContext = new IndexContext();
		indexContext.setBufferedDocs(10);
		indexContext.setMergeFactor(10);
		indexContext.setBufferSize(100);

		indexableInternet = new IndexableInternet();
		indexableInternet.setExcludedPattern(".*(.zip).*|.*(.png).*|.*(/search).*|.*(/images).*"
				+ "|.*(wiki).*|.*(r=).*|.*(.war).*|.*(terminate).*|.*(startup).*");
		indexableInternetHandler = new IndexableInternetHandler();
		indexableInternetHandler.setThreads(1);
		IDataBase dataBase = new DataBaseJpa() {
			@SuppressWarnings("unchecked")
			public <T> List<T> find(Class<T> klass, String sql, String[] names, Object[] values, int startPosition, int maxResults) {
				return Arrays.asList();
			}

			public <T> void persistBatch(List<T> list) {
				// Do nothing
			}
		};
		Deencapsulation.setField(indexableInternetHandler, "dataBase", dataBase);

		File indexDirectory = FileUtilities.getFile("./indexes", Boolean.TRUE);
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, true);

		indexContext.getIndex().setIndexWriter(indexWriter);
		indexContext.setAction(new Action());
	}

	@Test
	public void login() throws Exception {
		URL url = new URL("http", LOCALHOST, SERVER_PORT, IConstants.SEP + IConstants.IKUBE);
		indexableInternet.setLoginUrl(url.toString());
		indexableInternet.setUserid(REST_USER_NAME);
		indexableInternet.setPassword(REST_PASSWORD);
		indexableInternet.setBaseUrl(url.toString());
		indexableInternet.setUrl(url.toString());
		// Index the pages in the application
		List<Future<?>> futures = indexableInternetHandler.handle(indexContext, indexableInternet);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		assertTrue("There must be some documents in the index : ", indexContext.getIndex().getIndexWriter().numDocs() > 3);
	}

}