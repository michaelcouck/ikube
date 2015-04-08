package ikube.action.rule;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FILE;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;

import static org.junit.Assert.assertFalse;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 19-04-2014
 */
public class IsRemoteIndexCurrentIntegration extends IntegrationTest {

    /**
     * Class under test.
     */
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private IsRemoteIndexCurrent isRemoteIndexCurrent;
    @Autowired
    @Qualifier(IConstants.GEOSPATIAL)
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private IndexContext geospatialIndexContext;

    @Before
    public void before() {
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(geospatialIndexContext);
        FILE.deleteFile(new File(indexDirectoryPath));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void evaluate() throws Exception {
        boolean indexCurrent = isRemoteIndexCurrent.evaluate(geospatialIndexContext);
        assertFalse("This index should never be current in the tests : ", indexCurrent);
    }

}