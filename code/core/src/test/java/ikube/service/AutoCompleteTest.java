package ikube.service;

import ikube.ATest;

import java.util.Arrays;

import org.junit.Test;

public class AutoCompleteTest extends ATest {

	public AutoCompleteTest() {
		super(AutoCompleteTest.class);
	}

	@Test
	public void suggestions() throws Exception {
		AutoComplete autoComplete = new AutoComplete();
		String[] suggestions = autoComplete.suggestions("steve");
		logger.info("Suggestions : " + Arrays.deepToString(suggestions));
	}

}
