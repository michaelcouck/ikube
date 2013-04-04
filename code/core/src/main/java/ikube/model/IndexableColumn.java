package ikube.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableColumn extends Indexable<IndexableColumn> {

	@Transient
	private transient int columnType;

	@Column
	@Attribute(description = "This is the name of the field in the Lucene index")
	private String fieldName;
	@Column
	private boolean idColumn;
	@Column
	private boolean filePath;
	@Column
	private boolean numeric;
	@Column
	@PrimaryKeyJoinColumn
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private IndexableColumn foreignKey;

	/**
	 * This is the column where the name of the column is stored. In the case of a file in the database the name of the file can be used to get the correct
	 * parser for that type of content. This will typically be a sibling in the same table.
	 */
	private IndexableColumn nameColumn;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(final String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean isIdColumn() {
		return idColumn;
	}

	public void setIdColumn(final boolean idColumn) {
		this.idColumn = idColumn;
	}

	public boolean isFilePath() {
		return filePath;
	}

	public void setFilePath(boolean filePath) {
		this.filePath = filePath;
	}

	public boolean isNumeric() {
		return numeric;
	}

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	public IndexableColumn getForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(final IndexableColumn foreignKey) {
		this.foreignKey = foreignKey;
	}

	public IndexableColumn getNameColumn() {
		return nameColumn;
	}

	public void setNameColumn(final IndexableColumn indexableColumn) {
		this.nameColumn = indexableColumn;
	}

	public int getColumnType() {
		return columnType;
	}

	public void setColumnType(final int columnType) {
		this.columnType = columnType;
	}

}
