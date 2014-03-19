package ikube.model;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.sql.Timestamp;

/**
 * This listener will insert the timestamp when the entity gets persisted or updated.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-09-2012
 */
public class TimestampListener {

    @PrePersist
    public void prePersist(final Persistable persistable) {
        persistable.setTimestamp(new Timestamp(System.currentTimeMillis()));
    }

    @PreUpdate
    public void preUpdate(final Persistable persistable) {
        persistable.setTimestamp(new Timestamp(System.currentTimeMillis()));
    }

}
