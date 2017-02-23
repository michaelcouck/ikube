package ikube.action.index.handler.database;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.URI;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This class has some methods to build specific queries, like selects based on the identifiers in a particular column,
 * and queries for functions like minimum and maximum for a particular column.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 04-06-2013
 */
public final class QueryBuilder {

    static final Logger LOGGER = Logger.getLogger(QueryBuilder.class);

    static String buildNextIdQuery(final IndexableTable indexableTable, final Long currentId) {
        StringBuilder stringBuilder = new StringBuilder();
        IndexableColumn idColumn = getIdColumn(indexableTable.getChildren());
        stringBuilder.append("select ");
        stringBuilder.append(idColumn.getName());
        stringBuilder.append(" from ");
        stringBuilder.append(indexableTable.getName());
        stringBuilder.append(" where ");
        stringBuilder.append(idColumn.getName());
        stringBuilder.append(" >= ");
        stringBuilder.append(currentId);
        return stringBuilder.toString();
    }

    static String buildCountQuery(final IndexableTable indexableTable, final Long currentId, final Long nextId) {
        StringBuilder stringBuilder = new StringBuilder();
        IndexableColumn idColumn = getIdColumn(indexableTable.getChildren());
        stringBuilder.append("select count(*) from ");
        stringBuilder.append(indexableTable.getName());
        stringBuilder.append(" where ");
        stringBuilder.append(idColumn.getName());
        stringBuilder.append(" > ");
        stringBuilder.append(currentId);
        stringBuilder.append(" and ");
        stringBuilder.append(idColumn.getName());
        stringBuilder.append(" < ");
        stringBuilder.append(nextId);
        return stringBuilder.toString();
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    static String getFunctionQuery(final String function, final String tableName, final String idColumnName) {
        StringBuilder builder = new StringBuilder("select ");
        builder.append(function);
        builder.append('(');
        builder.append(tableName);
        builder.append('.');
        builder.append(idColumnName);
        builder.append(") from ");
        builder.append(tableName);
        return builder.toString();
    }

    /**
     * Looks through the columns and returns the id column.
     *
     * @param indexableColumns the columns to look through
     * @return the id column or null if no such column is defined. Generally this will mean a configuration problem, every
     * table must have a unique id column
     */
    static IndexableColumn getIdColumn(final List<Indexable> indexableColumns) {
        for (Indexable indexable : indexableColumns) {
            if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
                continue;
            }
            IndexableColumn indexableColumn = (IndexableColumn) indexable;
            if (indexableColumn.isIdColumn()) {
                return indexableColumn;
            }
        }
        LOGGER.warn("No id column defined for table : " + indexableColumns);
        return null;
    }

    /**
     * This method returns a sql query for a particular table, starting at a particular index in the table.
     * <p/>
     * <pre>
     * 		select g.id, g.name, g.geonameid, g.city, a.id, a.geonameid from geoname g, alternatename a
     * 		where g.id > 0 and a.id > 0 and g.geonameid = a.geonameid
     * </pre>
     *
     * @param indexableTable the table to generate the sql from
     * @param nextIdNumber   the next identifier in the table, i.e. the 'from' part of the predicate
     * @param batchSize      the size of the batch, the result set size in other words
     * @return the sql to get the next batch of records from the table
     */
    String buildQuery(final IndexableTable indexableTable, final long nextIdNumber, final long batchSize) {
        Table table = new Table(indexableTable.getName());
        SelectQuery selectQuery = new SelectQuery(table);
        buildQuery(selectQuery, null, table, indexableTable);
        IndexableColumn idIndexableColumn = getIdColumn(indexableTable.getChildren());
        selectQuery.addCriteria(new MatchCriteria(table, idIndexableColumn.getName(), MatchCriteria.GREATEREQUAL, nextIdNumber));
        selectQuery.addCriteria(new MatchCriteria(table, idIndexableColumn.getName(), MatchCriteria.LESS, nextIdNumber + batchSize));
        return URI.stripCarriageReturn(selectQuery.toString()).toLowerCase();
    }

    void buildQuery(final SelectQuery selectQuery, final Table parentTable, final Table table, final IndexableTable indexableTable) {
        for (final Indexable childIndexable : indexableTable.getChildren()) {
            if (IndexableColumn.class.isAssignableFrom(childIndexable.getClass())) {
                IndexableColumn indexableColumn = (IndexableColumn) childIndexable;
                selectQuery.addColumn(table, indexableColumn.getName());
                if (indexableColumn.getForeignKey() != null) {
                    // Get the parent table and make a join with this table
                    // Note to self: This was a serious bug!
                    // IndexableColumn primaryKeyColumn = getIdColumn(((IndexableTable) indexableTable.getParent()).getChildren());
                    IndexableColumn foreignKeyColumn = indexableColumn.getForeignKey();
                    selectQuery.addJoin(parentTable, foreignKeyColumn.getName(), table, indexableColumn.getName());
                }
            } else if (IndexableTable.class.isAssignableFrom(childIndexable.getClass())) {
                IndexableTable joinedIndexableTable = (IndexableTable) childIndexable;
                Table joinedTable = new Table(joinedIndexableTable.getName());
                buildQuery(selectQuery, table, joinedTable, joinedIndexableTable);
            }
        }
    }

}