package ikube.integration.action;

import ikube.IConstants;
import ikube.action.Enrichment;
import ikube.action.Open;
import ikube.integration.AbstractIntegration;
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
public class EnrichmentTest extends AbstractIntegration {

	@Test
	@Ignore
	public void execute() throws Exception {
		// final IndexContext indexContext
		IndexContext<?> indexContext = ApplicationContextManager.getBean(IConstants.GEOSPATIAL);
		Open open = ApplicationContextManager.getBean(Open.class);
		open.execute(indexContext);
		Enrichment enrichment = ApplicationContextManager.getBean(Enrichment.class);
		enrichment.execute(indexContext);
		while (true) {
			Thread.sleep(1000);
		}
	}

}
