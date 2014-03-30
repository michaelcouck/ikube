package ikube.action.rule;

import ikube.AbstractTest;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 30-03-2014
 */
public class IsRemoteIndexCurrentTest extends AbstractTest {

    /**
     * Class under test.
     */
    private IsRemoteIndexCurrent isRemoteIndexCurrent;

    @Before
    public void before() {
        isRemoteIndexCurrent = new IsRemoteIndexCurrent();
        Deencapsulation.setField(isRemoteIndexCurrent, "clusterManager", clusterManager);
    }

    @MockClass(realClass = IsIndexCurrentCallable.class)
    public static class IsIndexCurrentCallableMock {

        @Mock
        public Boolean call() throws Exception {
            return Boolean.TRUE;
        }

    }

    @Test
    @SuppressWarnings("unchecked")
    public void evaluate() throws Exception {
        List<Future<Boolean>> futures = new ArrayList<>();
        Future<Boolean> futureOne = mock(Future.class);
        setFuture(futureOne, Boolean.FALSE, Boolean.TRUE);
        futures.add(futureOne);

        when(clusterManager.sendTaskToAll(any(Callable.class))).thenReturn(futures);

        boolean isIndexCurrent = isRemoteIndexCurrent.evaluate(indexContext);
        assertFalse(isIndexCurrent);

        Future<Boolean> futureTwo = mock(Future.class);
        futures.add(futureTwo);
        setFuture(futureOne, Boolean.FALSE, Boolean.TRUE);
        setFuture(futureTwo, Boolean.TRUE, Boolean.TRUE);

        isIndexCurrent = isRemoteIndexCurrent.evaluate(indexContext);
        assertTrue(isIndexCurrent);

        setFuture(futureTwo, Boolean.FALSE, Boolean.TRUE);
        isIndexCurrent = isRemoteIndexCurrent.evaluate(indexContext);
        assertFalse(isIndexCurrent);
    }

    private void setFuture(final Future<Boolean> future, final boolean result, final boolean done) throws Exception {
        when(future.get()).thenReturn(result);
        when(future.isDone()).thenReturn(done);
    }

}