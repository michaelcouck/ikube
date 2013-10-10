package ikube.action.index.handler.database;

import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.UriUtilities;

import java.util.List;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;

public final class QueryBuilder {

	static final Logger LOGGER = Logger.getLogger(QueryBuilder.class);

	/**
	 * <pre>
	 * 		select g.id, g.name, g.geonameid, g.city, a.id, a.geonameid from geoname g, alternatename a 
	 * 		where g.id > 0 and a.id > 0 and g.geonameid = a.geonameid
	 * </pre>
	 * 
	 * @param indexable
	 * @return
	 */
	public String buildQuery(final IndexableTable indexableTable, final long nextIdNumber, final long batchSize) {
		Table table = new Table(indexableTable.getName());
		SelectQuery selectQuery = new SelectQuery(table);
		buildQuery(selectQuery, null, table, indexableTable);
		IndexableColumn idIndexableColumn = getIdColumn(indexableTable.getChildren());
		selectQuery.addCriteria(new MatchCriteria(table, idIndexableColumn.getName(), MatchCriteria.GREATEREQUAL, nextIdNumber));
		selectQuery.addCriteria(new MatchCriteria(table, idIndexableColumn.getName(), MatchCriteria.LESS, nextIdNumber + batchSize));
		return UriUtilities.stripCarriageReturn(selectQuery.toString()).toLowerCase();
	}

	void buildQuery(final SelectQuery selectQuery, final Table parentTable, final Table table, final IndexableTable indexableTable) {
		IndexableTable currentIndexableTable = indexableTable;
		for (final Indexable<?> childIndexable : currentIndexableTable.getChildren()) {
			if (IndexableColumn.class.isAssignableFrom(childIndexable.getClass())) {
				IndexableColumn indexableColumn = (IndexableColumn) childIndexable;
				selectQuery.addColumn(table, indexableColumn.getName());
				if (indexableColumn.getForeignKey() != null) {
					// Get the parent table and make a join with this table
					// Note to self: This was a serious bug!
					// IndexableColumn primarkKeyColumn = getIdColumn(((IndexableTable) indexableTable.getParent()).getChildren());
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

	protected static String buildNextIdQuery(final IndexableTable indexableTable, final Long currentId) {
		StringBuilder stringBuilder = new StringBuilder();
		IndexableColumn idColumn = QueryBuilder.getIdColumn(indexableTable.getChildren());
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

	protected static String buildCountQuery(final IndexableTable indexableTable, final Long currentId, final Long nextId) {
		StringBuilder stringBuilder = new StringBuilder();
		IndexableColumn idColumn = QueryBuilder.getIdColumn(indexableTable.getChildren());
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

	/**
	 * Looks through the columns and returns the id column.
	 * 
	 * @param indexableColumns the columns to look through
	 * @return the id column or null if no such column is defined. Generally this will mean a configuration problem, every table must have a unique id column
	 */
	protected static IndexableColumn getIdColumn(final List<Indexable<?>> indexableColumns) {
		for (Indexable<?> indexable : indexableColumns) {
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

}