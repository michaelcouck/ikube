package ikube;

import ikube.cluster.ClusterIntegration;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DataGeneratorTwo;
import ikube.toolkit.DataLoader;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

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
		ClusterIntegration.SLEEP = 1000;

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

		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				DataGeneratorTwo dataGenerator = new DataGeneratorTwo(100, 1);
				dataGenerator.generate(SPRING_CONFIGURATION_FILE);
			}
		}, "Data generator two insertion : ", 1);
	}

	protected IndexContext indexContext = ApplicationContextManager.getBean("indexContextOne");

	/**
	 * Returns the path to the latest index directory for this server and this context. The result will be something like
	 * './index/faq/1234567890/127.0.0.1'.
	 * 
	 * @param indexContext
	 *            the index context to get the directory path for
	 * @return the directory path to the latest index directory for this servers and context
	 */
	protected String getServerIndexDirectoryPath(IndexContext indexContext) {
		StringBuilder builder = new StringBuilder();
		builder.append(indexContext.getIndexDirectoryPath());
		builder.append(File.separator);
		builder.append(indexContext.getIndexName());
		builder.append(File.separator);
		builder.append(System.currentTimeMillis());
		builder.append(File.separator);
		builder.append(IP);
		return builder.toString();
	}

}