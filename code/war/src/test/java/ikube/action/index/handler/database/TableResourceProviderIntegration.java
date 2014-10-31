package ikube.action.index.handler.database;

import ikube.IntegrationTest;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.model.Snapshot;
import ikube.toolkit.PropertyConfigurer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

import static ikube.action.index.handler.database.QueryBuilder.getIdColumn;
import static ikube.database.DatabaseUtilities.close;
import static ikube.toolkit.ApplicationContextManager.getBean;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 16-08-2014
 */
public class TableResourceProviderIntegration extends IntegrationTest {

    private Connection connection;
    private IndexContext indexContext;
    private IndexableTable snapshotTable;
    private IndexableColumn snapshotColumn;
    private TableResourceProvider tableResourceProvider;

    @Before
    public void before() throws SQLException, IOException {
        indexContext = getBean("indexContext");
        snapshotTable = getBean("snapshotTable");
        snapshotColumn = getIdColumn(snapshotTable.getChildren());

        PropertyConfigurer propertyConfigurer = getBean(PropertyConfigurer.class);
        DataSource dataSource = getBean(propertyConfigurer.getProperty("ikube.dataSource"));
        connection = dataSource.getConnection();

        tableResourceProvider = new TableResourceProvider(indexContext, snapshotTable);

        IDataBase dataBase = getBean(IDataBase.class);
        insert(dataBase, Snapshot.class, 10);
    }

    @After
    public void after() {
        IDataBase dataBase = getBean(IDataBase.class);
        delete(dataBase, Snapshot.class);
    }

    @Test
    public void getIdFunction() throws Exception {
        long minId = tableResourceProvider.getIdFunction(snapshotTable, connection, "min");
        assertTrue(minId >= 0);

        long maxId = tableResourceProvider.getIdFunction(snapshotTable, connection, "max");
        assertTrue(maxId >= 0);
    }

    @Test
    public void setParameters() throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            snapshotColumn.setContent(0);
            snapshotColumn.setForeignKey(snapshotColumn);
            String sql = "SELECT * FROM snapshot WHERE id >= ?";
            preparedStatement = connection.prepareStatement(sql);
            tableResourceProvider.setParameters(snapshotTable, preparedStatement);
            resultSet = preparedStatement.executeQuery();

            assertNotNull(resultSet);
            assertTrue(resultSet.next());
        } finally {
            close(resultSet);
            close(preparedStatement);
            snapshotColumn.setForeignKey(null);
        }
    }

    @Test
    public void getResultSetDatasource() throws Exception {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            snapshotColumn.setContent(snapshotTable.getMinimumId());
            snapshotTable.setMaximumId(snapshotTable.getMaximumId());

            resultSet = tableResourceProvider.getResultSet(indexContext, snapshotTable);
            statement = resultSet.getStatement();

            assertNotNull(resultSet);
            assertTrue(resultSet.next());
        } finally {
            close(resultSet);
            close(statement);
        }
    }

}
