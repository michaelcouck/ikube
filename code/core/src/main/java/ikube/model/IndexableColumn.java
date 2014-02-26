package ikube.model;

import javax.persistence.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
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
    @Attribute(description = "Whether the data from the column is hashed before adding to the document. " +
            "Typically this is when the data should be numeric, or at least searchable numerically but is " +
            "actually a string or alphanumeric.")
    private boolean hashed;
    @Column
    @PrimaryKeyJoinColumn
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private IndexableColumn foreignKey;

    /**
     * This is the column where the name of the column is stored. In the case of a file in the database the
     * name of the file can be used to get the correct parser for that type of content. This will typically be a
     * sibling in the same table.
     */
    @Column
    @PrimaryKeyJoinColumn
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private IndexableColumn nameColumn;

    public int getColumnType() {
        return columnType;
    }

    public void setColumnType(int columnType) {
        this.columnType = columnType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isIdColumn() {
        return idColumn;
    }

    public void setIdColumn(boolean idColumn) {
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

    public boolean isHashed() {
        return hashed;
    }

    public void setHashed(boolean hash) {
        this.hashed = hash;
    }

    public IndexableColumn getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(IndexableColumn foreignKey) {
        this.foreignKey = foreignKey;
    }

    public IndexableColumn getNameColumn() {
        return nameColumn;
    }

    public void setNameColumn(IndexableColumn nameColumn) {
        this.nameColumn = nameColumn;
    }

}
