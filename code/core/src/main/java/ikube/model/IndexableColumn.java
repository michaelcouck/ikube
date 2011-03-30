package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexableColumn extends Indexable<IndexableColumn> {

	@Transient
	private transient int columnType;

	@Field()
	private String fieldName;
	private boolean idColumn;
	private IndexableColumn foreignKey;

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

	public void setIdColumn(final boolean idColumn) {
		this.idColumn = idColumn;
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

	@Transient
	public int getColumnType() {
		return columnType;
	}

	public void setColumnType(final int columnType) {
		this.columnType = columnType;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
