package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexableColumn extends Indexable<IndexableColumn> {

	private transient Object object;
	private transient int columnType;

	private String fieldName;
	private boolean idColumn;
	private IndexableColumn foreignKey;
	private boolean stored = Boolean.FALSE;
	private boolean analyzed = Boolean.TRUE;
	private boolean vectored = Boolean.TRUE;

	/** These values are only used for generating data. */
	private String columnClass;
	private int columnLength;

	/**
	 * This is the column where the name of the column is stored. In the case of a file in the database the name of the file can be used to
	 * get the correct parser for that type of content. This will typically be a sibling in the same table.
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

	public void setIdColumn(boolean idColumn) {
		this.idColumn = idColumn;
	}

	public IndexableColumn getForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(IndexableColumn foreignKey) {
		this.foreignKey = foreignKey;
	}

	public boolean isStored() {
		return stored;
	}

	public void setStored(final boolean stored) {
		this.stored = stored;
	}

	public boolean isAnalyzed() {
		return analyzed;
	}

	public void setAnalyzed(final boolean analyzed) {
		this.analyzed = analyzed;
	}

	public boolean isVectored() {
		return vectored;
	}

	public void setVectored(final boolean vectored) {
		this.vectored = vectored;
	}

	public IndexableColumn getNameColumn() {
		return nameColumn;
	}

	public void setNameColumn(IndexableColumn indexableColumn) {
		this.nameColumn = indexableColumn;
	}

	public String getColumnClass() {
		return columnClass;
	}

	public void setColumnClass(String columnClass) {
		this.columnClass = columnClass;
	}

	public int getColumnLength() {
		return columnLength;
	}

	public void setColumnLength(int columnLength) {
		this.columnLength = columnLength;
	}

	@Transient
	public Object getObject() {
		return object;
	}

	public void setObject(final Object object) {
		this.object = object;
	}

	@Transient
	public int getColumnType() {
		return columnType;
	}

	public void setColumnType(final int columnType) {
		this.columnType = columnType;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(isIdColumn());
		builder.append(", ");
		builder.append(getName());
		builder.append("]");
		return builder.toString();
	}

}
