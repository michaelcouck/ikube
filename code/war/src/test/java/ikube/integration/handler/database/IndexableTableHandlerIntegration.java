package ikube.integration.handler.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.action.Index;
import ikube.cluster.IClusterManager;
import ikube.index.IndexManager;
import ikube.index.content.ColumnContentProvider;
import ikube.index.content.IContentProvider;
import ikube.index.handler.database.IndexableTableHandler;
import ikube.integration.AbstractIntegration;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.ThreadUtilities;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import mockit.Deencapsulation;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is an integration test as it will go to the database.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class IndexableTableHandlerIntegration extends AbstractIntegration {

	private IndexableTable faqIndexableTable;
	private IndexableColumn faqIdIndexableColumn;
	private IndexableTable attachmentIndexableTable;
	private List<Indexable<?>> faqIndexableColumns;
	private IndexableTableHandler indexableTableHandler;
	private Connection connection;

	@Before
	public void before() throws Exception {
		indexableTableHandler = ApplicationContextManager.getBean(IndexableTableHandler.class);
		faqIndexableTable = ApplicationContextManager.getBean("faqTableH2");
		attachmentIndexableTable = ApplicationContextManager.getBean("attachmentTableH2");
		faqIndexableColumns = faqIndexableTable.getChildren();
		faqIdIndexableColumn = Deencapsulation.invoke(indexableTableHandler, "getIdColumn", faqIndexableColumns);
		connection = ((DataSource) ApplicationContextManager.getBean("nonXaDataSourceH2")).getConnection();
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.stopWorking(0, Index.class.getSimpleName(), realIndexContext.getIndexName(), faqIndexableTable.getName());
	}

	@After
	public void after() {
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.stopWorking(0, Index.class.getSimpleName(), realIndexContext.getIndexName(), faqIndexableTable.getName());
		DatabaseUtilities.close(connection);
	}

	@Test
	public void buildSql() throws Exception {
		realIndexContext.setBatchSize(10);
		String expectedSql = "select faq.faqId, faq.creationtimestamp, faq.modifiedtimestamp, "
				+ "faq.creator, faq.modifier, faq.question, faq.answer, faq.published from faq";
		long nextIdNumber = 0;
		long batchSize = realIndexContext.getBatchSize();
		String sql = Deencapsulation.invoke(indexableTableHandler, "buildSql", faqIndexableTable, batchSize, nextIdNumber);
		logger.info("Sql : " + sql);
		assertTrue(sql.contains(expectedSql));
	}

	@Test
	public void getIdFunction() throws Exception {
		Long minId = Deencapsulation.invoke(indexableTableHandler, "getIdFunction", faqIndexableTable, connection, "min");
		logger.debug("Min id : " + minId);
		assertTrue("This is dependant on the database being used : ", minId > 0);
		Long maxId = Deencapsulation.invoke(indexableTableHandler, "getIdFunction", faqIndexableTable, connection, "max");
		logger.debug("Max id : " + maxId);
		assertTrue(maxId < 1000000000);
	}

	@Test
	public void getIdColumn() throws Exception {
		IndexableColumn idColumn = Deencapsulation.invoke(indexableTableHandler, "getIdColumn", faqIndexableTable.getChildren());
		assertNotNull(idColumn);
		assertEquals("faqId", idColumn.getName());
	}

	@Test
	public void setParameters() throws Exception {
		faqIdIndexableColumn.setContent(1);
		String sql = "select * from attachment where faqId = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		Deencapsulation.invoke(indexableTableHandler, "setParameters", attachmentIndexableTable, preparedStatement);
		// Execute this statement just for shits and giggles
		ResultSet resultSet = preparedStatement.executeQuery();
		assertNotNull(resultSet);

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(preparedStatement);
	}

	@Test
	public void getResultSet() throws Exception {
		faqIdIndexableColumn.setContent(1);
		ResultSet resultSet = Deencapsulation.invoke(indexableTableHandler, "getResultSet", realIndexContext, faqIndexableTable,
				connection, 1);
		assertNotNull(resultSet);

		Statement statement = resultSet.getStatement();
		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

	@Test
	public void handleColumn() throws Exception {
		IndexableColumn faqIdIndexableColumn = Deencapsulation.invoke(indexableTableHandler, "getIdColumn", faqIndexableColumns);
		faqIdIndexableColumn.setContent("Hello World!");
		faqIdIndexableColumn.setColumnType(Types.VARCHAR);
		Document document = new Document();
		IContentProvider<IndexableColumn> contentProvider = new ColumnContentProvider();
		Deencapsulation.invoke(indexableTableHandler, "handleColumn", contentProvider, faqIdIndexableColumn, document);
		// This must just succeed as the sub components are tested separately
		assertTrue(Boolean.TRUE);
	}

	@Test
	public void setIdField() throws Exception {
		Document document = new Document();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select * from faq");
		resultSet.next();

		Deencapsulation.invoke(indexableTableHandler, "setColumnTypesAndData", faqIndexableColumns, resultSet);
		Deencapsulation.invoke(indexableTableHandler, "setIdField", faqIndexableTable, document);

		logger.debug("Document : " + document);
		String idFieldValue = document.get(IConstants.ID);
		logger.debug("Id field : " + idFieldValue);
		assertTrue("The id field for the table is the name of the table and the column name, then the value : ",
				idFieldValue.contains("faq.faqId"));

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

	@Test
	public void setColumnTypes() throws Exception {
		faqIdIndexableColumn.setColumnType(0);
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select * from faq");
		resultSet.next();

		Deencapsulation.invoke(indexableTableHandler, "setColumnTypesAndData", faqIndexableColumns, resultSet);

		logger.debug("Faq id column type : " + faqIdIndexableColumn.getColumnType());
		assertEquals(Types.BIGINT, faqIdIndexableColumn.getColumnType());

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

	@Test
	public void handleTable() throws Exception {
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(realIndexContext, System.currentTimeMillis(), ip);
		realIndexContext.getIndex().setIndexWriter(indexWriter);
		List<Thread> threads = indexableTableHandler.handle(realIndexContext, faqIndexableTable);
		ThreadUtilities.waitForThreads(threads);
		// TODO Verify that the data has been indexed
	}

}