package ikube.mock;

import ikube.search.spelling.SpellingChecker;
import mockit.Mock;
import mockit.MockClass;

import org.mockito.Mockito;

@MockClass(realClass = SpellingChecker.class)
public class SpellingCheckerMock {

	@Mock
	public static final SpellingChecker getSpellingChecker() {
		return Mockito.mock(SpellingChecker.class);
	}

	@Mock
	public final String checkWords(String searchString) {
		return null;
	}

}
