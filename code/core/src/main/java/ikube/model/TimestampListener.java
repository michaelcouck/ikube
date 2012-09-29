package ikube.model;

import java.sql.Timestamp;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This listener will insert the timestamp when the entity gets persisted or updated.
 * 
 * @author Michael Couck
 * @since 29.09.12
 * @version 01.00
 */
public class TimestampListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimestampListener.class);

	@PrePersist
	@PreUpdate
	public void prePersist(final Persistable persistable) {
		LOGGER.info("Persistable : " + persistable);
		persistable.setTimestamp(new Timestamp(System.currentTimeMillis()));
	}

}
