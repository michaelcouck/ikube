package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.database.IDataBase;
import ikube.model.IndexableExchange;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
public class ExchangeHandlerTest extends AbstractTest {

    @Mock
    private IDataBase dataBase;
    @Mock
    private IndexableExchange indexableExchange;
    @Mock
    private ExchangeResourceHandler exchangeResourceHandler;
    @Spy
    @InjectMocks
    private ExchangeHandler exchangeHandler;

    @Test(expected = IllegalArgumentException.class)
    public void handleIndexableForked() throws Exception {
        // TODO: This test needs to be done when the logic is done
        exchangeHandler.handleIndexableForked(indexContext, indexableExchange);
    }

    @Test
    public void handleResource() {
        // TODO: This test needs to be done when the logic is done
        exchangeHandler.handleResource(indexContext, indexableExchange, indexableExchange);
    }

}