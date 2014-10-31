package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.model.File;
import ikube.model.Indexable;
import ikube.model.Url;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

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
    public void concatenateContent() throws Exception {
        Url url = new Url();
        url.setParsedContent("Hello world.");
        String content = contentConcatenationStrategy.concatenateContent(indexableTable, url, new StringBuilder());
        assertEquals(url.getParsedContent(), content);

        File file = new File();
        when(indexableTable.getContent()).thenReturn("content");
        content = contentConcatenationStrategy.concatenateContent(indexableTable, file, new StringBuilder());
        assertEquals(indexableTable.getContent(), content);

        when(indexableTable.getChildren()).thenReturn(new ArrayList<Indexable>(Arrays.asList(indexableColumn)));
        when(indexableColumn.getContent()).thenReturn("column content");
        content = contentConcatenationStrategy.concatenateContent(indexableTable, file, new StringBuilder());
        assertEquals(indexableTable.getContent() + " " + indexableColumn.getContent(), content);
    }

}