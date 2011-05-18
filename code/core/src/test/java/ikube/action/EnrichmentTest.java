package ikube.action;

import ikube.ATest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
@Ignore
public class EnrichmentTest extends ATest {

	public EnrichmentTest() {
		super(EnrichmentTest.class);
	}

	@Test
	public void execute() {
		// final IndexContext indexContext
		IndexContext indexContext = ApplicationContextManager.getBean(IConstants.GEOSPATIAL);
		Enrichment enrichment = new Enrichment();
		try {
			Thread.sleep(1000 * 15);
		} catch (InterruptedException e) {
			logger.error("Interrupted : ", e);
		}
		enrichment.execute(indexContext);
	}

}
