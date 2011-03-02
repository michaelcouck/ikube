package ikube;

import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import org.junit.Before;

/**
 * This is a test for the 'production' configuration, suitable for a single instance, i.e. no cluster functionality is tested.
 * 
 * @author Michael Couck
 * @since 20.12.10
 * @version 01.00
 */
public class Integration {

	private static String INTEGRATION_SPRING_CONFIG = IConstants.META_INF + IConstants.SEP + "integration" + IConstants.SEP
			+ IConstants.SPRING_XML;

	@Before
	public void before() {
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
	}

	public static void main(String[] args) throws Exception {
		// Class.forName(DB2Driver.class.getName());
		Connection connection = DriverManager.getConnection("jdbc:db2://localhost:50000/ikube", "db2admin", "db2admin");
		System.out.println("Connection : " + connection);

		ApplicationContextManager.getApplicationContext(INTEGRATION_SPRING_CONFIG);
		Integration integration = new Integration();
		integration.before();
		Thread.sleep(1000 * 60 * 60 * 10);
	}

}
