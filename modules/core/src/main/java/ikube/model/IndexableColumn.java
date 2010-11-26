package ikube.model;

import ikube.index.visitor.IndexableVisitor;

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

	/**
	 * This is the column where the name of the column is stored. In the case of a file in the database the name of the file can be used to
	 * get the correct parser for that type of content. This will typically be a sibling in the same table.
	 */
	private IndexableColumn indexableColumn;

	@Override
	public <V extends IndexableVisitor<Indexable<?>>> void accept(final V visitor) {
		visitor.visit(this);
	}

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

	public IndexableColumn getIndexableColumn() {
		return indexableColumn;
	}

	public void setIndexableColumn(IndexableColumn indexableColumn) {
		this.indexableColumn = indexableColumn;
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

}
