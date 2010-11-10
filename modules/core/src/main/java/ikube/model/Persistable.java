package ikube.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity()
public abstract class Persistable implements Serializable {

	@Id
	private long id;

	@GeneratedValue(strategy = GenerationType.TABLE)
	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

}
