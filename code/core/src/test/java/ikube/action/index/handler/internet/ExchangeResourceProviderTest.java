package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexableExchange;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
public class ExchangeResourceProviderTest extends AbstractTest {

    @Mock
    private IndexableExchange indexableExchange;
    private ExchangeResourceProvider exchangeResourceProvider;

    @Test(expected = IllegalArgumentException.class)
    public void constructor() {
        exchangeResourceProvider = new ExchangeResourceProvider(indexableExchange);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getResource() {
        exchangeResourceProvider = new ExchangeResourceProvider(indexableExchange);
        exchangeResourceProvider.getResource();
    }

    @Test(expected = IllegalArgumentException.class)
    public void setResources() {
        exchangeResourceProvider = new ExchangeResourceProvider(indexableExchange);
        exchangeResourceProvider.setResources(null);
    }

}