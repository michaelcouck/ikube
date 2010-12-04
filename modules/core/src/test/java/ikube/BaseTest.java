package ikube;

import ikube.cluster.ClusterTest;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DataGenerator;
import ikube.toolkit.DataLoader;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Map;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class BaseTest extends ATest {

	private static String SPRING_CONFIGURATION_FILE = "/spring.xml";

	static {
		try {
			ClusterTest.SLEEP = 1000;

			// Delete the database file
			FileUtilities.deleteFiles(new File("."), IConstants.DATABASE_FILE, ".transaction", ".odb");
			ApplicationContextManager.getApplicationContext(SPRING_CONFIGURATION_FILE);
			// Delete all the old index directories
			Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
			for (IndexContext indexContext : contexts.values()) {
				File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
				FileUtilities.deleteFile(baseIndexDirectory, 1);
			}

			DataLoader dataLoader = new DataLoader();
			File sqlFile = FileUtilities.findFile(new File("."), new String[] { "tables.sql" });
			dataLoader.createTables(sqlFile.getAbsolutePath());

			DataGenerator dataGenerator = new DataGenerator();
			dataGenerator.before();
			dataGenerator.generate();
			dataGenerator.after();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected IndexContext indexContext = ApplicationContextManager.getBean(IndexContext.class);

}