package ikube.action.index.handler.internet;

import static ikube.toolkit.ObjectToolkit.populateFields;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import ikube.AbstractTest;
import ikube.model.IndexableInternet;
import ikube.model.Url;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class InternetResourceProviderTest extends AbstractTest {

	private InternetResourceProvider internetResourceProvider;

	@Before
	public void before() {
		IndexableInternet indexableInternet = populateFields(new IndexableInternet(), Boolean.TRUE, Integer.MAX_VALUE, "parent");
		internetResourceProvider = new InternetResourceProvider(indexableInternet);
	}

	@Test
	public void getResource() {
		Url resourceUrl = internetResourceProvider.getResource();
		assertNotNull(resourceUrl);

		resourceUrl = internetResourceProvider.getResource();
		assertNull(resourceUrl);
	}

	@Test
	public void setResources() {
		Url resourceUrl = internetResourceProvider.getResource();
		assertNotNull(resourceUrl);

		internetResourceProvider.setResources(Arrays.asList(resourceUrl));
		resourceUrl = internetResourceProvider.getResource();
		assertNull(resourceUrl);
	}

}