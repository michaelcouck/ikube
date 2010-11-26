package ikube.model;

import ikube.index.visitor.IndexableVisitor;

import javax.persistence.Entity;
import javax.sql.DataSource;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexableTable extends Indexable<IndexableTable> {

	private String sql;
	private String predicate;
	private boolean primary;
	private DataSource dataSource;

	@Override
	public <V extends IndexableVisitor<Indexable<?>>> void accept(final V visitor) {
		visitor.visit(this);
	}

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

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
