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

	/** TODO - the datasource needs to be configured else where. */
	@Transient
	private transient DataSource dataSource;

	private String schema;
	private String predicate;
	private boolean primary;

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
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

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(getName());
		builder.append(", ");
		builder.append(getSchema());
		builder.append(", ");
		builder.append(getPredicate());
		builder.append("]");
		return builder.toString();
	}

}
