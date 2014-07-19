package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 05-12-2013
 */
public class ClassifierTrainingStrategyTest extends AbstractTest {

    @Spy
    @InjectMocks
    private ClassifierTrainingStrategy classifierTrainingStrategy;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aroundProcess() throws Exception {
        Assert.fail("Implement me");
    }

}