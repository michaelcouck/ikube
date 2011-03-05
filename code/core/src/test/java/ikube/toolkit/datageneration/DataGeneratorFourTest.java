package ikube.toolkit.datageneration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.model.medical.Address;
import ikube.model.medical.Condition;
import ikube.model.medical.Medication;
import ikube.model.medical.Patient;
import ikube.toolkit.ApplicationContextManager;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataGeneratorFourTest extends BaseTest {

	private DataGeneratorFour dataGeneratorFour;
	private Class<?>[] classes = new Class[] { Patient.class, Address.class, Condition.class, Medication.class };
	private String selectFromPatients = "select e from Patient as e";
	private String selectFromAddresses = "select e from Address as e";

	@Before
	public void before() throws Exception {
		dataGeneratorFour = new DataGeneratorFour(ENTITY_MANAGER, 1, classes);
		dataGeneratorFour.before();
		dataGeneratorFour.delete(ENTITY_MANAGER, classes);
	}

	@After
	public void after() throws Exception {
		dataGeneratorFour.after();
	}

	@Test
	public void generateFieldData() {
		Address address = dataGeneratorFour.generateFieldData(Address.class, new Address());
		assertNotNull(address.getCountry());
		assertNotNull(address.getNumber());
		assertNotNull(address.getPostCode());
		assertNotNull(address.getPostCode());
		assertNotNull(address.getProvince());
		assertNotNull(address.getStreet());
		Patient patient = dataGeneratorFour.generateFieldData(Patient.class, new Patient());
		assertNull(patient.getAddress());
		assertNotNull(patient.getBirthDate());
		assertNotNull(patient.getDeathDate());
		assertNotNull(patient.getFirstName());
		assertNotNull(patient.getLastName());
		assertNull(patient.getRecords());
	}

	@Test
	public void setTargets() {
		List<Patient> targets = Arrays.asList(new Patient());
		List<Address> results = Arrays.asList(new Address());
		dataGeneratorFour.setTargets(Patient.class, Address.class, targets, results);
		for (Patient patient : targets) {
			assertNotNull(patient.getAddress());
		}
		List<Condition> conditions = Arrays.asList(new Condition());
		List<Medication> medications = Arrays.asList(new Medication());
		dataGeneratorFour.setTargets(Condition.class, Medication.class, conditions, medications);
		dataGeneratorFour.setTargets(Medication.class, Condition.class, medications, conditions);
		for (Condition condition : conditions) {
			assertNotNull(condition.getMedications());
			assertTrue(condition.getMedications().size() > 0);
		}
		for (Medication medication : medications) {
			assertNotNull(medication.getConditions());
			assertTrue(medication.getConditions().size() > 0);
		}
	}

	@Test
	public void persistDelete() throws Exception {
		dataGeneratorFour.persist(ENTITY_MANAGER);
		// Verify that there are two entities in the database
		assertEquals(1, ENTITY_MANAGER.createQuery(selectFromPatients).getResultList().size());
		assertEquals(1, ENTITY_MANAGER.createQuery(selectFromAddresses).getResultList().size());
		dataGeneratorFour.delete(ENTITY_MANAGER, Patient.class, Address.class);
		assertEquals(0, ENTITY_MANAGER.createQuery(selectFromPatients).getResultList().size());
		assertEquals(0, ENTITY_MANAGER.createQuery(selectFromAddresses).getResultList().size());
	}

	@Test
	public void references() throws Exception {
		dataGeneratorFour.persist(ENTITY_MANAGER);
		dataGeneratorFour.references(ENTITY_MANAGER);
		List<Patient> patients = ENTITY_MANAGER.createQuery(selectFromPatients, Patient.class).getResultList();
		for (Patient patient : patients) {
			assertNotNull(patient.getAddress());
		}
	}

	@Test
	public void generate() throws Exception {
		ApplicationContextManager.getApplicationContext();
		dataGeneratorFour.before();
		dataGeneratorFour.generate();
		dataGeneratorFour.after();
	}

}