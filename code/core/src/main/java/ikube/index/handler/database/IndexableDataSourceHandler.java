package ikube.index.handler.database;

import ikube.IConstants;
import ikube.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableDataSource;
import ikube.model.IndexableTable;
import ikube.toolkit.DatabaseUtilities;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import javax.sql.DataSource;

/**
 * @author Michael Couck
 * @since 03.01.2012
 * @version 01.00
 */
public class IndexableDataSourceHandler extends IndexableHandler<IndexableDataSource> {

	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableDataSource indexable) throws Exception {
		// We just add all the tables individually to the index context, only if they are not there of course
		ResultSet resultSet = null;
		String excludedTablePatterns = indexable.getExcludedTablePatterns();
		try {
			DataSource dataSource = indexable.getDataSource();
			Connection connection = dataSource.getConnection();
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			resultSet = databaseMetaData.getTables(null, null, "%", new String[] { "TABLE" });
			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME");
				if (isExcluded(tableName, excludedTablePatterns)) {
					logger.info("Excluding table : " + tableName);
					continue;
				}
				if (isInContextAlready(tableName, indexContext)) {
					continue;
				}
				IndexableTable indexableTable = getIndexableTable(tableName, indexable);
				logger.info("Dynamically adding table to context : ");
				indexContext.getChildren().add(indexableTable);
			}
		} finally {
			if (resultSet != null) {
				DatabaseUtilities.closeAll(resultSet);
			}
		}
		// We return no futures because this operation just adds the tables to the context
		return null;
	}

	private IndexableTable getIndexableTable(final String tableName, final IndexableDataSource indexable) {
		IndexableTable indexableTable = new IndexableTable();
		indexableTable.setName(tableName);
		indexableTable.setAddress(indexable.isAddress());
		indexableTable.setAllColumns(indexable.isAllColumns());
		indexableTable.setAnalyzed(indexable.isAnalyzed());
		indexableTable.setDataSource(indexable.getDataSource());
		indexableTable.setMaxExceptions(indexable.getMaxExceptions());
		indexableTable.setParent(indexable.getParent());
		indexableTable.setPrimaryTable(Boolean.TRUE);
		indexableTable.setStored(indexable.isStored());
		indexableTable.setStrategies(indexable.getStrategies());
		indexableTable.setVectored(indexable.isVectored());
		return indexableTable;
	}

	private boolean isInContextAlready(final String tableName, final IndexContext<?> indexContext) {
		for (final Indexable<?> indexable : indexContext.getChildren()) {
			if (indexable.getName().equals(tableName)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	private boolean isExcluded(final String tableName, final String excludedTablePatterns) {
		StringTokenizer stringTokenizer = new StringTokenizer(excludedTablePatterns, IConstants.DELIMITER_CHARACTERS);
		while (stringTokenizer.hasMoreTokens()) {
			String excludedTablePattern = stringTokenizer.nextToken();
			if (Pattern.matches(excludedTablePattern, tableName)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

}