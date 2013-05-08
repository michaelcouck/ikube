package ikube.toolkit;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexContext;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectToolkitTest extends AbstractTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectToolkitTest.class);

	public ObjectToolkitTest() {
		super(ObjectToolkitTest.class);
	}

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
