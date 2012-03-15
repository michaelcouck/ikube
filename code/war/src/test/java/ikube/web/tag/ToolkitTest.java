package ikube.web.tag;

import static org.junit.Assert.*;

import org.junit.Test;

public class ToolkitTest {

	@Test
	public void subString() {
		String subString = Toolkit.subString(null, 0, 100);
		assertNull(subString);
		
		String string = "The quick brown fox jumped over the lazy dog.";
		int startPosition = 0;
		int maxLength = 10;
		subString = Toolkit.subString(string, startPosition, maxLength);
		assertEquals("The quick ", subString);

		startPosition = 10;
		maxLength = 22;
		subString = Toolkit.subString(string, startPosition, maxLength);
		assertEquals("brown fox jumped over ", subString);

		startPosition = 40;
		maxLength = 80;
		subString = Toolkit.subString(string, startPosition, maxLength);
		assertEquals(" dog.", subString);
	}

}
