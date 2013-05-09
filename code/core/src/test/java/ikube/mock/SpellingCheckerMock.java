package ikube.mock;

import ikube.search.spelling.SpellingChecker;
import mockit.Mock;
import mockit.MockClass;

import org.mockito.Mockito;

@MockClass(realClass = SpellingChecker.class)
public class SpellingCheckerMock {

	@Mock
	public static SpellingChecker getSpellingChecker() {
		return Mockito.mock(SpellingChecker.class);
	}

	@Mock
	public String checkWord(final String word) {
		return word;
	}

}
