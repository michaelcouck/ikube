package ikube.action.rule;

import ikube.AbstractTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * This rule checks if this server is working this index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 03.02.2014
 */
public class IsThisServerWorkingThisIndexTest extends AbstractTest {

    @Spy
    @InjectMocks
    private IsThisServerWorkingThisIndex isThisServerWorkingThisIndex;

    @Test
    public void evaluate() {
        boolean working = isThisServerWorkingThisIndex.evaluate(indexContext);
        assertTrue(working);
        verify(indexContext, times(2)).getIndexName();
    }

}
