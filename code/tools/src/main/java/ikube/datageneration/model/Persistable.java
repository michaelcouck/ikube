package ikube.datageneration.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public abstract class Persistable implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private long id;

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

}
