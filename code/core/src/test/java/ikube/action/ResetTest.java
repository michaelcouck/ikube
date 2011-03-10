package ikube.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.cluster.IClusterManager;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ResetTest extends BaseTest {

	private transient final Reset reset = new Reset();

	@Test
	public void execute() {
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		Url url = new Url();
		url.setUrl("dummy");
		url.setId(HashUtilities.hash(url.getUrl()));
		clusterManager.set(Url.class, url.getId(), url);

		int size = clusterManager.size(Url.class);
		assertTrue("There should be only one : ", size >= 1);

		reset.execute(indexContext);

		size = clusterManager.size(Url.class);
		assertEquals("There should be no urls in the cache : ", 0, size);
	}

}
