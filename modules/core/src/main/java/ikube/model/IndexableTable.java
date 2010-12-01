package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.sql.DataSource;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexableTable extends Indexable<IndexableTable> {

	private transient DataSource dataSource;

	private String sql;
	private String predicate;
	private boolean primary;

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(final String sql) {
		this.sql = sql;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	@Transient
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
