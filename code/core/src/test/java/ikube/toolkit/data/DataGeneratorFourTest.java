package ikube.toolkit.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.database.IDataBase;
import ikube.model.faq.Attachment;
import ikube.model.faq.Faq;
import ikube.model.medical.Address;
import ikube.model.medical.Hospital;
import ikube.model.medical.Patient;

import java.util.ArrayList;
import java.util.Collection;

import mockit.Cascading;
import mockit.Mockit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for the data generator.
 * 
 * @author Michael Couck
 * @since 14.03.2011
 * @version 01.00
 */
@Ignore
public class DataGeneratorFourTest {

	@Cascading
	private IDataBase dataBase;
	private DataGeneratorFour dataGeneratorFour;
	private Class<?>[] classes = new Class[] { Patient.class, Address.class, Hospital.class };

	@BeforeClass
	public static void beforeClass() throws Exception {
		Mockit.setUpMocks();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		Mockit.tearDownMocks();
	}

	@Before
	public void before() throws Exception {
		dataGeneratorFour = new DataGeneratorFour(dataBase, 1, classes);
		dataGeneratorFour.before();
	}

	@After
	public void after() throws Exception {
		dataGeneratorFour.after();
	}

	@Test
	public void createInstance() throws Exception {
		Address address = dataGeneratorFour.createInstance(Address.class);
		assertNotNull(address);
		assertNotNull(address.getCountry());
		assertNotNull(address.getNumb());
		assertNotNull(address.getPostCode());
		assertNotNull(address.getProvince());
		assertNotNull(address.getStreet());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void createCollection() throws Exception {
		ArrayList<Address> addresses = dataGeneratorFour.createCollection(ArrayList.class, Address.class);
		assertNotNull(addresses);
		assertTrue(addresses.size() > 0);
		for (Address address : addresses) {
			assertNotNull(address);
			assertNotNull(address.getCountry());
			assertNotNull(address.getNumb());
			assertNotNull(address.getPostCode());
			assertNotNull(address.getProvince());
			assertNotNull(address.getStreet());
		}
	}

	@Test
	public void createFields() throws Exception {
		Patient patient = dataGeneratorFour.createFields(Patient.class, new Patient());
		assertNotNull(patient.getBirthDate());
		assertNotNull(patient.getDeathDate());
		assertNotNull(patient.getFirstName());
		assertNotNull(patient.getLastName());
		assertNotNull(patient.getRecords());

		Address address = patient.getAddress();
		assertNotNull(address);
		assertNotNull(address.getCountry());
		assertNotNull(address.getNumb());
		assertNotNull(address.getPostCode());
		assertNotNull(address.getPostCode());
		assertNotNull(address.getProvince());
		assertNotNull(address.getStreet());

		Faq faq = dataGeneratorFour.createInstance(Faq.class);
		assertNotNull(faq);
		assertNotNull(faq.getAnswer());
		assertNotNull(faq.getCreationTimestamp());
		assertNotNull(faq.getCreator());
		assertNotNull(faq.getModifiedTimestamp());
		assertNotNull(faq.getModifier());
		assertNotNull(faq.getPublished());
		assertNotNull(faq.getQuestion());
		Collection<Attachment> attachments = faq.getAttachments();
		assertTrue(attachments.size() > 0);
		for (Attachment attachment : attachments) {
			assertNotNull(attachment.getAttachment());
			assertNotNull(attachment.getFaq());
			assertNotNull(attachment.getLength());
			assertNotNull(attachment.getName());
			assertEquals(faq, attachment.getFaq());
		}
	}

	@Test
	public void generate() throws Exception {
		dataGeneratorFour.before();
		dataGeneratorFour.generate();
		dataGeneratorFour.after();
	}

}