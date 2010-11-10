package ikube.model;

import ikube.index.visitor.IndexableVisitor;

import javax.persistence.Entity;
import javax.sql.DataSource;

@Entity()
public class IndexableTable extends Indexable<IndexableTable> {

	private String sql;
	private DataSource dataSource;

	@Override
	public <V extends IndexableVisitor<Indexable<?>>> void accept(final V visitor) {
		visitor.visit(this);
	}

	public String getSql() {
		return sql;
	}

	public void setSql(final String predicate) {
		this.sql = predicate;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
