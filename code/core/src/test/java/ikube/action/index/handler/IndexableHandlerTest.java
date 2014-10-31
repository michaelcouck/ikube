package ikube.action.index.handler;

import ikube.AbstractTest;
import ikube.model.Indexable;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2014
 */
public class IndexableHandlerTest extends AbstractTest {

    private IndexableHandler indexableHandler;

    @Before
    public void before() {
        indexableHandler = mock(IndexableHandler.class);
        Deencapsulation.setField(indexableHandler, "logger", logger);
    }

    @Test
    @SuppressWarnings({"unchecked", "ThrowableResultOfMethodCallIgnored"})
    public void handleException() {
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                invocation.callRealMethod();
                return Void.class;
            }
        }).when(indexableHandler).handleException(any(Indexable.class), any(Exception.class), any(String[].class));
        Exception exception = mock(Exception.class);
        when(exception.getLocalizedMessage()).thenReturn("Terrible exception...");

        indexableHandler.handleException(indexableTable, exception, this.getClass().getSimpleName());
        verify(exception, atLeastOnce()).getLocalizedMessage();

        exception = mock(InterruptedException.class);
        try {
            indexableHandler.handleException(indexableTable, exception, this.getClass().getSimpleName());
            fail("Should have thrown the interrupted exception");
        } catch (final Exception e) {
            // Expecting this
        }

        exception = mock(Exception.class);
        when(indexableTable.getMaxExceptions()).thenReturn(1l);
        when(indexableTable.incrementAndGetExceptions()).thenReturn(2);
        try {
            indexableHandler.handleException(indexableTable, exception, this.getClass().getSimpleName());
            fail("Should have re-thrown the exception");
        } catch (final Exception e) {
            // Expecting this
        }
    }

}
