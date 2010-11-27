package ikube;

import ikube.cluster.ClusterTest;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DataLoader;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class BaseTest extends ATest {

	static {
		try {
			ClusterTest.SLEEP = 1000;

			// Delete the database file
			FileUtilities.deleteFiles(new File("."), new String[] { IConstants.DATABASE_FILE, ".transaction", "serenity.odb" });
			ApplicationContextManager.getApplicationContext(new String[] { "/spring.xml" });
			// Delete all the old index directories
			Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
			for (IndexContext indexContext : contexts.values()) {
				File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
				FileUtilities.deleteFile(baseIndexDirectory, 1);
			}

			DataLoader dataLoader = new DataLoader();
			File folder = new File(".");

			File file = FileUtilities.findFile(folder, new String[] { "tables.sql" });
			dataLoader.createTables(file.getAbsolutePath());

			file = FileUtilities.findFile(folder, new String[] { "data.xml" });
			dataLoader.insertDataSet(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected IndexContext indexContext = ApplicationContextManager.getBean(IndexContext.class);

	protected static void delete(IDataBase dataBase, Class<?>... klasses) {
		for (Class<?> klass : klasses) {
			List<?> objects = dataBase.find(klass, 0, Integer.MAX_VALUE);
			for (Object object : objects) {
				dataBase.remove(object);
			}
		}
	}

}