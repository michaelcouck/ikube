package ikube.model;

import javax.persistence.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-07-2014
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ApiMethod extends Persistable {

    @Column
    private String type;
    @Column
    private String uri;
    @Column
    private String description;
    @Transient
    private Object consumes;
    @Transient
    private Object produces;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getConsumes() {
        return consumes;
    }

    public void setConsumes(Object consumes) {
        this.consumes = consumes;
    }

    public Object getProduces() {
        return produces;
    }

    public void setProduces(Object produces) {
        this.produces = produces;
    }

}