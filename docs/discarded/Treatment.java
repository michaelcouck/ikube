package ikube.model.medical;

import ikube.model.Persistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * This class represents a a service that was given to the patient like a consultation or a session at the radiologist.
 * 
 * @author Michael Couck
 * @since 05.03.2011
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Treatment extends Persistable {

	@Column(length = 64)
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
