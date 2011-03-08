package ikube.index.parse.mime;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Maps mime types to parser classes.
 * 
 * @author Michael Couck
 * @since 12.05.04
 * @version 01.00
 */
public class MimeMapper {

	private static final HashMap<String, String> MAPPING = new HashMap<String, String>();

	public MimeMapper(final String filePath) {
		try {
			InputStream inputStream = getClass().getResourceAsStream(filePath);
			SAXReader reader = new SAXReader();
			Document doc = reader.read(inputStream);
			Element root = doc.getRootElement();
			List<?> allElements = root.elements();
			for (int i = 0; i < allElements.size(); i++) {
				Element element = (Element) allElements.get(i);
				if (element.getName().equals("mime-type")) {
					Attribute type = element.attribute("type");
					Attribute parser = element.attribute("parser");
					if (type != null && parser != null) {
						try {
							// System.out.println(type.getValue() + ":" + parser.getValue());
							MAPPING.put(type.getValue(), parser.getValue());
						} catch (Exception t) {
							t.printStackTrace();
						}
					}
				}
			}
		} catch (Exception t) {
			t.printStackTrace();
		}
	}

	public static String getParserClass(final String mimeType) {
		return (String) MAPPING.get(mimeType);
	}

}
