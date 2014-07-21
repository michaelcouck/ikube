package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.action.index.handler.IStrategy;
import ikube.model.Indexable;
import ikube.toolkit.PerformanceTester;
import org.apache.lucene.document.Document;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static ikube.toolkit.PerformanceTester.execute;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 01-07-2013
 */
public class LanguageCleaningStrategyTest extends AbstractTest {

    private Object resource = new Object();
    private Document document = new Document();

    @Mock
    private Indexable indexable;
    @Mock
    private IStrategy nextStrategy;
    @Spy
    @InjectMocks
    private LanguageCleaningStrategy languageCleaningStrategy;

    @Test
    public void aroundProcess() throws Exception {
        when(indexable.getContent()).thenReturn("What a looovely dai where theere are moneyy mistaces aaaahhhhhhh thereererereee.");
        languageCleaningStrategy.aroundProcess(indexContext, indexable, document, resource);
        verify(indexable, atLeastOnce()).setContent("What a loovely dai where theere are moneyy mistaces aahh thereerereree.");

        double perSecond = execute(new PerformanceTester.APerform() {
            public void execute() throws Throwable {
                languageCleaningStrategy.aroundProcess(indexContext, indexable, new Document(), resource);
            }
        }, "Language detection strategy : ", 1000, Boolean.TRUE);
        assertTrue(perSecond > 1000);
    }

    @Test
    public void punctuation() throws Exception {
        when(indexable.getContent()).thenReturn("First sentence.No punctuation.Ever it seems.");
        languageCleaningStrategy.aroundProcess(indexContext, indexable, document, resource);
        verify(indexable, atLeastOnce()).setContent("First sentence. No punctuation. Ever it seems.");
    }


}
