package ikube.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;

/**
 * Base class for entities. All sub classes must declare the inheritance strategy.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Persistable implements Serializable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "persistable")
	@SequenceGenerator(name = "persistable", sequenceName = "persistable", allocationSize = 1000)
	@Attribute(field = false, description = "This is the identifier field in the entity and will be set by the database")
	private long id;

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

}