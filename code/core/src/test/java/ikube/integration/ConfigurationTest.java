package ikube.integration;

import static org.junit.Assert.*;

import ikube.toolkit.ApplicationContextManager;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * @author Michael Couck
 * @since 12.03.2011
 * @version 01.00
 */
@Ignore
public class ConfigurationTest {

	// @Test
	public void configurePersistence() {
		String persistence = "/META-INF/persistence/spring-persistence.xml";
		String db2Jdbc = "/META-INF/configuration/db2/spring-jdbc.xml";
		String oracleJdbc = "/META-INF/configuration/oracle/spring-jdbc.xml";
		String[] configLocations = { persistence, db2Jdbc, oracleJdbc };
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext(configLocations);
		assertNotNull(applicationContext);
	}

	@Test
	public void configureClient() {
		String spring = "/META-INF/spring.xml";
		String springClient = "/META-INF/configuration/spring-client.xml";
		String[] configLocations = { spring, springClient };
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext(configLocations);
		assertNotNull(applicationContext);
	}

	// @Test
	public void configureClientAndPersistence() {
		String spring = "/META-INF/spring.xml";
		String springClient = "/META-INF/configuration/spring-client.xml";
		String persistence = "/META-INF/persistence/spring-persistence.xml";
		String db2Jdbc = "/META-INF/configuration/db2/spring-jdbc.xml";
		String oracleJdbc = "/META-INF/configuration/oracle/spring-jdbc.xml";
		String[] configLocations = { spring, springClient, persistence, db2Jdbc, oracleJdbc };
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext(configLocations);
		assertNotNull(applicationContext);
	}

}
