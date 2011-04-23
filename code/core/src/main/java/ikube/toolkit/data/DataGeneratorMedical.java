package ikube.toolkit.data;

import ikube.model.medical.Doctor;

import javax.persistence.EntityManager;

/**
 * This class generates data for the medical model.
 * 
 * @author Michael Couck
 * @since 23.04.2011
 * @version 01.00
 */
public class DataGeneratorMedical extends DataGeneratorFour {

	public DataGeneratorMedical(EntityManager entityManager, int iterations, Class<?>[] classes) {
		super(entityManager, iterations, classes);
	}
	
	public void generate() throws Exception {
		// We'll start with one instance
		Doctor doctor = createInstance(Doctor.class);
		
		// Read the doctors file and get an address
		
		entityManager.getTransaction().begin();
		entityManager.persist(doctor);
		entityManager.getTransaction().commit();
	}
	
}
