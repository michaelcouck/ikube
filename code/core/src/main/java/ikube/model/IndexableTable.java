package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.sql.DataSource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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

	private String predicate;
	private boolean primary;

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(final boolean primary) {
		this.primary = primary;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(final String predicate) {
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
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
