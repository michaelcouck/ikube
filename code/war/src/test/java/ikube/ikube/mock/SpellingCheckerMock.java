package ikube.ikube.mock;

import ikube.search.spelling.SpellingChecker;
import mockit.Mock;
import mockit.MockClass;

@MockClass(realClass = SpellingChecker.class)
public class SpellingCheckerMock {

	// final static SpellingChecker SPELLING_CHECKER = Mockito.mock(SpellingChecker.class);

//	@Mock
//	public static SpellingChecker getSpellingChecker() {
//		return SPELLING_CHECKER;
//	}

	@Mock
	public String checkWord(final String word) {
		return word;
	}

}
