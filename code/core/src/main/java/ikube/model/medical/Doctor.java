package ikube.model.medical;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * This class represents a doctor.
 * 
 * @author Michael Couck
 * @since 05.03.2011
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQuery(name = Doctor.FIND_DOCTORS, query = Doctor.FIND_DOCTORS)
public class Doctor extends Person {
	
	public static final String FIND_DOCTORS = "select d from Doctor d";

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = Patient.class)
	private Collection<Patient> patients;

	public Collection<Patient> getPatients() {
		return patients;
	}

	public void setPatients(Collection<Patient> patients) {
		this.patients = patients;
	}

}
