package ikube.integration;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import ikube.IConstants;
import ikube.logging.Logging;
import ikube.model.faq.Attachment;
import ikube.model.faq.Faq;
import ikube.model.medical.Address;
import ikube.model.medical.Administration;
import ikube.model.medical.Condition;
import ikube.model.medical.Doctor;
import ikube.model.medical.Hospital;
import ikube.model.medical.Inpatient;
import ikube.model.medical.Medication;
import ikube.model.medical.Patient;
import ikube.model.medical.Person;
import ikube.model.medical.Record;
import ikube.model.medical.Treatment;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.datageneration.DataGeneratorFour;
import ikube.toolkit.datageneration.IDataGenerator;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * This is a test for the 'production' configuration, suitable for a multiple instances.
 * 
 * @author Michael Couck
 * @since 20.12.10
 * @version 01.00
 */
public class Integration {

	static {
		Logging.configure();
	}

	private Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void main() throws Exception {
		String osName = System.getProperty("os.name");
		logger.info("Operating system : " + osName);
		if (!osName.toLowerCase().contains("server")) {
			return;
		}
		ApplicationContextManager.getApplicationContext();
		generateData();
		Thread.sleep(1000 * 60 * 60 * 3);
		// TODO When the test ends then verify the data and the index
		// across the servers
	}

	private void generateData() throws Exception {
		EntityManager entityManager = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_NAME).createEntityManager();
		Class<?>[] classes = { Faq.class, Attachment.class, Hospital.class, Administration.class, Person.class, Doctor.class,
				Patient.class, Inpatient.class, Condition.class, Medication.class, Treatment.class, Record.class, Address.class };
		IDataGenerator dataGenerator = new DataGeneratorFour(entityManager, 10, classes);
		dataGenerator.before();
		dataGenerator.generate();
		dataGenerator.after();
	}

}