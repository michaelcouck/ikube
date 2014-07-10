package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class SavePoint extends Persistable {

    @Column
    private String indexable;
    @Column
    private String indexContext;
    @Column
    private Long identifier;

    public String getIndexable() {
        return indexable;
    }

    public void setIndexable(String indexable) {
        this.indexable = indexable;
    }

    public String getIndexContext() {
        return indexContext;
    }

    public void setIndexContext(String indexContext) {
        this.indexContext = indexContext;
    }

    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }
}
