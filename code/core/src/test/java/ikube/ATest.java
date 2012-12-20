package ikube;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeTypes;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.mock.SpellingCheckerMock;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.model.Server;
import ikube.search.Search;
import ikube.service.IMonitorService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Mockit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base test for all mocked tests. There are several useful mocks in this class that can be re-used like the index context and
 * the index reader etc. There are also helpful methods for creating Lucene indexes.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public abstract class ATest {

	static {
		Logging.configure();
		new MimeTypes(IConstants.MIME_TYPES);
		new MimeMapper(IConstants.MIME_MAPPING);
		// ThreadUtilities.initialize();
		Mockit.setUpMock(SpellingCheckerMock.class);
	}

	protected Logger logger;

	/** These are all mocked objects that are used in sub classes. */
	protected String ip;
	protected ScoreDoc[] scoreDocs;
	protected Searchable[] searchables;
	protected List<Indexable<?>> indexables;
	protected Map<String, Server> servers;

	protected Lock lock = mock(Lock.class);
	protected Server server = mock(Server.class);
	protected Action action = mock(Action.class);
	protected TopDocs topDocs = mock(TopDocs.class);
	protected IDataBase dataBase = mock(IDataBase.class);
	protected FSDirectory fsDirectory = mock(FSDirectory.class);
	protected IndexWriter indexWriter = mock(IndexWriter.class);
	protected IndexReader indexReader = mock(IndexReader.class);
	protected TopFieldDocs topFieldDocs = mock(TopFieldDocs.class);
	protected MultiSearcher multiSearcher = mock(MultiSearcher.class);
	protected IndexSearcher indexSearcher = mock(IndexSearcher.class);
	protected IndexContext<?> indexContext = mock(IndexContext.class);
	protected IClusterManager clusterManager = mock(IClusterManager.class);
	protected IMonitorService monitorService = mock(IMonitorService.class);
	protected IndexableTable indexableTable = mock(IndexableTable.class);
	protected IndexableColumn indexableColumn = mock(IndexableColumn.class);
	@SuppressWarnings("rawtypes")
	protected Map<String, IndexContext> indexContexts;

	protected String indexDirectoryPath = "./indexes";
	protected String indexDirectoryPathBackup = "./indexes/backup";

	@SuppressWarnings("rawtypes")
	public ATest(Class<?> subClass) {
		logger = LoggerFactory.getLogger(subClass);
		searchables = new Searchable[] { indexSearcher };
		scoreDocs = new ScoreDoc[0];
		indexables = new ArrayList<Indexable<?>>();
		servers = new HashMap<String, Server>();

		try {
			ip = InetAddress.getLocalHost().getHostAddress();
			when(indexSearcher.getIndexReader()).thenReturn(indexReader);
			when(indexSearcher.search(any(Query.class), anyInt())).thenReturn(topDocs);

			when(multiSearcher.getSearchables()).thenReturn(searchables);
			when(multiSearcher.search(any(Query.class), anyInt())).thenReturn(topDocs);
			when(multiSearcher.search(any(Query.class), any(Filter.class), anyInt(), any(Sort.class))).thenReturn(topFieldDocs);
		} catch (Exception e) {
			logger.error("", e);
		}

		topDocs.totalHits = 0;
		topDocs.scoreDocs = scoreDocs;
		topFieldDocs.totalHits = 0;
		topFieldDocs.scoreDocs = scoreDocs;
		when(indexReader.directory()).thenReturn(fsDirectory);

		when(fsDirectory.makeLock(anyString())).thenReturn(lock);

		when(indexWriter.getDirectory()).thenReturn(fsDirectory);

		when(indexContext.getIndexDirectoryPath()).thenReturn(indexDirectoryPath);
		when(indexContext.getIndexDirectoryPathBackup()).thenReturn(indexDirectoryPathBackup);
		when(indexContext.getIndexName()).thenReturn("index");
		when(indexContext.getIndexables()).thenReturn(indexables);

		when(indexContext.getMultiSearcher()).thenReturn(multiSearcher);

		when(indexContext.getBufferedDocs()).thenReturn(10);
		when(indexContext.getBufferSize()).thenReturn(10d);
		when(indexContext.getMaxFieldLength()).thenReturn(10);
		when(indexContext.getMaxReadLength()).thenReturn(10000l);
		when(indexContext.getMergeFactor()).thenReturn(10);
		when(indexContext.getMaxAge()).thenReturn((long) (60));
		when(clusterManager.getServer()).thenReturn(server);
		when(clusterManager.getServers()).thenReturn(servers);
		when(clusterManager.lock(anyString())).thenReturn(Boolean.TRUE);

		indexContexts = new HashMap<String, IndexContext>();
		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);

		when(server.isWorking()).thenReturn(Boolean.FALSE);
		when(server.getAddress()).thenReturn(ip);
		when(server.getIp()).thenReturn(ip);
		when(server.getActions()).thenReturn(Arrays.asList(action));
		when(indexContext.getIndexWriter()).thenReturn(indexWriter);
		when(indexableColumn.getContent()).thenReturn("9a avenue road, cape town, south africa");
		when(indexableColumn.isAddress()).thenReturn(Boolean.TRUE);
		when(indexableColumn.getName()).thenReturn("indexableName");
		when(ApplicationContextManagerMock.HANDLER.getIndexableClass()).thenReturn(IndexableTable.class);

		indexables.add(indexableTable);
		indexables.add(indexableColumn);
		servers.put(ip, server);
		IndexManagerMock.setIndexWriter(indexWriter);
		ApplicationContextManagerMock.setIndexContext(indexContext);
		ApplicationContextManagerMock.setClusterManager(clusterManager);
	}

	protected void delete(final IDataBase dataBase, final Class<?>... klasses) {
		for (Class<?> klass : klasses) {
			try {
				List<?> list = dataBase.find(klass, 0, 1000);
				do {
					dataBase.removeBatch(list);
					list = dataBase.find(klass, 0, 1000);
				} while (list.size() > 0);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Returns the path to the latest index directory for this server and this context. The result will be something like
	 * './index/faq/1234567890/127.0.0.1'.
	 * 
	 * @param indexContext the index context to get the directory path for
	 * @return the directory path to the latest index directory for this servers and context
	 */
	protected String getServerIndexDirectoryPath(final IndexContext<?> indexContext) {
		return IndexManager.getIndexDirectory(indexContext, System.currentTimeMillis(), ip);
	}

	protected File createIndex(final IndexContext<?> indexContext, final String... strings) {
		return createIndex(indexContext, System.currentTimeMillis(), ip, strings);
	}

	/**
	 * This method creates an index using the index path in the context, the time and the ip and returns the latest index directory, i.e.
	 * the index that has just been created. Note that if there are still cascading mocks from JMockit, the index writer sill not create the
	 * index! So you have to tear down all mocks prior to using this method.
	 * 
	 * @param indexContext the index context to use for the path to the index
	 * @param strings the data that must be in the index
	 * @return the latest index directory, i.e. the one that has just been created
	 */
	protected File createIndex(final IndexContext<?> indexContext, final long time, final String ip, final String... strings) {
		if (strings == null || strings.length == 0) {
			throw new RuntimeException("There must be some strings to index : " + strings);
		}
		IndexWriter indexWriter = null;
		try {
			indexWriter = createIndexReturnWriter(indexContext, time, ip, strings);
		} catch (Exception e) {
			logger.error("Exception creating the index : ", e);
		} finally {
			IndexManager.closeIndexWriter(indexWriter);
		}
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		File serverIndexDirectory = new File(latestIndexDirectory, ip);
		logger.info("Created index in : " + serverIndexDirectory.getAbsolutePath());
		return serverIndexDirectory;
	}

	/**
	 * This method will create an index in the index context directory and return the writer without closing it.
	 * 
	 * @param indexContext the context to create the index for
	 * @param time the time for the directory
	 * @param ip the ip of the server creating the index
	 * @param strings the data to put in the index
	 * @return the index writer that was used to create the index
	 */
	protected IndexWriter createIndexReturnWriter(final IndexContext<?> indexContext, final long time, final String ip,
			final String... strings) {
		if (strings == null || strings.length == 0) {
			throw new RuntimeException("There must be some strings to index : " + strings);
		}
		IndexWriter indexWriter = null;
		try {
			indexWriter = IndexManager.openIndexWriter(indexContext, time, ip);
			for (String string : strings) {
				String id = Long.toString(System.currentTimeMillis());
				Document document = getDocument(id, string, Index.ANALYZED);
				indexWriter.addDocument(document);
			}
		} catch (Exception e) {
			logger.error("Exception creating the index : ", e);
		}
		return indexWriter;
	}

	protected Document getDocument(final String id, final String string, final Index analyzed) {
		Document document = new Document();
		IndexManager.addStringField(IConstants.ID, id, document, Store.YES, analyzed, TermVector.YES);
		IndexManager.addStringField(IConstants.CONTENTS, string, document, Store.YES, analyzed, TermVector.YES);
		IndexManager.addStringField(IConstants.NAME, string, document, Store.YES, analyzed, TermVector.YES);
		return document;
	}

	/**
	 * This method will create multiple indexes and return the index directories.
	 * 
	 * @param indexContext the index context to create the indexes from
	 * @param time the time of the indexes
	 * @param ips the ip's of the servers that are creating the indexes
	 * @param strings the data that should be indexed
	 * @return the list of files that are the index directories
	 */
	protected List<File> createIndexes(final IndexContext<?> indexContext, final long time, final String[] ips, final String... strings) {
		if (strings == null || strings.length == 0) {
			throw new RuntimeException("There must be some strings to index : " + strings);
		}
		List<File> serverIndexDirectories = new ArrayList<File>();
		for (String ip : ips) {
			File serverIndexDirectory = createIndex(indexContext, time, ip, strings);
			serverIndexDirectories.add(serverIndexDirectory);
		}
		return serverIndexDirectories;
	}

	protected Lock getLock(Directory directory, File serverIndexDirectory) throws IOException {
		logger.info("Is locked : " + IndexWriter.isLocked(directory));
		Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		boolean gotLock = lock.obtain(Lock.LOCK_OBTAIN_WAIT_FOREVER);
		logger.info("Got lock : " + gotLock + ", is locked : " + lock.isLocked());
		if (!gotLock) {
			// If the lock is not created then we have to create it. Sometimes
			// this fails to create a lock for some unknown reason, similar to the index writer
			// not really creating the index in ATest, strange!!
			FileUtilities.getFile(new File(serverIndexDirectory, IndexWriter.WRITE_LOCK_NAME).getAbsolutePath(), Boolean.FALSE);
		} else {
			assertTrue(IndexWriter.isLocked(directory));
		}
		logger.info("Is now locked : " + IndexWriter.isLocked(directory));
		return lock;
	}

	/**
	 * This method will create an index using the analyzer specified with the field and the strings to index, in memory. Then open a
	 * searcher on the index and a {@link Search} class with the analyzer and the searcher for immediate use.
	 * 
	 * @param searchClass the class of search to instantiate with the searcher and the analyzer
	 * @param analyzer the analyzer to use for the indexing and the searching
	 * @param field the field in the index to create
	 * @param strings the string to index into the index
	 * @return the search class with the searcher and analyzer ready for use
	 * @throws Exception
	 */
	public static <T extends Search> T createIndexAndSearch(final Class<T> searchClass, final Analyzer analyzer, final String field,
			final String... strings) throws Exception {
		IndexWriterConfig conf = new IndexWriterConfig(IConstants.VERSION, analyzer);
		Directory directory = new RAMDirectory();
		IndexWriter indexWriter = new IndexWriter(directory, conf);

		for (final String string : strings) {
			Document document = new Document();
			IndexManager.addStringField(field, string, document, Store.YES, Index.ANALYZED, TermVector.YES);
			indexWriter.addDocument(document, analyzer);
		}

		IndexManager.closeIndexWriter(indexWriter);

		IndexReader indexReader = IndexReader.open(directory);
		Searcher searcher = new IndexSearcher(indexReader);

		return searchClass.getConstructor(Searcher.class, Analyzer.class).newInstance(searcher, analyzer);
	}

}