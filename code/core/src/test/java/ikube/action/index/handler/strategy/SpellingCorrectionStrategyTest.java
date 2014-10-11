package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.search.spelling.SpellingChecker;
import mockit.Deencapsulation;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 01-07-2013
 */
public class SpellingCorrectionStrategyTest extends AbstractTest {

    @Spy
    private SpellingChecker spellingChecker;
    @Spy
    private SpellingCorrectionStrategy spellingCorrectionStrategy;

    @Before
    public void before() {
        Whitebox.setInternalState(spellingCorrectionStrategy, "spellingChecker", spellingChecker);
    }

    @Test
    public void aroundProcess() throws Exception {
        when(spellingChecker.checkWord(anyString())).thenReturn("Thee", "words", "to", "be", "corrected");

        String resource = "Theee, wordss:;to |be (corrrected)";
        spellingCorrectionStrategy.aroundProcess(indexContext, indexableTable, new Document(), resource);
        verify(indexableTable, times(1)).setContent("Thee words to be corrected");
    }

    @Test
    public void aroundProcessDistance() throws Exception {
        Deencapsulation.setField(spellingCorrectionStrategy, "maxSpellingDistanceAllowed", 0.3);
        when(spellingChecker.checkWord(anyString())).thenReturn("Thee", "words", "to", "be", "corrected");

        String resource = "Theee, wordss:;to |aa (zzzzz)";
        spellingCorrectionStrategy.aroundProcess(indexContext, indexableTable, new Document(), resource);
        verify(indexableTable, times(1)).setContent("Thee words to");
    }

}
