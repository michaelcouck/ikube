package ikube.index.handler.database;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This is an integration test as it will go to the database.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@Ignore
public class IndexableTableHandlerAdHocIntegration {

	private static final Logger LOGGER = Logger.getLogger(IndexableTableHandlerAdHocIntegration.class);

	private IndexContext<?> indexContext;
	private IndexableTable snapshotTable;
	private IndexableTableHandler indexableTableHandler;
	private Connection connection;

	@Before
	public void before() throws SQLException {
		ThreadUtilities.initialize();
		ApplicationContextManager
				.getApplicationContextFilesystem("C:/eclipse/workspace/ikube/code/core/src/main/resources/ikube/bpost/spring-roma-streets.xml");
		indexableTableHandler = new IndexableTableHandler();
		indexableTableHandler.setIndexableClass(IndexableTable.class);
		indexableTableHandler.setThreads(1);
		snapshotTable = ApplicationContextManager.getBean("DELIVERY_POINT2");
		connection = ((DataSource) ApplicationContextManager.getBean("nonXaDataSourceOracleBpost")).getConnection();
		indexContext = ApplicationContextManager.getBean("roma-streets");
		LOGGER.error("Connection : " + connection);
	}

	@After
	public void after() {
		DatabaseUtilities.close(connection);
	}

	@Test
	public void handleTable() throws Exception {
		String indexPath = "C:/media/nas/xfs-one/indexes/roma-streets/101010101/10.100.118.59";
		FileUtilities.deleteFile(new File(indexPath), 1);
		
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, 101010101, ip);
		indexContext.setIndexWriter(indexWriter);
		List<Future<?>> threads = indexableTableHandler.handle(indexContext, snapshotTable);
		ThreadUtilities.waitForFutures(threads, 10);
		LOGGER.error("Derstroying threads : ");
		ThreadUtilities.destroy();
		LOGGER.error("Waiting for optimise... : ");
		IndexManager.closeIndexWriter(indexWriter);
		LOGGER.error("Done : ");
		
		Directory directory = FSDirectory.open(new File(indexPath));
		IndexReader indexReader = IndexReader.open(directory);
		System.out.println("Num docs : " + indexReader.numDocs());
		for (int i = 0; i < indexReader.numDocs(); i++) {
			Document document = indexReader.document(i);
			System.out.println("Document : " + document);
		}
		indexReader.close();
	}

}