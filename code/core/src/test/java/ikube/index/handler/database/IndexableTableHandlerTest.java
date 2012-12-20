package ikube.index.handler.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.DatabaseUtilities;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

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
public class IndexableTableHandlerTest extends ATest {

	private static List<String> PRIMARY_KEYS = Arrays.asList("id");
	private static List<String> ALL_COLUMNS = Arrays.asList("id", "name", "address");

	@MockClass(realClass = DatabaseUtilities.class)
	public static class DatabaseUtilitiesMock {
		@Mock()
		public static List<String> getAllColumns(final Connection connection, final String table) {
			return ALL_COLUMNS;
		}

		@Mock()
		public static List<String> getPrimaryKeys(final Connection connection, final String table) {
			return PRIMARY_KEYS;
		}
	}

	private IndexableTableHandler indexableTableHandler;

	public IndexableTableHandlerTest() {
		super(IndexableTableHandlerTest.class);
	}

	@Before
	public void before() {
		indexableTableHandler = new IndexableTableHandler();
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
		indexableTableHandler.addAllColumns(indexableTable, dataSource);
		assertEquals("There should be three columns added : ", ALL_COLUMNS.size(), indexableTable.getChildren().size());
		IndexableColumn indexableColumn = (IndexableColumn) indexableTable.getChildren().get(0);
		assertTrue("The first column should be the id column : ", indexableColumn.isIdColumn());
	}

}