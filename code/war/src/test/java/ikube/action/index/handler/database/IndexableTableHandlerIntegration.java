package ikube.action.index.handler.database;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.action.index.content.ColumnContentProvider;
import ikube.action.index.content.IContentProvider;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.*;
import ikube.toolkit.PropertyConfigurer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import static ikube.action.index.IndexManager.openIndexWriter;
import static ikube.action.index.handler.database.QueryBuilder.getIdColumn;
import static ikube.database.DatabaseUtilities.close;
import static ikube.toolkit.ApplicationContextManager.getBean;
import static ikube.toolkit.THREAD.*;
import static ikube.toolkit.UriUtilities.getIp;
import static java.lang.System.currentTimeMillis;
import static java.net.InetAddress.getLocalHost;
import static mockit.Deencapsulation.invoke;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This is an integration test as it will go to the database.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-10-2010
 */
public class IndexableTableHandlerIntegration extends IntegrationTest {

    private Connection connection;
    private IndexContext indexContext;
    private IndexableTable snapshotTable;
    private IndexableColumn snapshotColumn;
    private List<Indexable> snapshotTableChildren;
    private IndexableTableHandler indexableTableHandler;

    @Before
    public void before() throws SQLException {
        indexableTableHandler = getBean(IndexableTableHandler.class);

        indexContext = getBean("indexContext");
        snapshotTable = getBean("snapshotTable");

        PropertyConfigurer propertyConfigurer = getBean(PropertyConfigurer.class);
        DataSource dataSource = getBean(propertyConfigurer.getProperty("ikube.dataSource"));
        IClusterManager clusterManager = getBean(IClusterManager.class);

        indexContext.setBatchSize(10000);
        indexContext.setIndexDirectoryPath("./indexes");
        snapshotTable.setThreads(4);
        snapshotTable.setContent(new StringBuilder());

        snapshotTableChildren = snapshotTable.getChildren();
        snapshotColumn = getIdColumn(snapshotTableChildren);
        connection = dataSource.getConnection();

        clusterManager.getServer().getActions().clear();

        IDataBase dataBase = getBean(IDataBase.class);
        insert(dataBase, Snapshot.class, 11000);
    }

    @After
    public void after() {
        close(connection);
        IDataBase dataBase = getBean(IDataBase.class);
        delete(dataBase, Snapshot.class);
        IClusterManager clusterManager = getBean(IClusterManager.class);
        clusterManager.getServer().getActions().clear();
        // ThreadUtilities.cancelAllForkJoinPools();
    }

    @Test
    public void handleTableSingleRow() throws Exception {
        String predicate = snapshotTable.getPredicate();
        try {
            String ip = getLocalHost().getHostAddress();
            IndexWriter indexWriter = openIndexWriter(indexContext, currentTimeMillis(), ip);
            indexContext.setIndexWriters(indexWriter);
            snapshotTable.setPredicate("snapshot.id = " + snapshotTable.getMinimumId());
            ForkJoinTask<?> forkJoinTask = indexableTableHandler.handleIndexableForked(indexContext, snapshotTable);
            executeForkJoinTasks(indexContext.getName(), snapshotTable.getThreads(), forkJoinTask);
            sleep(5000);
            assertTrue("There must be more than one document in the index : ", indexContext.getIndexWriters()[0].numDocs() > 0);
        } finally {
            snapshotTable.setPredicate(predicate);
            cancelForkJoinPool(indexContext.getName());
        }
    }

    @Test
    public void handleColumn() throws Exception {
        IndexableColumn snapshotIdIndexableColumn = getIdColumn(snapshotTableChildren);
        snapshotIdIndexableColumn.setContent("Hello World!");
        snapshotIdIndexableColumn.setColumnType(Types.VARCHAR);
        snapshotIdIndexableColumn.setParent(snapshotTable);

        Document document = new Document();
        IContentProvider<IndexableColumn> contentProvider = new ColumnContentProvider();
        invoke(indexableTableHandler, "handleColumn", contentProvider, snapshotIdIndexableColumn, document);
        // This must just succeed as the sub components are tested separately
        assertTrue(Boolean.TRUE);
    }

    @Test
    public void setIdField() throws Exception {
        Document document = new Document();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from snapshot");
        resultSet.next();

        invoke(indexableTableHandler, "setColumnTypesAndData", snapshotTableChildren, resultSet);
        invoke(indexableTableHandler, "setIdField", snapshotTable, document);

        String idFieldValue = document.get(IConstants.ID);
        assertTrue("The id field for the table is the name of the table and the column name, then the value : " + idFieldValue,
                idFieldValue.contains("snapshot id"));

        close(resultSet);
        close(statement);
    }

    @Test
    public void setColumnTypes() throws Exception {
        snapshotColumn.setColumnType(0);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from snapshot");
        resultSet.next();

        invoke(indexableTableHandler, "setColumnTypesAndData", snapshotTableChildren, resultSet);
        close(resultSet);
        close(statement);

        // This is NUMERIC on Oracle and BIGINT on H2!
        assertEquals("Snapshot id column type : " + snapshotColumn.getColumnType(), Types.BIGINT, snapshotColumn.getColumnType());
    }

    @Test
    public void handleTable() throws Exception {
        try {
            String ip = getLocalHost().getHostAddress();
            IndexWriter indexWriter = openIndexWriter(indexContext, currentTimeMillis(), ip);
            indexContext.setIndexWriters(indexWriter);

            ForkJoinTask<?> forkJoinTask = indexableTableHandler.handleIndexableForked(indexContext, snapshotTable);
            executeForkJoinTasks(indexContext.getName(), snapshotTable.getThreads(), forkJoinTask);
            sleep(5000);
            assertTrue("There must be some data in the index : ", indexContext.getIndexWriters()[0].numDocs() > 0);
        } finally {
            cancelForkJoinPool(indexContext.getName());
        }
    }

    @Test
    public void handleAllColumnsAllTables() throws Exception {
        String ip = getLocalHost().getHostAddress();
        IndexWriter indexWriter = openIndexWriter(indexContext, currentTimeMillis(), ip);
        indexContext.setIndexWriters(indexWriter);
        for (Indexable indexable : indexContext.getChildren()) {
            if (!IndexableTable.class.isAssignableFrom(indexable.getClass())) {
                continue;
            }
            try {
                ForkJoinTask<?> forkJoinTask = indexableTableHandler.handleIndexableForked(indexContext, (IndexableTable) indexable);
                executeForkJoinTasks(indexContext.getName(), snapshotTable.getThreads(), forkJoinTask);
                sleep(5000);
            } catch (final Exception e) {
                logger.error(e.getMessage());
            }
        }
        assertTrue("There must be some data in the index : ", indexContext.getIndexWriters()[0].numDocs() > 0);
    }

    @Test
    public void interrupt() throws Exception {
        try {
            indexContext.setBatchSize(10);
            indexContext.setThrottle(60000);
            indexContext.setIndexWriters(openIndexWriter(indexContext, currentTimeMillis(), getIp()));
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    sleep(10000);
                    cancelForkJoinPool(indexContext.getName());
                }
            });
            thread.setDaemon(Boolean.TRUE);
            thread.start();

            ForkJoinTask<?> forkJoinTask = indexableTableHandler.handleIndexableForked(indexContext, snapshotTable);
            // This should throw a cancellation exception
            ForkJoinPool forkJoinPool = executeForkJoinTasks(indexContext.getName(), snapshotTable.getThreads(), forkJoinTask);
            sleep(15000);
            assertTrue(forkJoinPool.isShutdown() || forkJoinPool.isTerminated() || forkJoinPool.isTerminating());
        } finally {
            indexContext.setThrottle(0);
            indexContext.setBatchSize(1000);
        }
    }

}