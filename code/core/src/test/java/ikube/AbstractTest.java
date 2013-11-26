package ikube;

import static ikube.toolkit.ApplicationContextManager.getBean;
import static ikube.toolkit.ObjectToolkit.populateFields;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.action.index.IndexManager;
import ikube.action.index.parse.mime.MimeMapper;
import ikube.action.index.parse.mime.MimeTypes;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.model.Action;
import ikube.model.Analysis;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.model.Server;
import ikube.search.Search;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.FieldInfos;
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
import org.apache.lucene.util.ReaderUtil;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base test for all mocked tests. There are several useful mocks in this class that can be re-used like the index context and the index reader etc.
 * There are also helpful methods for creating Lucene indexes.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Ignore
@SuppressWarnings("deprecation")
public abstract class AbstractTest {

	static {
		try {
			Logging.configure();
			new MimeTypes(IConstants.MIME_TYPES);
			new MimeMapper(IConstants.MIME_MAPPING);
			ThreadUtilities.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/** These are all mocked objects that are used in sub classes. */
	protected String ip;
	protected ScoreDoc[] scoreDocs;
	protected Searchable[] searchables;
	protected List<Indexable<?>> indexables;
	protected Map<String, Server> servers;

	protected Lock lock;
	protected Server server;
	protected Action action;
	protected TopDocs topDocs;
	protected IDataBase dataBase;
	protected FSDirectory fsDirectory;
	protected IndexWriter indexWriter;
	protected IndexReader indexReader;
	protected TopFieldDocs topFieldDocs;
	protected MultiSearcher multiSearcher;
	protected IndexSearcher indexSearcher;
	protected IndexContext<?> indexContext;
	protected IClusterManager clusterManager;
	protected IMonitorService monitorService;
	protected IndexableTable indexableTable;
	protected IndexableColumn indexableColumn;
	@SuppressWarnings("rawtypes")
	protected Map<String, IndexContext> indexContexts;

	protected String indexDirectoryPath = "./indexes";
	protected String indexDirectoryPathBackup = "./indexes/backup";

	{
		try {
			initialize();
		} catch (Exception e) {
			logger.error(null, e);
		}
	}

	@SuppressWarnings("rawtypes")
	private void initialize() throws Exception {
		lock = mock(Lock.class);
		server = mock(Server.class);
		action = mock(Action.class);
		topDocs = mock(TopDocs.class);
		dataBase = mock(IDataBase.class);
		fsDirectory = mock(FSDirectory.class);
		indexWriter = mock(IndexWriter.class);
		indexReader = mock(IndexReader.class);
		topFieldDocs = mock(TopFieldDocs.class);
		multiSearcher = mock(MultiSearcher.class);
		indexSearcher = mock(IndexSearcher.class);
		indexContext = mock(IndexContext.class);
		clusterManager = mock(IClusterManager.class);
		monitorService = mock(IMonitorService.class);
		indexableTable = mock(IndexableTable.class);
		indexableColumn = mock(IndexableColumn.class);

		searchables = new Searchable[] { indexSearcher };
		scoreDocs = new ScoreDoc[0];
		indexables = new ArrayList<Indexable<?>>();
		servers = new HashMap<String, Server>();
		indexContexts = new HashMap<String, IndexContext>();

		ip = UriUtilities.getIp();

		when(indexSearcher.getIndexReader()).thenReturn(indexReader);
		when(indexSearcher.search(any(Query.class), anyInt())).thenReturn(topDocs);

		when(multiSearcher.getSearchables()).thenReturn(searchables);
		when(multiSearcher.search(any(Query.class), anyInt())).thenReturn(topDocs);
		when(multiSearcher.search(any(Query.class), any(Filter.class), anyInt(), any(Sort.class))).thenReturn(topFieldDocs);

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
		when(indexContext.getChildren()).thenReturn(indexables);

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

		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);

		when(action.getIndexName()).thenReturn("index");

		when(server.isWorking()).thenReturn(Boolean.FALSE);
		when(server.getAddress()).thenReturn(ip);
		when(server.getIp()).thenReturn(ip);
		when(server.getActions()).thenReturn(Arrays.asList(action));
		when(indexContext.getIndexWriters()).thenReturn(new IndexWriter[] { indexWriter });
		when(indexableTable.getName()).thenReturn("indexableTable");
		when(indexableColumn.getContent()).thenReturn("9a avenue road, cape town, south africa");
		when(indexableColumn.isAddress()).thenReturn(Boolean.TRUE);
		when(indexableColumn.getName()).thenReturn("indexableName");
		
		when(indexableColumn.isAnalyzed()).thenReturn(Boolean.TRUE);
		when(indexableColumn.isStored()).thenReturn(Boolean.TRUE);
		when(indexableColumn.isVectored()).thenReturn(Boolean.TRUE);
		
		when(ApplicationContextManagerMock.HANDLER.getIndexableClass()).thenReturn(IndexableTable.class);

		indexables.add(indexableTable);
		indexables.add(indexableColumn);
		servers.put(ip, server);
		IndexManagerMock.setIndexWriter(indexWriter);
		ApplicationContextManagerMock.setIndexContext(indexContext);
		ApplicationContextManagerMock.setClusterManager(clusterManager);
	}

	protected static void delete(final IDataBase dataBase, final Class<?>... klasses) {
		for (final Class<?> klass : klasses) {
			List<?> list = dataBase.find(klass, 0, 1000);
			do {
				dataBase.removeBatch(list);
				list = dataBase.find(klass, 0, 1000);
			} while (list.size() > 0);
		}
	}

	protected static <T> void insert(final Class<T> klass, final int entities) {
		IDataBase dataBase = getBean(IDataBase.class);
		List<T> tees = new ArrayList<T>();
		for (int i = 0; i < entities; i++) {
			T tee;
			try {
				tee = populateFields(klass, klass.newInstance(), Boolean.TRUE, 1, "id", "indexContext");
				tees.add(tee);
				if (tees.size() >= 1000) {
					dataBase.persistBatch(tees);
					tees.clear();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		dataBase.persistBatch(tees);
	}

	/**
	 * Returns the path to the latest index directory for this server and this context. The category will be something like './index/faq/1234567890/127.0.0.1'.
	 * 
	 * @param indexContext the index context to get the directory path for
	 * @return the directory path to the latest index directory for this servers and context
	 */
	protected String getServerIndexDirectoryPath(final IndexContext<?> indexContext) {
		return IndexManager.getIndexDirectory(indexContext, System.currentTimeMillis(), ip);
	}

	protected File createIndexFileSystem(final IndexContext<?> indexContext, final String... strings) {
		try {
			return createIndexFileSystem(indexContext, System.currentTimeMillis(), ip, strings);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Directory createIndexRam(final IndexContext<?> indexContext, final String... strings) throws Exception {
		IndexWriter indexWriter = getRamIndexWriter(new StandardAnalyzer(IConstants.VERSION));
		addDocuments(indexWriter, IConstants.CONTENTS, strings);
		printIndex(indexWriter.getReader(), 100);
		return indexWriter.getDirectory();
	}

	protected File createIndexFileSystem(final IndexContext<?> indexContext, final long time, final String ip, final String... strings) throws Exception {
		IndexWriter indexWriter = null;
		indexWriter = IndexManager.openIndexWriter(indexContext, time, ip);
		addDocuments(indexWriter, IConstants.CONTENTS, strings);
		File indexDirectory = ((FSDirectory) indexWriter.getDirectory()).getDirectory();
		IndexManager.closeIndexWriter(indexWriter);
		return indexDirectory;
	}

	protected List<File> createIndexesFileSystem(final IndexContext<?> indexContext, final long time, final String[] ips, final String... strings) {
		try {
			List<File> serverIndexDirectories = new ArrayList<File>();
			for (String ip : ips) {
				File serverIndexDirectory = createIndexFileSystem(indexContext, time, ip, strings);
				serverIndexDirectories.add(serverIndexDirectory);
			}
			return serverIndexDirectories;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected <T extends Search> T createIndexRamAndSearch(final Class<T> searchClass, final Analyzer analyzer, final String field, final String... strings)
			throws Exception {
		IndexWriter indexWriter = getRamIndexWriter(analyzer);
		addDocuments(indexWriter, field, strings);
		IndexReader indexReader = IndexReader.open(indexWriter.getDirectory());
		Searcher searcher = new IndexSearcher(indexReader);
		Searchable[] searchables = new Searchable[] { searcher };
		MultiSearcher multiSearcher = new MultiSearcher(searchables);
		// printIndex(indexReader, 100);
		return searchClass.getConstructor(Searcher.class, Analyzer.class).newInstance(multiSearcher, analyzer);
	}

	private IndexWriter getRamIndexWriter(final Analyzer analyzer) throws Exception {
		IndexWriterConfig conf = new IndexWriterConfig(IConstants.VERSION, analyzer);
		Directory directory = new RAMDirectory();
		return new IndexWriter(directory, conf);
	}

	protected <T extends Search> T createIndexRamAndSearch(final Class<T> searchClass, final Analyzer analyzer, final String[] fields,
			final String[]... strings) throws Exception {
		IndexWriter indexWriter = getRamIndexWriter(analyzer);

		Indexable<?> indexable = new Indexable<Object>() {};
		for (final String[] row : strings) {
			int i = 0;
			Document document = new Document();
			for (final String column : row) {
				String field = fields[i];
				if (StringUtils.isNumeric(column.trim())) {
					IndexManager.addNumericField(field, column.trim(), document, Store.YES);
				} else {
					IndexManager.addStringField(field, column, indexable, document);
				}
				i++;
			}
			indexWriter.addDocument(document);
		}

		Directory directory = indexWriter.getDirectory();

		indexWriter.commit();
		indexWriter.maybeMerge();
		indexWriter.forceMerge(5);
		indexWriter.close(Boolean.TRUE);

		IndexReader indexReader = IndexReader.open(directory);
		// printIndex(indexReader, 10);
		Searcher searcher = new IndexSearcher(indexReader);

		Searchable[] searchables = new Searchable[] { searcher };
		MultiSearcher multiSearcher = new MultiSearcher(searchables);
		return searchClass.getConstructor(Searcher.class, Analyzer.class).newInstance(multiSearcher, analyzer);
	}

	protected void addDocuments(final IndexWriter indexWriter, final String field, final String... strings) throws Exception {
		for (final String string : strings) {
			String id = Long.toString(System.currentTimeMillis());
			Document document = getDocument(id, string, field, Index.ANALYZED);
			indexWriter.addDocument(document);
		}
		try {
			indexWriter.commit();
			indexWriter.maybeMerge();
		} catch (NullPointerException e) {
			logger.error("Null pointer, mock? : " + e.getMessage());
		}
	}

	protected Document getDocument(final String id, final String string, final String field, final Index analyzed) {
		Document document = new Document();
		Indexable<?> indexable = new Indexable<Object>() {};
		IndexManager.addStringField(IConstants.ID, id, indexable, document);
		IndexManager.addStringField(IConstants.NAME, string, indexable, document);
		if (StringUtils.isNumeric(string.trim())) {
			// logger.info("Adding numeric field : " + string);
			IndexManager.addNumericField(field, string.trim(), document, Store.YES);
		} else {
			IndexManager.addStringField(field, string, indexable, document);
		}
		return document;
	}

	protected Lock getLock(Directory directory, File serverIndexDirectory) throws IOException {
		logger.info("Is locked : " + IndexWriter.isLocked(directory));
		Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		boolean gotLock = lock.obtain(Lock.LOCK_OBTAIN_WAIT_FOREVER);
		logger.info("Got lock : " + gotLock + ", is locked : " + lock.isLocked());
		if (!gotLock) {
			FileUtilities.getFile(new File(serverIndexDirectory, IndexWriter.WRITE_LOCK_NAME).getAbsolutePath(), Boolean.FALSE);
		} else {
			assertTrue(IndexWriter.isLocked(directory));
		}
		logger.info("Is now locked : " + IndexWriter.isLocked(directory));
		return lock;
	}

	/**
	 * This method will just print the data in the index reader.L
	 * 
	 * @param indexReader the reader to print the documents for
	 * @throws Exception
	 */
	protected void printIndex(final IndexReader indexReader, final int numDocs) throws Exception {
		logger.info("Num docs : " + indexReader.numDocs());
		FieldInfos fieldInfos = ReaderUtil.getMergedFieldInfos(indexReader);
		Iterator<FieldInfo> iterator = fieldInfos.iterator();
		while (iterator.hasNext()) {
			FieldInfo fieldInfo = iterator.next();
			IndexOptions indexOptions = fieldInfo.indexOptions;
			logger.info("Field : " + fieldInfo.name + ", " + fieldInfo.number + ", " + indexOptions.name());
		}
		for (int i = 0; i < numDocs && i < indexReader.numDocs(); i++) {
			Document document = indexReader.document(i);
			logger.info("Document : " + i);
			printDocument(document);
		}
	}

	protected void printDocument(final Document document) {
		List<Fieldable> fields = document.getFields();
		for (Fieldable fieldable : fields) {
			String fieldName = fieldable.name();
			String fieldValue = fieldable.stringValue();
			int fieldLength = fieldValue != null ? fieldValue.length() : 0;
			logger.info("        : " + fieldName + ", " + fieldLength + ", " + fieldValue);
		}
	}
	
	protected Analysis<String, double[]> getAnalysis(final String clazz, final String input) {
		Analysis<String, double[]> analysis = new Analysis<String, double[]>();
		analysis.setClazz(clazz);
		analysis.setInput(input);
		return analysis;
	}

}