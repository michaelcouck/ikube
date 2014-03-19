package ikube.action.index.handler.database;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.content.ColumnContentProvider;
import ikube.action.index.content.IContentProvider;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.*;
import ikube.scheduling.Scheduler;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.PropertyConfigurer;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.junit.*;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.sql.*;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicLong;

import static ikube.toolkit.ApplicationContextManager.getBean;
import static ikube.toolkit.DatabaseUtilities.close;
import static mockit.Deencapsulation.invoke;
import static org.junit.Assert.*;

/**
 * This is an integration test as it will go to the database.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.10.2010
 */
public class IndexableTableHandlerIntegration extends AbstractTest {

    private Connection connection;
    private IndexContext<?> indexContext;
    private IndexableTable snapshotTable;
    private IndexableColumn snapshotColumn;
    private List<Indexable> snapshotTableChildren;
    private IndexableTableHandler indexableTableHandler;

    @BeforeClass
    public static void beforeClass() throws Exception {
        getBean(Scheduler.class).shutdown();
        IDataBase dataBase = getBean(IDataBase.class);
        delete(dataBase, Snapshot.class);
        insert(Snapshot.class, 11000);
    }

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
        snapshotTableChildren = snapshotTable.getChildren();
        snapshotColumn = QueryBuilder.getIdColumn(snapshotTableChildren);
        connection = dataSource.getConnection();

        clusterManager.getServer().getActions().clear();
    }

    @After
    public void after() {
        close(connection);
        IClusterManager clusterManager = getBean(IClusterManager.class);
        clusterManager.getServer().getActions().clear();
        ThreadUtilities.cancelAllForkJoinPools();
    }

    @Test
    public void handleTableSingleRow() throws Exception {
        String predicate = snapshotTable.getPredicate();
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
            indexContext.setIndexWriters(indexWriter);
            snapshotTable.setPredicate("snapshot.id = " + snapshotTable.getMinimumId());
            ForkJoinTask<?> forkJoinTask = indexableTableHandler.handleIndexableForked(indexContext, snapshotTable);
            ThreadUtilities.executeForkJoinTasks(indexContext.getName(), snapshotTable.getThreads(), forkJoinTask);
            ThreadUtilities.sleep(5000);
            assertTrue("There must be more than one document in the index : ", indexContext.getIndexWriters()[0].numDocs() > 0);
        } finally {
            snapshotTable.setPredicate(predicate);
            ThreadUtilities.cancelForkJoinPool(indexContext.getName());
        }
    }

    @Test
    public void handleColumn() throws Exception {
        IndexableColumn snapshotIdIndexableColumn = QueryBuilder.getIdColumn(snapshotTableChildren);
        snapshotIdIndexableColumn.setContent("Hello World!");
        snapshotIdIndexableColumn.setColumnType(Types.VARCHAR);
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
            String ip = InetAddress.getLocalHost().getHostAddress();
            IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
            indexContext.setIndexWriters(indexWriter);

            ForkJoinTask<?> forkJoinTask = indexableTableHandler.handleIndexableForked(indexContext, snapshotTable);
            ThreadUtilities.executeForkJoinTasks(indexContext.getName(), snapshotTable.getThreads(), forkJoinTask);
            ThreadUtilities.sleep(5000);
            assertTrue("There must be some data in the index : ", indexContext.getIndexWriters()[0].numDocs() > 0);
        } finally {
            ThreadUtilities.cancelForkJoinPool(indexContext.getName());
        }
    }

    @Test
    public void handleAllColumnsAllTables() throws Exception {
        String ip = InetAddress.getLocalHost().getHostAddress();
        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
        indexContext.setIndexWriters(indexWriter);
        for (Indexable indexable : indexContext.getChildren()) {
            if (!IndexableTable.class.isAssignableFrom(indexable.getClass())) {
                continue;
            }
            try {
                ForkJoinTask<?> forkJoinTask = indexableTableHandler.handleIndexableForked(indexContext, (IndexableTable) indexable);
                ThreadUtilities.executeForkJoinTasks(indexContext.getName(), snapshotTable.getThreads(), forkJoinTask);
                ThreadUtilities.sleep(5000);
            } catch (Exception e) {
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
            indexContext.setIndexWriters(IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), UriUtilities.getIp()));
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    ThreadUtilities.sleep(10000);
                    ThreadUtilities.cancelForkJoinPool(indexContext.getName());
                }
            });
            thread.setDaemon(Boolean.TRUE);
            thread.start();

            ForkJoinTask<?> forkJoinTask = indexableTableHandler.handleIndexableForked(indexContext, snapshotTable);
            // This should throw a cancellation exception
            ForkJoinPool forkJoinPool = ThreadUtilities.executeForkJoinTasks(indexContext.getName(), snapshotTable.getThreads(), forkJoinTask);
            ThreadUtilities.sleep(15000);
            assertTrue(forkJoinPool.isShutdown() || forkJoinPool.isTerminated() || forkJoinPool.isTerminating());
        } finally {
            indexContext.setThrottle(0);
            indexContext.setBatchSize(1000);
        }
    }

    @Test
    // @Ignore("Move to table resource provider test")
    public void getIdFunction() throws Exception {
        TableResourceProvider tableResourceProvider = new TableResourceProvider(indexContext, snapshotTable);
        Long minId = invoke(tableResourceProvider, "getIdFunction", snapshotTable, connection, "min");
        assertTrue("The min id should be : " + snapshotTable.getMinimumId(), minId.equals(snapshotTable.getMinimumId()));
        Long maxId = invoke(tableResourceProvider, "getIdFunction", snapshotTable, connection, "max");
        assertTrue("The max id should be " + snapshotTable.getMaximumId(), maxId.equals(snapshotTable.getMaximumId()));
    }

    @Test
    // @Ignore("Move to table resource provider test")
    public void setParameters() throws Exception {
        TableResourceProvider tableResourceProvider = new TableResourceProvider(indexContext, snapshotTable);
        try {
            IndexableTable indexContextTable = getBean("indexContextTable");
            IndexableColumn indexContextIdColumn = QueryBuilder.getIdColumn(indexContextTable.getChildren());
            snapshotColumn.setForeignKey(indexContextIdColumn);
            snapshotColumn.setContent(1);
            String sql = "select * from snapshot where id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            invoke(tableResourceProvider, "setParameters", snapshotTable, preparedStatement);
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
    // @Ignore("Move to table resource provider test")
    public void getResultSetDatasource() throws Exception {
        TableResourceProvider tableResourceProvider = new TableResourceProvider(indexContext, snapshotTable);

        snapshotColumn.setContent(snapshotTable.getMinimumId());
        snapshotTable.setMaximumId(snapshotTable.getMaximumId());
        ResultSet resultSet = invoke(tableResourceProvider, "getResultSet", indexContext, snapshotTable, new AtomicLong(0));
        assertNotNull(resultSet);
        assertTrue(resultSet.next());

        Statement statement = resultSet.getStatement();
        DatabaseUtilities.close(resultSet);
        DatabaseUtilities.close(statement);
    }

    @Test
    @Ignore("Move to table resource provider test")
    public void getResultSetConnection() throws Exception {
        TableResourceProvider tableResourceProvider = new TableResourceProvider(indexContext, snapshotTable);

        snapshotColumn.setContent(snapshotTable.getMinimumId());
        snapshotTable.setMaximumId(snapshotTable.getMaximumId());
        ResultSet resultSet = invoke(tableResourceProvider, "getResultSet", indexContext, snapshotTable, connection, new AtomicLong(0));
        assertNotNull(resultSet);

        Statement statement = resultSet.getStatement();
        DatabaseUtilities.close(resultSet);
        DatabaseUtilities.close(statement);
    }

}