package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.database.IDataBase;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.junit.Assert.fail;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
@Ignore
public class ExchangeHandlerTest extends AbstractTest {

    @Mock
    private IDataBase dataBase;
    @Mock
    private ExchangeResourceHandler exchangeResourceHandler;

    @Spy
    @InjectMocks
    private ExchangeHandler exchangeHandler;

    @Test
    public void handleIndexableForked() throws Exception {
        fail("Please implement me :(");
    }

    @Test
    public void handleResource() {
        fail("Please implement me :(");
    }

}