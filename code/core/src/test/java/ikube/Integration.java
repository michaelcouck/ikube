package ikube;

import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.net.URL;
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
		URL url = new URL(//
				"http://ikube.ikube.cloudbees.net/SearchServlet?" + //
						"indexName=indexOne&" + //
						"searchStrings=Michael&" + //
						"searchFields=content&" + //
						"sortFields=content&" + //
						"fragment=true&" + //
						"firstResult=0&" + //
						"maxResults=100");
		String content = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
		System.out.println(content);
		// ApplicationContextManager.getApplicationContext();
		// Integration integration = new Integration();
		// integration.before();
		// Thread.sleep(1000 * 60 * 60 * 10);
	}

}
