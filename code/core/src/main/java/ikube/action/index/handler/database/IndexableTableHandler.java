package ikube.action.index.handler.database;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.content.ByteOutputStream;
import ikube.action.index.content.ColumnContentProvider;
import ikube.action.index.content.IContentProvider;
import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * This class performs the indexing of tables. It is the primary focus of Ikube. This class is essentially a database crawler, and is multi threaded. Because
 * Ikube is clusterable it means that there are two levels of threading, within this Jvm and within the cluster. The cluster synchronization is done using the
 * {@link IClusterManager}.
 * 
 * Tables are hierarchical, as such the configuration is also and the table handler will recursively call it's self to navigate the hierarchy. The operation is
 * as follows:
 * 
 * 1) Sql will be generated to select the top level table and sub tables with an inner join(to be changed to left outer perhaps)<br>
 * 2) Move to the first row<br>
 * 3) Set all the data from the sql query in the columns in the table objects<br>
 * 4) Add all the column data to the Lucene document for the current row in the category set<br>
 * 5) Repeat until all records are exhausted<br>
 * 
 * This allows arbitrarily complex data structures in databases to be indexed.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableTableHandler extends IndexableHandler<IndexableTable> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final IndexableTable indexableTable) throws Exception {
		IResourceProvider<ResultSet> resourceProvider = new TableResourceProvider(indexContext, indexableTable);
		return getRecursiveAction(indexContext, indexableTable, resourceProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final IndexableTable indexableTable, final Object resource) {
		ResultSet resultSet = (ResultSet) resource;
		IContentProvider<IndexableColumn> contentProvider = new ColumnContentProvider();
		try {
			do {
				Document document = new Document();
				// The category set is already moved to the first row, i.e. next()
				try {
					handleRow(indexContext, indexableTable, resultSet, document, contentProvider);
					// Add the document to the index
					resourceHandler.handleResource(indexContext, indexableTable, document, null);
					ThreadUtilities.sleep(indexContext.getThrottle());
				} catch (Exception e) {
					handleException(indexableTable, e, "Exception indexing table : " + indexableTable.getName());
				}
			} while (resultSet.next());
		} catch (SQLException e) {
			handleException(indexableTable, e, "Exception indexing table : " + indexableTable.getName());
		}
		DatabaseUtilities.closeAll(resultSet);
		return null;
	}

	@SuppressWarnings("rawtypes")
	protected void handleRow(final IndexContext indexContext, final IndexableTable indexableTable, final ResultSet resultSet, final Document currentDocument,
			final IContentProvider<IndexableColumn> contentProvider) throws Exception {
		// We have results from the table and we are already on the first category
		List<Indexable<?>> children = indexableTable.getChildren();
		// Set the column types and the data from the table in the column objects
		setColumnTypesAndData(children, resultSet);
		// Set the id field
		setIdField(indexableTable, currentDocument);
		for (final Indexable<?> indexable : children) {
			// Handle all the columns, if any column refers to another column then they
			// must be configured in the correct order so that the name column is before the
			// binary data for the document for example
			if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				IndexableColumn indexableColumn = (IndexableColumn) indexable;
				handleColumn(contentProvider, indexableColumn, currentDocument);
			}
		}
		for (final Indexable<?> indexable : children) {
			if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
				IndexableTable childTable = (IndexableTable) indexable;
				handleRow(indexContext, childTable, resultSet, currentDocument, contentProvider);
			}
		}
	}

	/**
	 * This method handles a column. Essentially what this means is that the data from the table is extracted and added to the document, in the field specified.
	 * 
	 * @param indexable the column to extract the data from and add to the document
	 * @param document the document to add the data to using the field name specified in the column definition
	 * @throws Exception
	 */
	protected void handleColumn(final IContentProvider<IndexableColumn> contentProvider, final IndexableColumn indexable, final Document document)
			throws Exception {
		InputStream inputStream = null;
		OutputStream parsedOutputStream = null;
		ByteOutputStream byteOutputStream = null;
		try {
			String mimeType = null;
			if (indexable.getNameColumn() != null) {
				if (indexable.getNameColumn().getContent() != null) {
					mimeType = indexable.getNameColumn().getContent().toString();
				}
			}

			byteOutputStream = new ByteOutputStream();
			contentProvider.getContent(indexable, byteOutputStream);
			if (byteOutputStream.size() == 0) {
				return;
			}

			byte[] buffer = byteOutputStream.getBytes();
			int length = Math.min(buffer.length, 1024);
			byte[] bytes = new byte[length];

			System.arraycopy(buffer, 0, bytes, 0, bytes.length);

			inputStream = new ByteArrayInputStream(buffer, 0, byteOutputStream.getCount());
			IParser parser = ParserProvider.getParser(mimeType, bytes);
			parsedOutputStream = parser.parse(inputStream, new ByteOutputStream());

			String fieldName = indexable.getFieldName() != null ? indexable.getFieldName() : indexable.getName();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;
			String fieldContent = parsedOutputStream.toString();
			if (indexable.isNumeric()) {
				if (indexable.isHashed()) {
					fieldContent = HashUtilities.hash(fieldContent).toString();
				}
				IndexManager.addNumericField(fieldName, fieldContent, document, store);
			} else {
				IndexManager.addStringField(fieldName, fieldContent, document, store, analyzed, termVector);
			}
		} finally {
			FileUtilities.close(inputStream);
			FileUtilities.close(parsedOutputStream);
			FileUtilities.close(byteOutputStream);
		}
	}

	/**
	 * Sets the id field for this document. Typically the id for the document in the index is unique in the index but it may not be. It is always a good idea to
	 * have a unique field, but a table may be indexed twice of course, in which case there will be duplicates in the id fields.
	 * 
	 * @param indexableTable the table to get the id for
	 * @param document the document to set the id field in
	 * @throws Exception
	 */
	protected void setIdField(final IndexableTable indexableTable, final Document document) {
		List<Indexable<?>> children = indexableTable.getChildren();
		IndexableColumn idColumn = QueryBuilder.getIdColumn(children);
		if (idColumn == null) {
			return;
		}

		StringBuilder builder = new StringBuilder();
		builder.append(indexableTable.getName());
		builder.append(' ');
		builder.append(idColumn.getName());
		builder.append(' ');
		builder.append(idColumn.getContent());

		String id = builder.toString();
		IndexManager.addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
	}

	/**
	 * This method sets the data from the table columns in the column objects as well as the type which is gotten from the category set emta data.
	 * 
	 * @param children the children indexables of the table object
	 * @param resultSet the category set for the table
	 * @throws Exception
	 */
	protected void setColumnTypesAndData(final List<Indexable<?>> children, final ResultSet resultSet) {
		try {
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			for (int i = 1, j = 0; i < resultSetMetaData.getColumnCount() && j < children.size(); i++) {
				String columnName = resultSetMetaData.getColumnName(i);
				Indexable<?> indexable = children.get(j);
				if (columnName.equalsIgnoreCase(indexable.getName())) {
					if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
						IndexableColumn indexableColumn = (IndexableColumn) indexable;
						Object object = resultSet.getObject(i);
						int columnType = resultSetMetaData.getColumnType(i);
						indexableColumn.setColumnType(columnType);
						indexableColumn.setContent(object);
					}
					j++;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}