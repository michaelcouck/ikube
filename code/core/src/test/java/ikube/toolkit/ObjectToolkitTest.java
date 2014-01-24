package ikube.toolkit;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexContext;
import org.junit.Test;

import static ikube.toolkit.ObjectToolkit.populateFields;
import static org.junit.Assert.assertTrue;

public class ObjectToolkitTest extends AbstractTest {

    @Test
    @SuppressWarnings("rawtypes")
    public void testPopulateFields() {
        indexContext = populateFields(IndexContext.class, new IndexContext(), true, 0, 3);
        assertTrue(indexContext.getId() > 0);
        assertTrue(indexContext.getName() != null);

        indexContext = populateFields(IndexContext.class, new IndexContext(), true, 0, 0, IConstants.ID);
        assertTrue(indexContext.getId() == 0);
        assertTrue(indexContext.getName() != null);
        assertTrue(indexContext.getBufferSize() != 0.0);
    }

}
