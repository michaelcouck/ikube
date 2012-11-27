package ikube.handler.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.Integration;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.content.ColumnContentProvider;
import ikube.index.content.IContentProvider;
import ikube.index.handler.database.IndexableTableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.model.Snapshot;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.ThreadUtilities;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

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
public class IndexableTableHandlerIntegration extends Integration {

	private IDataBase dataBase;
	private IndexContext<?> indexContext;
	private IndexableTable snapshotTable;
	private IndexableColumn snapshotColumn;
	private List<Indexable<?>> snapshotTableChildren;
	private IndexableTableHandler indexableTableHandler;
	private Connection connection;

	@Before
	public void before() throws SQLException {
		ThreadUtilities.initialize();
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
		indexableTableHandler = ApplicationContextManager.getBean(IndexableTableHandler.class);

		snapshotTable = ApplicationContextManager.getBean("snapshotTable");
		snapshotTableChildren = snapshotTable.getChildren();
		snapshotColumn = Deencapsulation.invoke(indexableTableHandler, "getIdColumn", snapshotTableChildren);

		connection = ((DataSource) ApplicationContextManager.getBean("nonXaDataSourceH2")).getConnection();

		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.getServer().getActions().clear();
		indexContext = monitorService.getIndexContext("indexContext");
	}

	@After
	public void after() {
		ThreadUtilities.destroy();
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.getServer().getActions().clear();
		DatabaseUtilities.close(connection);
	}

	private long getMinId() {
		return dataBase.find(Snapshot.class, new String[] { "id" }, new Boolean[] { true }, 0, 1).get(0).getId();
	}

	private long getMaxId() {
		return dataBase.find(Snapshot.class, new String[] { "id" }, new Boolean[] { false }, 0, 1).get(0).getId();
	}

	@Test
	public void handleTableSingleRow() throws Exception {
		String predicate = snapshotTable.getPredicate();
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
			indexContext.setIndexWriter(indexWriter);
			Long minId = getMinId();
			snapshotTable.setPredicate("where snapshot.id = " + minId);
			List<Future<?>> threads = indexableTableHandler.handle(indexContext, snapshotTable);
			ThreadUtilities.waitForFutures(threads, Integer.MAX_VALUE);
			assertEquals("There must be exactly one document in the index : ", 1, indexContext.getIndexWriter().numDocs());
		} finally {
			snapshotTable.setPredicate(predicate);
		}
	}

	@Test
	public void buildSql() throws Exception {
		indexContext.setBatchSize(1000);
		String expectedSql = "select snapshot.id, snapshot.numdocs, snapshot.timestamp, snapshot.availableprocessors, "
				+ "snapshot.docsperminute, snapshot.indexsize, snapshot.latestindextimestamp, snapshot.searchesperminute, "
				+ "snapshot.systemload, snapshot.totalsearches from snapshot";
		long nextIdNumber = 0;
		long batchSize = indexContext.getBatchSize();
		Deencapsulation.invoke(indexableTableHandler, "addAllColumns", snapshotTable, connection);
		String sql = Deencapsulation.invoke(indexableTableHandler, "buildSql", snapshotTable, batchSize, nextIdNumber);
		sql = sql.toLowerCase();
		assertTrue(sql.contains(expectedSql));
	}

	@Test
	public void getIdFunction() throws Exception {
		Long jpaMinId = getMinId();
		Long jpaMaxId = getMaxId();
		Long minId = Deencapsulation.invoke(indexableTableHandler, "getIdFunction", snapshotTable, connection, "min");
		assertTrue("The min id should be : " + jpaMinId, minId.equals(jpaMinId));
		Long maxId = Deencapsulation.invoke(indexableTableHandler, "getIdFunction", snapshotTable, connection, "max");
		assertTrue("The max id should be " + jpaMaxId, maxId.equals(jpaMaxId));
	}

	@Test
	public void getIdColumn() throws Exception {
		IndexableColumn idColumn = Deencapsulation.invoke(indexableTableHandler, "getIdColumn", snapshotTable.getChildren());
		assertNotNull(idColumn);
		assertEquals("id", idColumn.getName());
	}

	@Test
	public void setParameters() throws Exception {
		try {
			IndexableTable indexContextTable = ApplicationContextManager.getBean("indexContextTable");
			IndexableColumn indexContextIdColumn = Deencapsulation.invoke(indexableTableHandler, "getIdColumn",
					indexContextTable.getChildren());
			snapshotColumn.setForeignKey(indexContextIdColumn);
			snapshotColumn.setContent(1);
			String sql = "select * from snapshot where id = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			Deencapsulation.invoke(indexableTableHandler, "setParameters", snapshotTable, preparedStatement);
			// Execute this statement just for shits and giggles
			ResultSet resultSet = preparedStatement.executeQuery();
			assertNotNull(resultSet);

			DatabaseUtilities.close(resultSet);
			DatabaseUtilities.close(preparedStatement);
		} finally {
			snapshotColumn.setForeignKey(null);
		}
	}

	@Test
	public void getResultSet() throws Exception {
		long minId = getMinId();
		snapshotColumn.setContent(minId);
		ResultSet resultSet = Deencapsulation.invoke(indexableTableHandler, "getResultSet", indexContext, snapshotTable, connection,
				new AtomicLong(0), 1);
		assertNotNull(resultSet);

		Statement statement = resultSet.getStatement();
		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

	@Test
	public void handleColumn() throws Exception {
		IndexableColumn snapshotIdIndexableColumn = Deencapsulation.invoke(indexableTableHandler, "getIdColumn", snapshotTableChildren);
		snapshotIdIndexableColumn.setContent("Hello World!");
		snapshotIdIndexableColumn.setColumnType(Types.VARCHAR);
		Document document = new Document();
		IContentProvider<IndexableColumn> contentProvider = new ColumnContentProvider();
		Deencapsulation.invoke(indexableTableHandler, "handleColumn", contentProvider, snapshotIdIndexableColumn, document);
		// This must just succeed as the sub components are tested separately
		assertTrue(Boolean.TRUE);
	}

	@Test
	public void setIdField() throws Exception {
		Document document = new Document();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select * from snapshot");
		resultSet.next();

		Deencapsulation.invoke(indexableTableHandler, "setColumnTypesAndData", snapshotTableChildren, resultSet);
		Deencapsulation.invoke(indexableTableHandler, "setIdField", snapshotTable, document);

		String idFieldValue = document.get(IConstants.ID);
		assertTrue("The id field for the table is the name of the table and the column name, then the value : " + idFieldValue,
				idFieldValue.contains("snapshot id"));

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

	@Test
	public void setColumnTypes() throws Exception {
		snapshotColumn.setColumnType(0);
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select * from snapshot");
		resultSet.next();

		Deencapsulation.invoke(indexableTableHandler, "setColumnTypesAndData", snapshotTableChildren, resultSet);

		assertEquals("Snapshot id column type : " + snapshotColumn.getColumnType(), Types.BIGINT, snapshotColumn.getColumnType());

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

	@Test
	public void handleTable() throws Exception {
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
		indexContext.setIndexWriter(indexWriter);
		List<Future<?>> threads = indexableTableHandler.handle(indexContext, snapshotTable);
		ThreadUtilities.waitForFutures(threads, Integer.MAX_VALUE);
		assertTrue("There must be some data in the index : ", indexContext.getIndexWriter().numDocs() > 0);
	}

	@Test
	public void handleAllColumnsTable() throws Exception {
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
		indexContext.setIndexWriter(indexWriter);
		List<Future<?>> futures = indexableTableHandler.handle(indexContext, snapshotTable);
		ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		assertTrue("There must be some data in the index : ", indexContext.getIndexWriter().numDocs() > 0);
	}

	@Test
	public void handleAllColumnsAllTables() throws Exception {
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
		indexContext.setIndexWriter(indexWriter);
		for (Indexable<?> indexable : indexContext.getChildren()) {
			if (!IndexableTable.class.isAssignableFrom(indexable.getClass())) {
				continue;
			}
			if (!((IndexableTable) indexable).isPrimaryTable()) {
				continue;
			}
			try {
				List<Future<?>> futures = indexableTableHandler.handle(indexContext, (IndexableTable) indexable);
				ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		assertTrue("There must be some data in the index : ", indexContext.getIndexWriter().numDocs() > 0);
	}

	@Test
	public void interrupt() throws Exception {
		try {
			long start = System.currentTimeMillis();
			indexContext.setBatchSize(10);
			indexContext.setThrottle(60000);
			indexContext.setIndexWriter(IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), InetAddress.getLocalHost()
					.getHostAddress()));
			Thread thread = new Thread(new Runnable() {
				public void run() {
					ThreadUtilities.sleep(10000);
					ThreadUtilities.destroy();
				}
			});
			thread.setDaemon(Boolean.TRUE);
			thread.start();
			List<Future<?>> futures = indexableTableHandler.handle(indexContext, snapshotTable);
			ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
			// We should get here when the futures are interrupted
			assertTrue(Boolean.TRUE);
			assertTrue(System.currentTimeMillis() - start < 60000);
		} finally {
			ThreadUtilities.initialize();
			indexContext.setThrottle(0);
		}
	}

}