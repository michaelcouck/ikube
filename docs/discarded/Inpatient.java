package ikube.model.medical;

import ikube.model.Persistable;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * This class represents a day at the hospital.
 * 
 * @author Michael Couck
 * @since 05.03.2011
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Inpatient extends Persistable {

	private Date dt;

	public Date getDt() {
		return dt;
	}

	public void setDt(Date date) {
		this.dt = date;
	}

}
