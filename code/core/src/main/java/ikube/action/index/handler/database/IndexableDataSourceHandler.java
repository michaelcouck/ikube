package ikube.action.index.handler.database;

import ikube.IConstants;
import ikube.action.index.handler.IndexableHandler;
import ikube.database.DatabaseUtilities;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableDataSource;
import ikube.model.IndexableTable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 03-01-2012
 */
public class IndexableDataSourceHandler extends IndexableHandler<IndexableDataSource> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ForkJoinTask<?> handleIndexableForked(
            final IndexContext indexContext,
            final IndexableDataSource indexableDataSource)
            throws Exception {
        return new RecursiveTask<Object>() {
            @Override
            protected Object compute() {
                try {
                    addAllTables(indexContext, indexableDataSource);
                } catch (final SQLException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };
    }

    private void addAllTables(
            final IndexContext indexContext,
            final IndexableDataSource indexableDataSource)
            throws SQLException {
        // We just add all the tables individually to the index context, only if they are not there of course
        ResultSet resultSet = null;
        String excludedTablePatterns = indexableDataSource.getExcludedTablePatterns();
        try {
            DataSource dataSource = indexableDataSource.getDataSource();
            Connection connection = dataSource.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            resultSet = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (isExcluded(tableName, excludedTablePatterns)) {
                    logger.info("Excluding table : " + tableName);
                    continue;
                }
                if (isInContextAlready(tableName, indexContext)) {
                    continue;
                }
                IndexableTable indexableTable = getIndexableTable(tableName, indexableDataSource);
                logger.info("Dynamically adding table to context : " + indexableTable.getName());
                indexContext.getChildren().add(indexableTable);
            }
        } finally {
            if (resultSet != null) {
                DatabaseUtilities.closeAll(resultSet);
            }
        }
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
        indexableTable.setStored(indexable.isStored());
        indexableTable.setStrategies(indexable.getStrategies());
        indexableTable.setVectored(indexable.isVectored());
        return indexableTable;
    }

    private boolean isInContextAlready(final String tableName, final IndexContext indexContext) {
        for (final Indexable indexable : indexContext.getChildren()) {
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

    @Override
    protected List<?> handleResource(final IndexContext indexContext, final IndexableDataSource indexableDataSource, final Object resource) {
        logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
        return null;
    }

}