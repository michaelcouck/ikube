package ikube.model;

import java.sql.Timestamp;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * This listener will insert the timestamp when the entity gets persisted or updated.
 * 
 * @author Michael Couck
 * @since 29.09.12
 * @version 01.00
 */
public class TimestampListener {

	@PrePersist
	@PreUpdate
	public void prePersist(final Persistable persistable) {
		persistable.setTimestamp(new Timestamp(System.currentTimeMillis()));
	}

}
