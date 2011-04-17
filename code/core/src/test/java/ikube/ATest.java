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
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.model.Index;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.model.Server;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;

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
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;

/**
 * This is the base test for all mocked tests. There are several useful mocks in this class that can be re-used like the index context and
 * the index reader etc. There are also helpful methods for creating Lucene indexes.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class ATest {

	static {
		Logging.configure();
		new MimeTypes(IConstants.MIME_TYPES);
		new MimeMapper(IConstants.MIME_MAPPING);
	}

	protected Logger logger;

	/** These are all mocked objects that are used in sub classes. */
	protected String IP;
	protected ScoreDoc[] SCORE_DOCS;
	protected Searchable[] SEARCHABLES;
	protected List<Indexable<?>> INDEXABLES;

	protected Lock LOCK = mock(Lock.class);
	protected Index INDEX = mock(Index.class);
	protected Server SERVER = mock(Server.class);
	protected TopDocs TOP_DOCS = mock(TopDocs.class);
	protected FSDirectory FS_DIRECTORY = mock(FSDirectory.class);
	protected IndexWriter INDEX_WRITER = mock(IndexWriter.class);
	protected IndexReader INDEX_READER = mock(IndexReader.class);
	protected IndexContext INDEX_CONTEXT = mock(IndexContext.class);
	protected TopFieldDocs TOP_FIELD_DOCS = mock(TopFieldDocs.class);
	protected MultiSearcher MULTI_SEARCHER = mock(MultiSearcher.class);
	protected IndexSearcher INDEX_SEARCHER = mock(IndexSearcher.class);
	protected IClusterManager CLUSTER_MANAGER = mock(IClusterManager.class);
	protected IndexableInternet INDEXABLE = mock(IndexableInternet.class);

	protected String indexDirectoryPath = "./indexes";
	protected String indexDirectoryPathBackup = "./indexes/backup";

	public ATest(Class<?> subClass) {
		logger = Logger.getLogger(subClass);
		SEARCHABLES = new Searchable[] { INDEX_SEARCHER };
		SCORE_DOCS = new ScoreDoc[0];
		INDEXABLES = new ArrayList<Indexable<?>>();

		try {
			IP = InetAddress.getLocalHost().getHostAddress();
			when(INDEX_SEARCHER.getIndexReader()).thenReturn(INDEX_READER);
			when(INDEX_SEARCHER.search(any(Query.class), anyInt())).thenReturn(TOP_DOCS);

			when(MULTI_SEARCHER.getSearchables()).thenReturn(SEARCHABLES);
			when(MULTI_SEARCHER.search(any(Query.class), anyInt())).thenReturn(TOP_DOCS);
			when(MULTI_SEARCHER.search(any(Query.class), any(Filter.class), anyInt(), any(Sort.class))).thenReturn(TOP_FIELD_DOCS);
		} catch (Exception e) {
			e.printStackTrace();
		}

		TOP_DOCS.totalHits = 0;
		TOP_DOCS.scoreDocs = SCORE_DOCS;
		TOP_FIELD_DOCS.totalHits = 0;
		TOP_FIELD_DOCS.scoreDocs = SCORE_DOCS;
		when(INDEX_READER.directory()).thenReturn(FS_DIRECTORY);
		when(FS_DIRECTORY.makeLock(anyString())).thenReturn(LOCK);

		when(INDEX_CONTEXT.getIndexDirectoryPath()).thenReturn(indexDirectoryPath);
		when(INDEX_CONTEXT.getIndexDirectoryPathBackup()).thenReturn(indexDirectoryPathBackup);
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
		when(SERVER.getWorking()).thenReturn(Boolean.FALSE);
		when(SERVER.getAddress()).thenReturn(IP);
		when(SERVER.getIp()).thenReturn(IP);
		when(INDEX.getIndexWriter()).thenReturn(INDEX_WRITER);
		INDEXABLES.add(INDEXABLE);
		when(INDEXABLE.getUrl()).thenReturn("http://ikokoon.ikube.cloudbees.net/index.html");
		when(INDEXABLE.isAddress()).thenReturn(Boolean.TRUE);
		when(INDEXABLE.getContent()).thenReturn("9 avenue road, cape town, south africa");

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
		return IndexManager.getIndexDirectory(indexContext, System.currentTimeMillis(), IP);
	}

	protected File createIndex(IndexContext indexContext, String... strings) {
		IndexWriter indexWriter = null;
		try {
			indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), IP);
			Document document = new Document();
			IndexManager.addStringField(IConstants.CONTENTS, "Michael Couck", document, Store.YES, Field.Index.ANALYZED, TermVector.YES);
			indexWriter.addDocument(document);
			for (String string : strings) {
				document = new Document();
				IndexManager.addStringField(IConstants.CONTENTS, string, document, Store.YES, Field.Index.ANALYZED, TermVector.YES);
				indexWriter.addDocument(document);
			}
		} catch (Exception e) {
			logger.error("Exception creating the index : ", e);
		} finally {
			IndexManager.closeIndexWriter(indexWriter);
		}
		// In some cases the index is not created for!!!??? Strange as this may sound
		// we have to have the index created, or at least look like it is created, we make
		// sure that the primary files for the Lucene index are there
		// String segments = "segments_2";
		// String segmentsGen = "segments.gen";
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		File serverIndexDirectory = new File(latestIndexDirectory, IP);
		// File segmentsGenFile = FileUtilities.findFile(serverIndexDirectory, segmentsGen);
		// if (segmentsGenFile == null) {
		// try {
		// logger.info("Index exists : " + IndexReader.indexExists(FSDirectory.open(serverIndexDirectory)));
		// } catch (IOException e) {
		// logger.error("Exception checking index : ", e);
		// }
		// logger.info("Didn't create index at : " + serverIndexDirectory.getAbsolutePath());
		// logger.info("Will try to create the index dummy : ");
		// FileUtilities.getFile(serverIndexDirectory.getAbsolutePath() + "/" + segments, Boolean.FALSE);
		// FileUtilities.getFile(serverIndexDirectory.getAbsolutePath() + "/" + segmentsGen, Boolean.FALSE);
		// }
		logger.info("Created index in : " + serverIndexDirectory.getAbsolutePath());
		return latestIndexDirectory;
	}

}