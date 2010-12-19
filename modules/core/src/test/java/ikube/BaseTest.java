package ikube;

import ikube.cluster.ClusterIntegration;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DataGeneratorTwo;
import ikube.toolkit.DataLoader;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class BaseTest extends ATest {

	private static String SPRING_CONFIGURATION_FILE = "/spring.xml";

	static {
		ClusterIntegration.SLEEP = 1000;

		// Delete the database file
		FileUtilities.deleteFiles(new File("."), IConstants.DATABASE_FILE, ".transaction", ".odb");
		ApplicationContextManager.getApplicationContext(SPRING_CONFIGURATION_FILE);
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}

		DataLoader dataLoader = new DataLoader();
		File sqlFile = FileUtilities.findFile(new File("."), new String[] { "tables.sql" });
		dataLoader.createTables(sqlFile.getAbsolutePath());

		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				DataGeneratorTwo dataGenerator = new DataGeneratorTwo(100, 1);
				dataGenerator.generate(SPRING_CONFIGURATION_FILE);
			}
		}, "Data generator two insertion : ", 1);
	}

	protected IndexContext indexContext = ApplicationContextManager.getBean("indexContextOne");

	/**
	 * Returns the path to the latest index directory for this server and this context. The result will be something like
	 * './index/faq/1234567890/127.0.0.1'.
	 * 
	 * @param indexContext
	 *            the index context to get the directory path for
	 * @return the directory path to the latest index directory for this servers and context
	 */
	protected String getServerIndexDirectoryPath(IndexContext indexContext) {
		return IndexManager.getIndexDirectory(IP, indexContext, System.currentTimeMillis());
	}

	protected File createIndex(File indexDirectory) throws Exception {
		logger.info("Creating Lucene index in : " + indexDirectory);
		Directory directory = null;
		IndexWriter indexWriter = null;
		try {
			directory = FSDirectory.open(indexDirectory);
			indexWriter = new IndexWriter(directory, IConstants.ANALYZER, MaxFieldLength.UNLIMITED);
			Document document = new Document();
			document.add(new Field(IConstants.CONTENTS, "Michael Couck", Store.YES, Index.ANALYZED));
			indexWriter.addDocument(document);
			indexWriter.commit();
			indexWriter.optimize(Boolean.TRUE);
		} finally {
			try {
				directory.close();
			} finally {
				try {
					indexWriter.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		return indexDirectory;
	}

}