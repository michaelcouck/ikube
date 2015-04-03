package ikube.action.rule;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FILE;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 19-04-2014
 */
@Ignore
public class IsRemoteIndexCurrentIntegration extends IntegrationTest {

    /**
     * Class under test.
     */
    private IsRemoteIndexCurrent isRemoteIndexCurrent;

    @Before
    public void before() {
        isRemoteIndexCurrent = ApplicationContextManager.getBean(IsRemoteIndexCurrent.class);
        IndexContext geospatialIndexContext = ApplicationContextManager.getBean(IConstants.GEOSPATIAL);
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(geospatialIndexContext);
        FILE.deleteFile(new File(indexDirectoryPath));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void evaluate() throws Exception {
        IndexContext indexContext = ApplicationContextManager.getBean(IConstants.GEOSPATIAL);
        boolean indexCurrent = isRemoteIndexCurrent.evaluate(indexContext);
        assertFalse("This index should never be current in the tests : ", indexCurrent);
    }

}