package ikube.action.index.handler.database;

import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select ");
		addColumns(stringBuilder, true, indexableTable);
		stringBuilder.append(" from ");
		addTables(stringBuilder, true, indexableTable);
		stringBuilder.append(" where ");
		boolean first = addPredicates(stringBuilder, true, indexableTable);
		first = addPredicate(stringBuilder, first, indexableTable);
		addBatchPredicate(stringBuilder, first, indexableTable, nextIdNumber, batchSize);
		return stringBuilder.toString();
	}

	private void addBatchPredicate(final StringBuilder stringBuilder, boolean first, final IndexableTable indexableTable, final long nextIdNumber,
			final long batchSize) {
		String idColumnName = getIdColumn(indexableTable.getChildren()).getName();
		
		if (!first) {
			stringBuilder.append(" and ");
		}
		
		stringBuilder.append(indexableTable.getName());
		stringBuilder.append('.');
		stringBuilder.append(idColumnName);

		stringBuilder.append(" >= ");
		stringBuilder.append(nextIdNumber);
		stringBuilder.append(" and ");

		stringBuilder.append(indexableTable.getName());
		stringBuilder.append('.');
		stringBuilder.append(idColumnName);

		stringBuilder.append(" < ");
		stringBuilder.append(nextIdNumber + batchSize);
	}

	private boolean addPredicates(final StringBuilder stringBuilder, boolean first, final Indexable<?> indexable) {
		if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
			IndexableTable indexableTable = (IndexableTable) indexable;
			if (!StringUtils.isEmpty(indexableTable.getPredicate())) {
				if (!first) {
					stringBuilder.append(" and ");
				}
				stringBuilder.append(indexableTable.getPredicate());
				first = Boolean.FALSE;
			}
		}
		if (indexable.getChildren() != null) {
			for (final Indexable<?> child : indexable.getChildren()) {
				addPredicates(stringBuilder, first, child);
			}
		}
		return first;
	}

	private boolean addPredicate(final StringBuilder stringBuilder, boolean first, final Indexable<?> indexable) {
		if (indexable.getChildren() != null) {
			for (final Indexable<?> child : indexable.getChildren()) {
				if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
					IndexableColumn indexableColumn = (IndexableColumn) child;
					IndexableColumn foreignKeyColumn = indexableColumn.getForeignKey();
					if (foreignKeyColumn == null) {
						continue;
					}
					if (!first) {
						stringBuilder.append(" and ");
					}
					IndexableTable indexableTable = (IndexableTable) indexableColumn.getParent();
					IndexableTable foreignKeyTable = (IndexableTable) foreignKeyColumn.getParent();
					String indexableTableIdentifier = indexableTable.getName();
					String foreignKeyTableIdentifier = foreignKeyTable.getName();
					stringBuilder.append(foreignKeyTableIdentifier);
					stringBuilder.append(".");
					stringBuilder.append(foreignKeyColumn.getName());
					stringBuilder.append(" = ");
					stringBuilder.append(indexableTableIdentifier);
					stringBuilder.append(".");
					stringBuilder.append(indexableColumn.getName());
					first = Boolean.FALSE;
				}
				addPredicate(stringBuilder, first, child);
			}
		}
		return first;
	}

	private boolean addColumns(final StringBuilder stringBuilder, boolean first, final Indexable<?> indexable) {
		if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
			IndexableColumn indexableColumn = (IndexableColumn) indexable;
			IndexableTable indexableTable = (IndexableTable) indexable.getParent();
			String identifier = indexableTable.getName();
			if (!first) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(identifier);
			stringBuilder.append(".");
			stringBuilder.append(indexableColumn.getName());
			first = Boolean.FALSE;
		}
		if (indexable.getChildren() != null) {
			for (final Indexable<?> child : indexable.getChildren()) {
				first = addColumns(stringBuilder, first, child);
			}
		}
		return first;
	}

	private void addTables(final StringBuilder stringBuilder, boolean first, final Indexable<?> indexable) {
		if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
			if (!first) {
				stringBuilder.append(", ");
			}
			IndexableTable indexableTable = (IndexableTable) indexable;
			String tableName = indexableTable.getName();
			stringBuilder.append(tableName);
			stringBuilder.append(" ");
			stringBuilder.append(tableName);
			first = Boolean.FALSE;
		}
		if (indexable.getChildren() != null) {
			for (final Indexable<?> child : indexable.getChildren()) {
				addTables(stringBuilder, first, child);
			}
		}
	}

	/**
	 * Looks through the columns and returns the id column.
	 * 
	 * @param indexableColumns the columns to look through
	 * @return the id column or null if no such column is defined. Generally this will mean a configuration problem, every table must have a
	 *         unique id column
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
