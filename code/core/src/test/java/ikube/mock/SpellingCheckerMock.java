package ikube.mock;

import ikube.search.spelling.SpellingChecker;
import mockit.Mock;
import mockit.MockClass;

@MockClass(realClass = SpellingChecker.class)
public class SpellingCheckerMock {

    @Mock
    @SuppressWarnings("UnusedDeclaration")
    public String checkWord(final String word) {
        return word;
    }

}
