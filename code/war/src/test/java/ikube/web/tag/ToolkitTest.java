package ikube.web.tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		assertEquals("The quick ...", subString);

		startPosition = 10;
		maxLength = 22;
		subString = Toolkit.subString(string, startPosition, maxLength);
		assertEquals("brown fox jumped over ...", subString);

		startPosition = 40;
		maxLength = 80;
		subString = Toolkit.subString(string, startPosition, maxLength);
		assertEquals(" dog.", subString);
	}

	@Test
	public void queryString() {
		Map<Object, Object> parameterMap = new HashMap<Object, Object>();
		parameterMap.put("parameterName", new String[] { "parameterValue" });
		parameterMap.put("replacementName", new String[] { "originalValue" });
		List<Object> parameterNamesReplacements = Arrays.asList();
		List<Object> parameterValuesReplacements = Arrays.asList();
		String queryString = Toolkit.queryString(parameterMap, parameterNamesReplacements, parameterValuesReplacements);
		assertEquals("?replacementName=originalValue&parameterName=parameterValue&", queryString);

		parameterNamesReplacements = Arrays.asList((Object) "replacementName");
		parameterValuesReplacements = Arrays.asList((Object) "replacementValue");
		queryString = Toolkit.queryString(parameterMap, parameterNamesReplacements, parameterValuesReplacements);
		assertEquals("?replacementName=replacementValue&parameterName=parameterValue&", queryString);
	}

	@Test
	public void asList() {
		List<Object> list = Toolkit.asList("object");
		list = Toolkit.asList(new Object[] { "object", "object", "object" });
		assertEquals(3, list.size());
	}
	
	@Test
	public void getDocumentIcon() {
		String def = "blank.ico";
		String path = "/the/path/to/the/word.doc";
		String icons = "pdf.ico;doc.ico;html.ico;xml.gif;txt.jpg";
		String icon = Toolkit.documentIcon(path, icons, def);
		assertEquals("doc.ico", icon);
		
		path = "/the/path/to/the/text.txt";
		icon = Toolkit.documentIcon(path, icons, def);
		assertEquals("txt.jpg", icon);
		
		path = "/the/path/to/the/text.wierd";
		icon = Toolkit.documentIcon(path, icons, def);
		assertEquals(def, icon);
		
		path = "http://code.google.com/p/ikube/";
		icon = Toolkit.documentIcon(path, icons, def);
		assertEquals(def, icon);
	}

}