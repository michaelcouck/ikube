package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.sql.DataSource;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class IndexableTable extends Indexable<IndexableTable> {

	@Transient
	private transient long minimumId = -1;
	@Transient
	private transient long maximumId = -1;
	/** TODO - the datasource needs to be configured else where. */
	@Transient
	private transient DataSource dataSource;

	private String predicate;
	private boolean primaryTable;
	private int maxExceptions = 10;
	private boolean allColumns = Boolean.FALSE;

	public boolean isAllColumns() {
		return allColumns;
	}

	public void setAllColumns(boolean allColumns) {
		this.allColumns = allColumns;
	}

	public int getMaxExceptions() {
		return maxExceptions;
	}

	public void setMaxExceptions(int maxExceptions) {
		this.maxExceptions = maxExceptions;
	}

	public boolean isPrimaryTable() {
		return primaryTable;
	}

	public void setPrimaryTable(final boolean primary) {
		this.primaryTable = primary;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(final String predicate) {
		this.predicate = predicate;
	}

	public long getMinimumId() {
		return minimumId;
	}

	public void setMinimumId(long minimumId) {
		this.minimumId = minimumId;
	}

	public long getMaximumId() {
		return maximumId;
	}

	public void setMaximumId(long maximumId) {
		this.maximumId = maximumId;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
