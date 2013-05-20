package ikube.model;

import javax.persistence.Column;
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
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableTable extends Indexable<IndexableTable> {

	@Transient
	private transient long minimumId = -1;
	@Transient
	private transient long maximumId = -1;
	/** TODO - the datasource needs to be configured else where. */
	@Transient
	private transient DataSource dataSource;

	@Column
	@Attribute(field = false, description = "This is a sql predicate, like 'where id > 1000'")
	private String predicate;
	@Column
	@Attribute(field = false, description = "This flag is whether to index all the columns in the database, default is true")
	private boolean allColumns = Boolean.TRUE;

	public boolean isAllColumns() {
		return allColumns;
	}

	public void setAllColumns(boolean allColumns) {
		this.allColumns = allColumns;
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
