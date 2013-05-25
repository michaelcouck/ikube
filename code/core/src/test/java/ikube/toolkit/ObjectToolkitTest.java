package ikube.toolkit;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexContext;

import org.junit.Test;

public class ObjectToolkitTest extends AbstractTest {

	@Test
	@SuppressWarnings("rawtypes")
	public void populateFields() {
		indexContext = ObjectToolkit.populateFields(IndexContext.class, new IndexContext(), true, 0, 3);
		assertTrue(indexContext.getId() > 0);
		assertTrue(indexContext.getName() != null);

		indexContext = ObjectToolkit.populateFields(IndexContext.class, new IndexContext(), true, 0, 0, IConstants.ID);
		assertTrue(indexContext.getId() == 0);
		assertTrue(indexContext.getName() != null);
		assertTrue(indexContext.getBufferSize() != 0.0);
	}

}
