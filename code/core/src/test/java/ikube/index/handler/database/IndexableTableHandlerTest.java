package ikube.index.handler.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.ThreadUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class IndexableTableHandlerTest extends BaseTest {

	private IndexableTable faqIndexableTable;
	private IndexableColumn faqIdIndexableColumn;
	private IndexableTable attachmentIndexableTable;
	private List<Indexable<?>> faqIndexableColumns;
	private IndexableTableHandler indexableTableHandler;
	private Connection connection;

	@Before
	public void before() throws Exception {
		indexableTableHandler = ApplicationContextManager.getBean(IndexableTableHandler.class);

		faqIndexableTable = ApplicationContextManager.getBean("tableOne");
		attachmentIndexableTable = ApplicationContextManager.getBean("tableTwo");

		faqIndexableColumns = faqIndexableTable.getChildren();
		faqIdIndexableColumn = indexableTableHandler.getIdColumn(faqIndexableColumns);

		connection = ApplicationContextManager.getBean(DataSource.class).getConnection();
	}

	@After
	public void after() {
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.setWorking(indexContext.getIndexName(), faqIndexableTable.getName(), Boolean.FALSE);
	}

	@Test
	public void handleTable() throws Exception {
		// IndexContext, IndexableTable, Connection, Document
		indexContext.getIndex().setIndexWriter(INDEX_WRITER);
		List<Thread> threads = indexableTableHandler.handle(indexContext, faqIndexableTable);
		ThreadUtilities.waitForThreads(threads);
		// We just need to succeed, the integration tests test the
		// data that is indexed and validates it
	}

	@Test
	public void buildSql() throws Exception {
		String expectedSql = "select faq.faqId, faq.creationtimestamp, faq.modifiedtimestamp, "
				+ "faq.creator, faq.modifier, faq.question, faq.answer, faq.published "
				+ "from faq where faq.faqid > 0 and faq.faqId >= 0 and faq.faqId < 10";
		// IndexContext, IndexableTable, long
		long nextIdNumber = 0;
		String sql = indexableTableHandler.buildSql(faqIndexableTable, indexContext.getBatchSize(), nextIdNumber);
		logger.debug("Sql : " + sql);
		assertEquals(expectedSql, sql);
	}

	@Test
	public void getIdFunction() throws Exception {
		// IndexableTable, Connection, String
		long minId = indexableTableHandler.getIdFunction(faqIndexableTable, connection, "min");
		logger.debug("Min id : " + minId);
		assertEquals(1, minId);
		long maxId = indexableTableHandler.getIdFunction(faqIndexableTable, connection, "max");
		logger.debug("Max id : " + maxId);
		assertTrue(maxId < 1000);
	}

	@Test
	public void getIdColumn() throws Exception {
		// List<Indexable<?>>
		IndexableColumn idColumn = indexableTableHandler.getIdColumn(faqIndexableTable.getChildren());
		assertNotNull(idColumn);
		assertEquals("faqId", idColumn.getName());
	}

	@Test
	public void setParameters() throws Exception {
		// IndexableTable, PreparedStatement
		faqIdIndexableColumn.setContent(1);
		String sql = "select * from attachment where faqId = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		indexableTableHandler.setParameters(attachmentIndexableTable, preparedStatement);
		// Execute this statement just for shits and giggles
		ResultSet resultSet = preparedStatement.executeQuery();
		assertNotNull(resultSet);

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(preparedStatement);
	}

	@Test
	public void getResultSet() throws Exception {
		// IndexContext, IndexableTable, Connection
		faqIdIndexableColumn.setContent(1);
		ResultSet resultSet = indexableTableHandler.getResultSet(indexContext, faqIndexableTable, connection);
		assertNotNull(resultSet);

		Statement statement = resultSet.getStatement();
		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

	@Test
	public void handleColumn() throws Exception {
		// IndexableColumn, Document
		IndexableColumn faqIdIndexableColumn = indexableTableHandler.getIdColumn(faqIndexableColumns);
		faqIdIndexableColumn.setContent("Hello World!");
		faqIdIndexableColumn.setColumnType(Types.VARCHAR);
		Document document = new Document();
		indexableTableHandler.handleColumn(faqIdIndexableColumn, document);
		// This must just succeed as the sub components are tested separately
		assertTrue(Boolean.TRUE);
	}

	@Test
	public void setIdField() throws Exception {
		// List<Indexable<?>>, IndexableTable, Document, ResultSet
		Document document = new Document();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select * from faq");
		resultSet.next();
		indexableTableHandler.setColumnTypesAndData(faqIndexableColumns, resultSet);
		indexableTableHandler.setIdField(faqIndexableTable, document);
		logger.debug("Document : " + document);
		String idFieldValue = document.get(IConstants.ID);
		logger.debug("Id field : " + idFieldValue);
		assertEquals("faq.faqId.1", idFieldValue);

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

	@Test
	public void setColumnTypes() throws Exception {
		// List<Indexable<?>>, ResultSet
		faqIdIndexableColumn.setColumnType(0);
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select * from faq");
		resultSet.next();
		indexableTableHandler.setColumnTypesAndData(faqIndexableColumns, resultSet);
		logger.debug("Faq id column type : " + faqIdIndexableColumn.getColumnType());
		assertEquals(Types.BIGINT, faqIdIndexableColumn.getColumnType());

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

}