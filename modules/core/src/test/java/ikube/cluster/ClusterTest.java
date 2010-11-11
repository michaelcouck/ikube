package ikube.cluster;

import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.Lock;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class ClusterTest {

	public static long SLEEP = 360000000;

	protected void delete(IDataBase dataBase, Class<?>... klasses) {
		for (Class<?> klass : klasses) {
			List<?> objects = dataBase.find(klass, 0, Integer.MAX_VALUE);
			for (Object object : objects) {
				dataBase.remove(object);
			}
		}
	}

	@Test
	@Ignore
	public void start() throws Exception {
		init();
		Thread.sleep(SLEEP);
	}

	private void init() {
		// We get the production configuration file for the cluster test
		ApplicationContextManager.getApplicationContext(new String[] { "/cluster/spring.xml" });
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		delete(dataBase, Server.class, Lock.class);
	}

	public static void main(String[] args) throws Exception {
		ClusterTest clusterTest = new ClusterTest();
		clusterTest.start();
	}

}
