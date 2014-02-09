package ikube.action.index.handler.database;

import ikube.AbstractTest;
import ikube.mock.DatabaseUtilitiesMock;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.DatabaseUtilities;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 03-02-2014
 */
public class TableResourceProviderTest extends AbstractTest {

    @SuppressWarnings("UnusedDeclaration")
    @MockClass(realClass = QueryBuilder.class)
    public static class QueryBuilderMock {
        @Mock
        protected static IndexableColumn getIdColumn(final List<Indexable<?>> indexableColumns) {
            return (IndexableColumn) indexableColumns.get(0);
        }
    }

    @Before
    public void before() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(indexableTable.getDataSource()).thenReturn(dataSource);
        when(indexableTable.getChildren()).thenReturn(Arrays.<Indexable<?>>asList(indexableColumn));
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        Mockit.setUpMocks(QueryBuilderMock.class, DatabaseUtilitiesMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(QueryBuilder.class, DatabaseUtilities.class);
    }

    @Test
    public void getResource() throws Exception {
        // The payoff to mock is not enough!!!
    }

    @Test
    public void addAllColumns() throws Exception {
        IndexableTable indexableTable = new IndexableTable();
        Connection connection = Mockito.mock(Connection.class);
        DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        indexableTable.setDataSource(dataSource);
        indexableTable.setAllColumns(Boolean.TRUE);
        TableResourceProvider tableResourceProvider = new TableResourceProvider(indexContext, indexableTable);
        Deencapsulation.invoke(tableResourceProvider, "addAllColumns", indexableTable, dataSource);
        // indexableTableHandler.addAllColumns(indexableTable, dataSource);
        int expected = DatabaseUtilitiesMock.getAllColumns(connection, null).size();
        int actual = indexableTable.getChildren().size();
        assertTrue(expected == actual);
        IndexableColumn indexableColumn = (IndexableColumn) indexableTable.getChildren().get(0);
        assertTrue("The first column should be the id column : ", indexableColumn.isIdColumn());
    }

}