package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
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

    @Test
    public void aroundProcess() throws Exception {
        // There are no moe tweets, so training it dead now
    }

}