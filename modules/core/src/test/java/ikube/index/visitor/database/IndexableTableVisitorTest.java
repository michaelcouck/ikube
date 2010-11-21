package ikube.index.visitor.database;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableTableVisitorTest extends BaseTest {

	private IndexableTable indexableTable = ApplicationContextManager.getBean("faqTable");
	private List<Indexable<?>> indexableColumns = indexableTable.getChildren();
	private IndexableTableVisitor<Indexable<?>> indexableTableVisitor = ApplicationContextManager.getBean("faqTableVisitor");
	{
		Collections.sort(indexableColumns, new Comparator<Indexable<?>>() {
			@Override
			public int compare(Indexable<?> o1, Indexable<?> o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
	private IndexableColumn idColumn = indexableTableVisitor.getIdColumn(indexableColumns);

	private ResultSet resultSet = mock(ResultSet.class);
	private ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);

	@Before
	public void before() throws Exception {
		when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
	}

	@Test
	public void binarySearch() {
		// List<Indexable<?>>, String
		int index = indexableTableVisitor.getColumnIndex(indexableColumns, "faqId");
		assertTrue(index > -1);
		index = indexableTableVisitor.getColumnIndex(indexableColumns, "someOtherColumnName");
		logger.info("Column index : " + index);
		assertTrue(index < 0);
	}

	@Test
	public void doRow() throws Exception {
		// List, ResultSet
		IndexWriter indexWriter = mock(IndexWriter.class);
		indexContext.setIndexWriter(indexWriter);
		indexableTableVisitor.doRow(indexableTable, idColumn, resultSet);
		indexContext.setIndexWriter(null);
	}

	@Test
	public void getResultSet() throws Exception {
		// IndexableTable
		ResultSet resultSet = indexableTableVisitor.getResultSet(indexableTable.getDataSource().getConnection(), indexableTable, idColumn,
				0);
		assertNotNull(resultSet);
	}

	@Test
	public void getColumnIndex() {
		// List<Indexable<?>>, String
	}

	@Test
	public void getCount() {
		// Connection, IndexableTable, IndexableColumn, long
	}

	@Test
	public void getIdColumn() {
		// List<Indexable<?>>
	}

	@Test
	public void getIdNumber() {
		// Connection, IndexableTable, IndexableColumn, long
	}

	@Test
	public void getIndexableColumnVisitor() {

	}

	@Test
	public void getIndexContext() {

	}

	@Test
	public void getMaxId() {
		// Connection, IndexableTable, IndexableColumn
	}

	@Test
	public void getMinId() {
		// Connection, IndexableTable, IndexableColumn
	}

	@Test
	public void setIndexableColumnVisitor() {
		// IndexableVisitor<Indexable<?>>
	}

	@Test
	public void setIndexContext() {
		// IndexContext
	}

	@Test
	public void visit() {
		// IndexableTable
	}

}