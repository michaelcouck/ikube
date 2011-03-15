package ikube.integration;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import ikube.IConstants;
import ikube.logging.Logging;
import ikube.model.faq.Attachment;
import ikube.model.faq.Faq;
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
			// return;
		}
		ApplicationContextManager.getApplicationContext();
		generateData();
		Thread.sleep(1000 * 60 * 60 * 3);
	}

	private void generateData() throws Exception {
		EntityManager entityManager = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_NAME).createEntityManager();
		IDataGenerator dataGenerator = new DataGeneratorFour(entityManager, 100, Faq.class, Attachment.class);
		dataGenerator.before();
		dataGenerator.generate();
		dataGenerator.after();
	}

}