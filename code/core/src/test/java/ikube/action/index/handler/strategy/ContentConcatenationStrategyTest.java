package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.model.Url;
import org.apache.lucene.document.Document;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 13-08-2014
 */
public class ContentConcatenationStrategyTest extends AbstractTest {

    @Spy
    @InjectMocks
    private ContentConcatenationStrategy contentConcatenationStrategy;

    @Test
    @SuppressWarnings("unchecked")
    public void aroundProcess() throws Exception {
        Url url = new Url();
        url.setParsedContent("Hello world.");
        Document document = new Document();
        contentConcatenationStrategy.aroundProcess(indexContext, indexableTable, document, url);
        // TODO: Finish this test
    }

}