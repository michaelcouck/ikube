package ikube;

import ikube.cluster.ClusterTest;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DataLoader;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseTest extends ATest {

	static {
		try {
			// Delete the database file
			FileUtilities.deleteFiles(new File("."), new String[] { IConstants.DATABASE_FILE, ".transaction" });
			ApplicationContextManager.getApplicationContext(new String[] { "/spring.xml" });
			ClusterTest.SLEEP = 1000;
			DataLoader dataLoader = new DataLoader();
			File folder = new File(".");

			String[] stringPatterns = new String[] { "createTables.sql" };
			List<File> files = FileUtilities.findFilesRecursively(folder, stringPatterns, new ArrayList<File>());
			File file = files.get(0);
			dataLoader.createTables(file.getAbsolutePath());

			stringPatterns = new String[] { "data.xml" };
			files = FileUtilities.findFilesRecursively(folder, stringPatterns, new ArrayList<File>());
			file = files.get(0);
			dataLoader.insertDataSet(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected String ip;
	{
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
		}
	}
	protected IndexContext indexContext = ApplicationContextManager.getBean("faqIndexContext");

	protected static void delete(IDataBase dataBase, Class<?>... klasses) {
		for (Class<?> klass : klasses) {
			List<?> objects = dataBase.find(klass, 0, Integer.MAX_VALUE);
			for (Object object : objects) {
				dataBase.remove(object);
			}
		}
	}

}
