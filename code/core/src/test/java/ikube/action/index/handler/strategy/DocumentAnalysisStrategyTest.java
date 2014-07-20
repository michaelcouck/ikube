package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20-07-2014
 */
public class DocumentAnalysisStrategyTest extends AbstractTest {

    @Spy
    @InjectMocks
    private DocumentAnalysisStrategy documentAnalysisStrategy;


    @Before
    public void before() throws Exception {
    }

    @Test
    public void aroundProcess() throws Exception {
        Assert.fail("Implement me");
    }

    @Test
    public void highestVotedClassification() {
        Assert.fail("Implement me");
    }

    @Test
    public void breakDocumentIntoSentences() {
        Assert.fail("Implement me");
    }

}