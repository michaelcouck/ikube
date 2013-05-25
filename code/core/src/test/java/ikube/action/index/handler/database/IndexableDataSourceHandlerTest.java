package ikube.action.index.handler.database;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.model.Indexable;
import ikube.model.IndexableDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableDataSourceHandlerTest extends AbstractTest {

	private IndexableDataSourceHandler indexableTableHandler;

	@Before
	public void before() {
		indexableTableHandler = new IndexableDataSourceHandler();
	}

	@Test
	public void handle() throws Exception {
		IndexableDataSource indexableDataSource = mock(IndexableDataSource.class);
		Connection connection = mock(Connection.class);
		DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
		DataSource dataSource = mock(DataSource.class);
		ResultSet resultSet = mock(ResultSet.class);

		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.getMetaData()).thenReturn(databaseMetaData);
		when(databaseMetaData.getTables(anyString(), anyString(), anyString(), any(String[].class))).thenReturn(resultSet);
		when(resultSet.next()).thenAnswer(new Answer<Boolean>() {

			boolean first = Boolean.TRUE;

			@Override
			public Boolean answer(InvocationOnMock arg0) throws Throwable {
				try {
					return first;
				} finally {
					first = Boolean.FALSE;
				}
			}
		});
		when(resultSet.getString(anyString())).thenReturn("tableName");
		when(indexableDataSource.getExcludedTablePatterns()).thenReturn("someOtherTableName");
		when(indexableDataSource.getDataSource()).thenReturn(dataSource);

		indexableTableHandler.handleIndexable(indexContext, indexableDataSource);

		verify(indexContext, atLeastOnce()).getChildren();
	}

	@Test
	public void isExcluded() {
		Boolean isExcluded = Deencapsulation.invoke(indexableTableHandler, "isExcluded", "tableName", "excluded:table:patterns");
		assertFalse(isExcluded);
		isExcluded = Deencapsulation.invoke(indexableTableHandler, "isExcluded", "tableName", "excluded:tableName:patterns");
		assertTrue(isExcluded);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void isInContextAlready() {
		Indexable<?> child = mock(Indexable.class);
		when(child.getName()).thenReturn("tableName");
		List<Indexable<?>> children = new ArrayList<Indexable<?>>(Arrays.asList(child));
		when(indexContext.getChildren()).thenReturn(children);
		Boolean isInContextAlready = Deencapsulation.invoke(indexableTableHandler, "isInContextAlready", "tableOtherName", indexContext);
		assertFalse(isInContextAlready);

		isInContextAlready = Deencapsulation.invoke(indexableTableHandler, "isInContextAlready", "tableName", indexContext);
		assertTrue(isInContextAlready);
	}

}