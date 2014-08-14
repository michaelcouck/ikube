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
    private String uri;
    @Column
    private String method;
    @Column
    private String description;

    @Transient
    private Object consumes;
    @Transient
    private Object produces;

    @Column
    private String consumesType;
    @Column
    private String producesType;


    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Object getConsumes() {
        return consumes;
    }

    public void setConsumes(final Object consumes) {
        this.consumes = consumes;
    }

    public Object getProduces() {
        return produces;
    }

    public void setProduces(final Object produces) {
        this.produces = produces;
    }

    public String getConsumesType() {
        return consumesType;
    }

    public void setConsumesType(final String consumesType) {
        this.consumesType = consumesType;
    }

    public String getProducesType() {
        return producesType;
    }

    public void setProducesType(final String producesType) {
        this.producesType = producesType;
    }
}