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
        when(spellingChecker.checkWord("Theee")).thenReturn("Thee");
        when(spellingChecker.checkWord("wordss")).thenReturn("words");
        when(spellingChecker.checkWord("to")).thenReturn("to");
        when(spellingChecker.checkWord("be")).thenReturn("be");
        when(spellingChecker.checkWord("corrrected")).thenReturn("corrected");

        String resource = "Theee, wordss:;to |be (corrrected)";
        spellingCorrectionStrategy.aroundProcess(indexContext, indexableTable, new Document(), resource);
        verify(indexableTable, times(1)).setContent("Thee, words:;to |be (corrected)");
    }

    @Test
    public void aroundProcessDistance() throws Exception {
        Deencapsulation.setField(spellingCorrectionStrategy, "maxSpellingDistanceAllowed", 0.3);

        when(spellingChecker.checkWord("Theee")).thenReturn("Thee");
        when(spellingChecker.checkWord("wordss")).thenReturn("words");
        when(spellingChecker.checkWord("to")).thenReturn("to");
        when(spellingChecker.checkWord("aa")).thenReturn("aaaaaaaaa");
        when(spellingChecker.checkWord("zzzzz")).thenReturn("zzzzzzzzzzzzzzzz");

        String resource = "Theee, wordss:;to |aa (zzzzz)";
        spellingCorrectionStrategy.aroundProcess(indexContext, indexableTable, new Document(), resource);
        verify(indexableTable, times(1)).setContent("Thee, words:;to |aa (zzzzz)");
    }

}