package ikube.index.handler.database;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.content.ColumnContentProvider;
import ikube.index.content.IContentProvider;
import ikube.index.handler.Handler;
import ikube.index.handler.IHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.DatabaseUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

public class IndexableTableHandler extends Handler {

	private IContentProvider<IndexableColumn> contentProvider;
	private Comparator<Indexable<?>> columnComparator = new Comparator<Indexable<?>>() {
		@Override
		public int compare(Indexable<?> o1, Indexable<?> o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	public IndexableTableHandler(IHandler<Indexable<?>> previous) {
		super(previous);
		this.contentProvider = new ColumnContentProvider();
	}

	@Override
	public void handle(IndexContext indexContext, Indexable<?> indexable) {
		handle(indexContext, indexable, null);
		// Do the next handler in the chain
		super.handle(indexContext, indexable);
	}

	protected void handle(IndexContext indexContext, Indexable<?> indexable, Document document) {
		if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
			IndexableTable indexableTable = (IndexableTable) indexable;
			List<Indexable<?>> children = indexableTable.getChildren();
			Collections.sort(children, columnComparator);

			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			try {
				String sql = buildSql(indexableTable);
				connection = indexableTable.getDataSource().getConnection();
				preparedStatement = connection.prepareStatement(sql);
				setParameters(indexableTable, preparedStatement);
				resultSet = preparedStatement.executeQuery();
				// Set the column types
				ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					String columnName = resultSetMetaData.getColumnName(i);
					int index = getColumnIndex(children, columnName);
					if (index < 0) {
						continue;
					}
					IndexableColumn indexableColumn = (IndexableColumn) children.get(index);
					int columnType = resultSetMetaData.getColumnType(i);
					indexableColumn.setColumnType(columnType);
				}
				while (resultSet.next()) {
					if (indexableTable.isPrimary()) {
						document = new Document();

						IndexableColumn idColumn = getIdColumn(children);
						StringBuilder builder = new StringBuilder();
						builder.append(indexableTable.getName());
						builder.append(".");
						builder.append(idColumn.getName());
						builder.append(".");
						builder.append(resultSet.getObject(idColumn.getName()));

						IndexManager.addStringField(IConstants.ID, builder.toString(), document, Store.YES, Index.ANALYZED, TermVector.YES);
					}
					// Handle all the columns that are 'normal'
					for (Indexable<?> child : children) {
						if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
							IndexableColumn indexableColumn = (IndexableColumn) child;
							if (indexableColumn.getIndexableColumn() != null) {
								continue;
							}
							Object object = resultSet.getObject(indexableColumn.getName());
							indexableColumn.setObject(object);
							handle(indexableColumn, document);
						}
					}
					// Handle all the columns that rely on another column
					for (Indexable<?> child : children) {
						if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
							IndexableColumn indexableColumn = (IndexableColumn) child;
							if (indexableColumn.getIndexableColumn() == null) {
								continue;
							}
							Object object = resultSet.getObject(indexableColumn.getName());
							indexableColumn.setObject(object);
							handle(indexableColumn, document);
						}
					}
					// Handle all the sub tables
					for (Indexable<?> child : children) {
						if (IndexableTable.class.isAssignableFrom(child.getClass())) {
							IndexableTable childIndexableTable = (IndexableTable) child;
							handle(indexContext, childIndexableTable, document);
						}
					}
					if (indexableTable.isPrimary()) {
						logger.info("Adding document : " + document.get(IConstants.ID));
						indexContext.getIndexWriter().addDocument(document);
					}
				}
				DatabaseUtilities.close(resultSet);
				DatabaseUtilities.close(preparedStatement);
			} catch (Exception e) {
				logger.error("Exception indexing table : " + indexableTable, e);
			} finally {
				// Close the result set and statement
				DatabaseUtilities.close(resultSet);
				DatabaseUtilities.close(preparedStatement);
			}
			if (indexableTable.isPrimary()) {
				// Table done, close the connection
				DatabaseUtilities.close(connection);
			}
		}
		logger.info("Finished : " + indexable);
	}

	protected IndexableColumn getIdColumn(List<Indexable<?>> indexableColumns) {
		for (Indexable<?> indexable : indexableColumns) {
			if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				continue;
			}
			IndexableColumn indexableColumn = (IndexableColumn) indexable;
			if (indexableColumn.isIdColumn()) {
				return indexableColumn;
			}
		}
		logger.warn("No id column defined for table : " + indexableColumns);
		return null;
	}

	protected void handle(IndexableColumn indexable, Document document) {
		try {
			Object content = contentProvider.getContent(indexable);
			if (content == null) {
				return;
			}
			String fieldName = indexable.getFieldName() != null ? indexable.getFieldName() : indexable.getName();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

			String mimeType = null;
			if (indexable.getIndexableColumn() != null) {
				if (indexable.getIndexableColumn().getObject() != null) {
					mimeType = indexable.getIndexableColumn().getObject().toString();
					// logger.debug("Got mime type : " + mimeType);
				}
			}

			byte[] bytes = new byte[1024];
			InputStream inputStream = null;

			if (String.class.isAssignableFrom(content.getClass())) {
				bytes = ((String) content).getBytes(IConstants.ENCODING);
				inputStream = new ByteArrayInputStream(bytes);
			} else if (InputStream.class.isAssignableFrom(content.getClass())) {
				inputStream = (InputStream) content;
			}

			if (inputStream.markSupported()) {
				inputStream.mark(bytes.length);
				inputStream.read(bytes);
				inputStream.reset();
			}

			// logger.debug("Bytes : " + new String(bytes, IConstants.ENCODING));
			IParser parser = ParserProvider.getParser(mimeType, bytes);
			OutputStream parsedOutputStream = parser.parse(inputStream);
			// logger.debug("After parse : " + parsedOutputStream);

			if (ByteArrayOutputStream.class.isAssignableFrom(parsedOutputStream.getClass())) {
				String fieldContent = parsedOutputStream.toString();
				IndexManager.addStringField(fieldName, fieldContent, document, store, analyzed, termVector);
			} else if (FileOutputStream.class.isAssignableFrom(parsedOutputStream.getClass())) {
				Reader reader = new InputStreamReader(inputStream);
				IndexManager.addReaderField(fieldName, document, store, termVector, reader);
			} else {
				logger.error("Type not supported from the parser : " + content.getClass().getName());
			}
		} catch (Exception e) {
			logger.error("Exception accessing the column content : ", e);
		}
	}

	protected void setParameters(IndexableTable indexableTable, PreparedStatement preparedStatement) {
		List<Indexable<?>> children = indexableTable.getChildren();
		for (Indexable<?> child : children) {
			if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
				IndexableColumn indexableColumn = (IndexableColumn) child;
				if (indexableColumn.getForeignKey() != null) {
					IndexableColumn foreignKey = indexableColumn.getForeignKey();
					try {
						preparedStatement.setObject(1, foreignKey.getObject());
					} catch (SQLException e) {
						logger.error("Exception setting the parameters : " + indexableTable + ", " + indexableTable.getChildren(), e);
					}
				}
			}
		}
	}

	protected String buildSql(IndexableTable indexableTable) {
		StringBuilder builder = new StringBuilder();
		builder.append("select ");
		List<Indexable<?>> children = indexableTable.getChildren();
		boolean first = Boolean.TRUE;
		for (Indexable<?> child : children) {
			if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
				IndexableColumn indexableColumn = (IndexableColumn) child;
				if (!first) {
					builder.append(", ");
				} else {
					first = Boolean.FALSE;
				}
				builder.append(indexableTable.getName());
				builder.append(".");
				builder.append(indexableColumn.getName());
			}
		}
		builder.append(" from ");
		builder.append(indexableTable.getName());

		if (indexableTable.getPredicate() != null) {
			builder.append(" ");
			builder.append(indexableTable.getPredicate());
		}

		if (!indexableTable.isPrimary()) {
			for (Indexable<?> child : children) {
				if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
					IndexableColumn indexableColumn = (IndexableColumn) child;
					if (indexableColumn.getForeignKey() != null) {
						if (indexableTable.getPredicate() == null) {
							builder.append(" where ");
						} else {
							builder.append(" and ");
						}
						builder.append(indexableTable.getName());
						builder.append(".");
						builder.append(indexableColumn.getName());
						builder.append(" = ");
						builder.append("?");
						break;
					}
				}
			}
		}
		String sql = builder.toString();
		logger.info("Sql : " + sql);
		return sql;
	}

	protected int getColumnIndex(List<Indexable<?>> list, String name) {
		int low = 0;
		int high = list.size() - 1;
		name = name.toLowerCase();
		while (low <= high) {
			int mid = (low + high) >>> 1;
			Indexable<?> midVal = list.get(mid);
			String midValName = midVal.getName().toLowerCase();
			int cmp = midValName.compareTo(name);
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid;
		}
		return -(low + 1);
	}

}