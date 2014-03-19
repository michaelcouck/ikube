package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.sql.DataSource;

/**
 * This is a holder object for a datasource. The user can add one of these and the handler will dynamically add all the tables in the
 * database to the tables in the context for the index.
 * 
 * @author Michael Couck
 * @since 03.01.2012
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableDataSource extends Indexable {

	/** TODO - the datasource needs to be configured else where. */
	@Transient
	private transient DataSource dataSource;

	@Column
	@Attribute(field = false, description = "This flag is whether to index all the columns in the database, default is true")
	private boolean allColumns = Boolean.TRUE;
	@Column
	@Attribute(field = false, description = "This is a delimiter seperated list of patterns that will exclude tables from being indexed")
	private String excludedTablePatterns;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean isAllColumns() {
		return allColumns;
	}

	public void setAllColumns(final boolean allColumns) {
		this.allColumns = allColumns;
	}

	public String getExcludedTablePatterns() {
		return excludedTablePatterns;
	}

	public void setExcludedTablePatterns(final String excludedTablePatterns) {
		this.excludedTablePatterns = excludedTablePatterns;
	}

}