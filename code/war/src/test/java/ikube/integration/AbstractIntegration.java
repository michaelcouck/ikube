package ikube.integration;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.model.faq.Faq;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.data.DataUtilities;

import java.io.File;
import java.net.InetAddress;
import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.junit.Ignore;

@Ignore
public abstract class AbstractIntegration {

	private static final Logger	LOGGER				= Logger.getLogger(AbstractIntegration.class);

	static {
		Logging.configure();
		try {
			FileUtilities.deleteFiles(new File("."), "btm1.tlog", "btm2.tlog", "ikube.h2.db", "ikube.log", "openjpa.log");
			ApplicationContextManager.getBean(ListenerManager.class).removeListeners();
			IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
			dataBase.find(Faq.class, 0l);
			DataSource dataSource = ApplicationContextManager.getBean("nonXaDataSourceH2");
			Connection connection = dataSource.getConnection();
			String filePath = FileUtilities.findFileRecursively(new File("."), "allData.xml").getAbsolutePath();
			DataUtilities.DATA_TYPE_FACTORY = new H2DataTypeFactory();
			DataUtilities.insertData(connection, filePath);
		} catch (Exception e) {
			LOGGER.error("Exception inserting the data for the base test : ", e);
		}
	}

	protected IndexContext<?>	realIndexContext	= ApplicationContextManager.getBean("indexContext");
	protected Logger			logger				= Logger.getLogger(this.getClass());

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

	/**
	 * This method creates an index using the index path in the context, the time and the ip and returns the latest
	 * index directory, i.e. the index that has just been created. Note that if there are still cascading mocks from
	 * JMockit, the index writer sill not create the index! So you have to tear down all mocks prior to using this
	 * method.
	 * 
	 * @param indexContext
	 *            the index context to use for the path to the index
	 * @param strings
	 *            the data that must be in the index
	 * @return the latest index directory, i.e. the one that has just been created
	 */
	protected File createIndex(IndexContext<?> indexContext, String... strings) {
		IndexWriter indexWriter = null;
		String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
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
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		File serverIndexDirectory = new File(latestIndexDirectory, ip);
		logger.info("Created index in : " + serverIndexDirectory.getAbsolutePath());
		return latestIndexDirectory;
	}

}