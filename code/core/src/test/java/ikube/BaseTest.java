package ikube;

import ikube.cluster.ClusterIntegration;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DataLoader;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.datageneration.DataGeneratorTwo;
import ikube.toolkit.datageneration.IDataGenerator;

import java.io.File;
import java.util.Map;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class BaseTest extends ATest {

	static {
		ClassLoader.getSystemClassLoader();

		ClusterIntegration.SLEEP = 1000;

		// Delete the database file
		FileUtilities.deleteFiles(new File("."), IConstants.DATABASE_FILE, IConstants.TRANSACTION_FILES, IConstants.DATABASE_FILE);
		ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}

		DataLoader dataLoader = new DataLoader();
		File sqlFile = FileUtilities.findFile(new File("."), new String[] { "tables.sql" });
		dataLoader.createTables(sqlFile.getAbsolutePath());

		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				IDataGenerator dataGenerator = new DataGeneratorTwo(100, 1);
				dataGenerator.before();
				dataGenerator.generate();
				dataGenerator.after();
			}
		}, "Data generator two insertion : ", 1);
	}

	protected IndexContext indexContext = ApplicationContextManager.getBean("indexContext");

}