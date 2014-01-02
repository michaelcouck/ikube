package ikube.action.index.handler.database;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.DatabaseUtilities;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableTableHandlerTest extends AbstractTest {

	@MockClass(realClass = DatabaseUtilities.class)
	public static class DatabaseUtilitiesMock {
		@Mock()
		public static List<String> getAllColumns(final Connection connection, final String table) {
			return Arrays.asList("id", "name", "address");
		}

		@Mock()
		public static List<String> getPrimaryKeys(final Connection connection, final String table) {
			return Arrays.asList("id");
		}
	}

	@Before
	public void before() {
		Mockit.setUpMocks(DatabaseUtilitiesMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(DatabaseUtilitiesMock.class);
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