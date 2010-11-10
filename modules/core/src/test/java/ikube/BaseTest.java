package ikube;

import ikube.cluster.ClusterTest;
import ikube.database.IDataBase;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DataLoader;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class BaseTest {

	static {
		Logging.configure();
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
			dataLoader.setData(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Logger logger = Logger.getLogger(this.getClass());
	protected IndexContext indexContext = ApplicationContextManager.getBean("faqIndexContext");

	protected static void delete(IDataBase dataBase, Class<?>... klasses) {
		for (Class<?> klass : klasses) {
			List<?> objects = dataBase.find(klass, 0, Integer.MAX_VALUE);
			for (Object object : objects) {
				dataBase.remove(object);
			}
		}
	}

	/**
	 * Returns the max read length byte array plus 1000, i.e. more than the max bytes that the application can read. This forces the indexer
	 * to get a reader rather than a string.
	 *
	 * @param string
	 *            the string to copy to the byte array until the max read length is exceeded
	 * @return the byte array of the string copied several times more than the max read meength
	 */
	protected byte[] getBytes(String string) {
		byte[] bytes = new byte[(int) (IConstants.MAX_READ_LENGTH + 1000)];
		for (int offset = 0; offset < bytes.length;) {
			byte[] segment = string.getBytes();
			if (offset + segment.length >= bytes.length) {
				break;
			}
			System.arraycopy(segment, 0, bytes, offset, segment.length);
			offset += segment.length;
		}
		return bytes;
	}

}
