package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.model.Indexable;
import ikube.search.spelling.SpellingChecker;
import ikube.toolkit.PerformanceTester;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 01-07-2013
 */
public class LanguageCleaningStrategyTest extends AbstractTest {

    private LanguageCleaningStrategy languageCleaningStrategy;

    @Before
    public void before() throws Exception {
        new SpellingChecker();
        languageCleaningStrategy = new LanguageCleaningStrategy();
        languageCleaningStrategy.initialize();
    }

    @Test
    public void aroundProcess() throws Exception {
        final Indexable<?> indexable = Mockito.mock(Indexable.class);
        final Object resource = new Object();
        Mockito.when(indexable.getContent()).thenReturn(
                "What a looovely dai where theere are moneyy mistaces aaaahhhhhhh thereererereee yooooouuuuu aaarrrrreeee");
        Document document = new Document();
        languageCleaningStrategy.aroundProcess(indexContext, indexable, document, resource);
        Mockito.verify(indexable, Mockito.atLeastOnce()).setContent("what a loovely dai where theere are moneyy mistaces aahh thereerereree yoouu aarree");

        double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
            public void execute() throws Throwable {
                languageCleaningStrategy.aroundProcess(indexContext, indexable, new Document(), resource);
            }
        }, "Language detection strategy : ", 1000, Boolean.TRUE);
        assertTrue(perSecond > 1000);
    }


}
