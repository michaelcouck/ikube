package ikube.integration.toolkit;

import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

public class DatabaseUtilitiesIntegration {

	@Test
	@Ignore
	public void executeStatement() {
		File springH2JdbcFile = FileUtilities.findFileRecursively(new File("."), "spring-h2-jdbc.xml");
		ApplicationContextManager.getApplicationContext(springH2JdbcFile);
	}

}
