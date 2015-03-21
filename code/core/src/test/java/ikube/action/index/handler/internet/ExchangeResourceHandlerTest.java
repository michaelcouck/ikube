package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexableMessage;
import ikube.model.IndexableExchange;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.mockito.Mockito.when;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
@Ignore
public class ExchangeResourceHandlerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private ExchangeResourceHandler exchangeResourceHandler;
    @Mock
    private IndexableExchange indexableExchange;

    @Before
    public void before() {
        when(indexableExchange.getContent()).thenReturn("Hello world!");
        when(indexableExchange.isAnalyzed()).thenReturn(Boolean.TRUE);
        when(indexableExchange.isTokenized()).thenReturn(Boolean.TRUE);
        when(indexableExchange.isStored()).thenReturn(Boolean.TRUE);
    }

    @Test
    public void handleResource() throws Exception {
        exchangeResourceHandler.handleResource(indexContext, indexableExchange, new Document(), indexableExchange);
    }

    @Test
    public void parseContent() {
        Document document = new Document();
        IndexableMessage indexableMessage = new IndexableMessage();
        exchangeResourceHandler.parseContent(indexableExchange, document, indexableMessage);
    }

}
