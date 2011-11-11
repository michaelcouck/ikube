package ikube.integration;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Ignore;
import org.junit.Test;

/**
 * This test is just for running the enrichment from within Eclipse, so not really a test just a stand alone enrichment runner essentially.
 * 
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
@Ignore
public class EnrichmentTest extends AbstractIntegration {

	@Test
	public void execute() throws Exception {
		// final IndexContext indexContext
		IndexContext<?> indexContext = ApplicationContextManager.getBean(IConstants.GEOSPATIAL);
		Enrichment enrichment = new Enrichment();
		while (indexContext.getIndex().getMultiSearcher() == null) {
			Thread.sleep(1000);
		}
		Thread.sleep(1000);
		enrichment.execute();
		while (true) {
			Thread.sleep(1000);
		}
	}

}
