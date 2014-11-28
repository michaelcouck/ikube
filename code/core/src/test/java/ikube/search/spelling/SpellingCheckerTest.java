package ikube.search.spelling;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.mock.SpellingCheckerMock;
import ikube.search.Search;
import ikube.toolkit.PERFORMANCE;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-03-2011
 */
public class SpellingCheckerTest extends AbstractTest {

    @MockClass(realClass = Search.class)
    public static class SearchMock {
        @Mock
        public ArrayList<HashMap<String, String>> execute() {
            HashMap<String, String> result = new HashMap<>();
            result.put(IConstants.WORD, IConstants.WORD);
            ArrayList<HashMap<String, String>> results = new ArrayList<>();
            results.add(result);
            return results;
        }
    }

    private SpellingChecker spellingChecker;

    @Before
    public void before() throws Exception {
        Mockit.tearDownMocks(SpellingChecker.class);
        Mockit.setUpMocks(SearchMock.class);
        spellingChecker = new SpellingChecker();
        Deencapsulation.setField(spellingChecker, "indexContext", indexContext);
    }

    @After
    public void after() {
        Mockit.setUpMocks(SpellingCheckerMock.class);
    }

    @Test
    public void checkWords() {
        String corrected = spellingChecker.checkWord("wrongk");
        assertEquals(IConstants.WORD, corrected);
    }

    @Test
    public void checkPerformance() {
        double iterationsPerSecond = PERFORMANCE.execute(new PERFORMANCE.APerform() {
            @Override
            public void execute() throws Throwable {
                spellingChecker.checkWord("michael");
            }
        }, "Spelling checking performance : ", 1000, Boolean.FALSE);
        assertTrue(iterationsPerSecond > 100);
        iterationsPerSecond = PERFORMANCE.execute(new PERFORMANCE.APerform() {
            @Override
            public void execute() throws Throwable {
                spellingChecker.checkWord("couck");
            }
        }, "Spelling checking performance : ", 1000, Boolean.FALSE);
        assertTrue(iterationsPerSecond > 100);
    }

}