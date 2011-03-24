package ikube;

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
import ikube.logging.Logging;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.model.Index;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.model.Server;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.RAMDirectory;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class ATest {

	static {
		Logging.configure();
		new MimeTypes(IConstants.MIME_TYPES);
		new MimeMapper(IConstants.MIME_MAPPING);
		// Every time the JVM starts a +~JF#######.tmp file is created. Strange as that is
		// we still need to delete it manually.
		// FileUtilities.deleteFiles(new File(System.getProperty("user.home")), ".tmp");
	}

	protected Logger logger;

	/** These are all mocked objects that are used in sub classes. */
	protected String IP;
	protected Lock LOCK;
	protected Index INDEX;
	protected Server SERVER;
	protected TopDocs TOP_DOCS;
	protected ScoreDoc[] SCORE_DOCS;
	protected FSDirectory FS_DIRECTORY;
	protected Searchable[] SEARCHABLES;
	protected IndexReader INDEX_READER;
	protected IndexContext INDEX_CONTEXT;
	protected TopFieldDocs TOP_FIELD_DOCS;
	protected MultiSearcher MULTI_SEARCHER;
	protected IndexSearcher INDEX_SEARCHER;
	protected IClusterManager CLUSTER_MANAGER;
	protected IndexWriter INDEX_WRITER;
	protected List<Indexable<?>> INDEXABLES;
	protected IndexableInternet INDEXABLE;

	public ATest(Class<?> subClass) {
		logger = Logger.getLogger(subClass);
		MULTI_SEARCHER = mock(MultiSearcher.class);
		INDEX_SEARCHER = mock(IndexSearcher.class);
		INDEX_READER = mock(IndexReader.class);
		INDEX_WRITER = mock(IndexWriter.class);
		FS_DIRECTORY = mock(FSDirectory.class);
		SEARCHABLES = new Searchable[] { INDEX_SEARCHER };
		TOP_DOCS = mock(TopDocs.class);
		TOP_FIELD_DOCS = mock(TopFieldDocs.class);
		SCORE_DOCS = new ScoreDoc[0];
		LOCK = mock(Lock.class);
		INDEX_CONTEXT = mock(IndexContext.class);
		INDEX = mock(Index.class);
		CLUSTER_MANAGER = mock(IClusterManager.class);
		SERVER = mock(Server.class);
		INDEXABLES = new ArrayList<Indexable<?>>();
		INDEXABLE = mock(IndexableInternet.class);

		try {
			IP = InetAddress.getLocalHost().getHostAddress();
			when(INDEX_SEARCHER.search(any(Query.class), anyInt())).thenReturn(TOP_DOCS);
			when(MULTI_SEARCHER.search(any(Query.class), anyInt())).thenReturn(TOP_DOCS);
			when(MULTI_SEARCHER.search(any(Query.class), any(Filter.class), anyInt(), any(Sort.class))).thenReturn(TOP_FIELD_DOCS);
		} catch (Exception e) {
			e.printStackTrace();
		}

		when(MULTI_SEARCHER.getSearchables()).thenReturn(SEARCHABLES);
		when(INDEX_SEARCHER.getIndexReader()).thenReturn(INDEX_READER);

		TOP_DOCS.totalHits = 0;
		TOP_DOCS.scoreDocs = SCORE_DOCS;
		TOP_FIELD_DOCS.totalHits = 0;
		TOP_FIELD_DOCS.scoreDocs = SCORE_DOCS;
		when(INDEX_READER.directory()).thenReturn(FS_DIRECTORY);
		when(FS_DIRECTORY.makeLock(anyString())).thenReturn(LOCK);

		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn("./test");
		when(INDEX_CONTEXT.getIndexName()).thenReturn("index");
		when(INDEX_CONTEXT.getIndexables()).thenReturn(INDEXABLES);

		when(INDEX_CONTEXT.getIndex()).thenReturn(INDEX);
		when(INDEX.getMultiSearcher()).thenReturn(MULTI_SEARCHER);

		when(INDEX_CONTEXT.getBufferedDocs()).thenReturn(100);
		when(INDEX_CONTEXT.getBufferSize()).thenReturn(100d);
		when(INDEX_CONTEXT.getMaxFieldLength()).thenReturn(100);
		when(INDEX_CONTEXT.getMaxReadLength()).thenReturn(100l);
		when(INDEX_CONTEXT.getMergeFactor()).thenReturn(100);
		when(INDEX_CONTEXT.getMaxAge()).thenReturn((long) (1000 * 60 * 60));
		when(CLUSTER_MANAGER.getServer()).thenReturn(SERVER);
		when(SERVER.isWorking()).thenReturn(Boolean.FALSE);
		when(SERVER.getAddress()).thenReturn(IP);
		when(SERVER.getIp()).thenReturn(IP);
		when(INDEX.getIndexWriter()).thenReturn(INDEX_WRITER);
		INDEXABLES.add(INDEXABLE);
		when(INDEXABLE.getUrl()).thenReturn("http://ikube.ikube.cloudbees.net/");

		IndexManagerMock.INDEX_WRITER = INDEX_WRITER;
		ApplicationContextManagerMock.INDEX_CONTEXT = INDEX_CONTEXT;
		ApplicationContextManagerMock.CLUSTER_MANAGER = CLUSTER_MANAGER;
		when(ApplicationContextManagerMock.HANDLER.getIndexableClass()).thenReturn(IndexableInternet.class);
	}

	protected void delete(final IDataBase dataBase, final Class<?>... klasses) {
		for (Class<?> klass : klasses) {
			List<?> objects = dataBase.find(klass, 0, Integer.MAX_VALUE);
			for (Object object : objects) {
				dataBase.remove(object);
			}
		}
	}

	/**
	 * Returns the path to the latest index directory for this server and this context. The result will be something like
	 * './index/faq/1234567890/127.0.0.1'.
	 * 
	 * @param indexContext
	 *            the index context to get the directory path for
	 * @return the directory path to the latest index directory for this servers and context
	 */
	protected String getServerIndexDirectoryPath(final IndexContext indexContext) {
		return IndexManager.getIndexDirectory(IP, indexContext, System.currentTimeMillis());
	}

	protected File createIndex(final File indexDirectory, String... strings) {
		logger.info("Creating Lucene index in : " + indexDirectory);
		createIndex(indexDirectory, Boolean.FALSE, Boolean.TRUE, strings);
		return indexDirectory;
	}

	protected Directory createIndex(File indexDirectory, boolean inMemory, boolean closeDirectory, String... strings) {
		Directory directory = null;
		IndexWriter indexWriter = null;
		try {
			if (inMemory) {
				directory = new RAMDirectory();
			} else {
				directory = FSDirectory.open(indexDirectory);
			}
			indexWriter = new IndexWriter(directory, IConstants.ANALYZER, Boolean.TRUE, MaxFieldLength.UNLIMITED);
			Document document = new Document();
			IndexManager.addStringField(IConstants.CONTENTS, "Michael Couck", document, Store.YES, Field.Index.ANALYZED, TermVector.YES);
			indexWriter.addDocument(document);

			for (String string : strings) {
				document = new Document();
				IndexManager.addStringField(IConstants.CONTENTS, string, document, Store.YES, Field.Index.ANALYZED, TermVector.YES);
				indexWriter.addDocument(document);
			}

			indexWriter.commit();
			indexWriter.optimize(Boolean.TRUE);
		} catch (Exception e) {
			logger.error("Exception creating the index : " + indexDirectory, e);
		} finally {
			if (closeDirectory) {
				try {
					indexWriter.close(Boolean.FALSE);
				} catch (Exception e) {
					logger.error("Exception closing the writer : " + indexDirectory, e);
				}
				try {
					directory.close();
				} catch (Exception e) {
					logger.error("Exception closing the directory : " + indexDirectory, e);
				}
			}
		}
		return directory;
	}

}