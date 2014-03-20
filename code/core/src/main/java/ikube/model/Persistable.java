package ikube.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Base class for entities. All sub classes must declare the inheritance strategy.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@Entity
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EntityListeners(value = {TimestampListener.class})
public abstract class Persistable implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "persistable")
    @SequenceGenerator(name = "persistable", sequenceName = "persistable", allocationSize = 1000)
    @Attribute(field = false, description = "This is the identifier field in the entity and will be set by the database")
    private long id;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}