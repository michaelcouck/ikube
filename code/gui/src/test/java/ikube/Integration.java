package ikube;

import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeTypes;
import ikube.listener.ListenerManager;
import ikube.listener.Scheduler;
import ikube.model.IndexContext;
import ikube.security.WebServiceAuthentication;
import ikube.service.IMonitorService;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DataUtilities;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;

@Ignore
public abstract class Integration {

	private static final Logger LOGGER = Logger.getLogger(Integration.class);

	private static boolean INITIALIZED = Boolean.FALSE;
	private static final File DOT_DIRECTORY = new File(".");

	protected static String LOCALHOST = "localhost";
	/** This client({@link HttpClient}) is for the web services. */
	protected static HttpClient HTTP_CLIENT = new HttpClient();
	protected static int SERVER_PORT = 9080;
	protected static String REST_USER_NAME = "user";
	protected static String REST_PASSWORD = "user";

	@BeforeClass
	public static void beforeClass() throws Exception {
		if (INITIALIZED) {
			return;
		}
		INITIALIZED = Boolean.TRUE;

		Logging.configure();

		new MimeTypes(IConstants.MIME_TYPES);
		new MimeMapper(IConstants.MIME_MAPPING);

		startContext();
		Thread.sleep(3000);
		insertData();
		Thread.sleep(3000);
		FileUtilities.deleteFiles(DOT_DIRECTORY, "btm1.tlog", "btm2.tlog", "ikube.h2.db", "ikube.lobs.db", "ikube.log", "openjpa.log");
		WebServiceAuthentication.authenticate(HTTP_CLIENT, LOCALHOST, SERVER_PORT, REST_USER_NAME, REST_PASSWORD);
	}

	private static void startContext() {
		ApplicationContextManager.getBean(ListenerManager.class).removeListeners();
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		dataBase.find(ikube.model.File.class, 0l);
		ApplicationContextManager.getBean(ListenerManager.class).removeListeners();
		ApplicationContextManager.getBean(Scheduler.class).shutdown();
		ThreadUtilities.destroy();
	}

	private static void insertData() throws SQLException, FileNotFoundException {
		DataSource dataSource = ApplicationContextManager.getBean("nonXaDataSourceH2");
		Connection connection = dataSource.getConnection();
		LOGGER.info("Dot directory : " + DOT_DIRECTORY.getAbsolutePath());
		File allData = FileUtilities.findFileRecursively(DOT_DIRECTORY, false, "allData");
		InputStream inputStream = new FileInputStream(allData);
		DataUtilities.setDataTypeFactory(new H2DataTypeFactory());
		DataUtilities.insertData(connection, inputStream);
	}

	public static void delete(final IDataBase dataBase, final Class<?>... klasses) {
		int batchSize = 1000;
		for (Class<?> klass : klasses) {
			try {
				List<?> list = dataBase.find(klass, 0, batchSize);
				do {
					dataBase.removeBatch(list);
					list = dataBase.find(klass, 0, batchSize);
				} while (list.size() > 0);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	protected static NameValuePair[] getNameValuePairs(String[] names, String[] values) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (int i = 0; i < names.length && i < values.length; i++) {
			NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
			nameValuePairs.add(nameValuePair);
		}
		return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
	}

	protected Logger logger = Logger.getLogger(this.getClass());
	protected IMonitorService monitorService = ApplicationContextManager.getBean(IMonitorService.class);

	/**
	 * This method creates an index using the index path in the context, the time and the ip and returns the latest index directory, i.e.
	 * the index that has just been created. Note that if there are still cascading mocks from JMockit, the index writer sill not create the
	 * index! So you have to tear down all mocks prior to using this method.
	 * 
	 * @param indexContext the index context to use for the path to the index
	 * @param strings the data that must be in the index
	 * @return the latest index directory, i.e. the one that has just been created
	 */
	protected File createIndex(IndexContext<?> indexContext, String... strings) {
		IndexWriter indexWriter = null;
		String ip = null;
		try {
			ip = UriUtilities.getIp();
			indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
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
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
		// File serverIndexDirectory = new File(latestIndexDirectory, ip);
		// logger.info("Created index in : " + serverIndexDirectory.getAbsolutePath());
		return latestIndexDirectory;
	}

}