package ikube.action.index.parse.mime;

import ikube.toolkit.FILE;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
public final class MimeMapper {

	private static final Logger LOGGER = Logger.getLogger(MimeMapper.class);
	private static final Map<String, String> MAPPING = new HashMap<String, String>();

	public MimeMapper(final String fileName) {
		InputStream inputStream = null;
		try {
			File file = FILE.findFileRecursively(new File("."), fileName);
			inputStream = new FileInputStream(file);
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
						MAPPING.put(type.getValue(), parser.getValue());
					}
				}
			}
		} catch (Exception e) {
			String message = "Exception loading the mapping for parsers : " + fileName;
			LOGGER.error(message, e);
			// throw new RuntimeException(message, e);
		} finally {
			FILE.close(inputStream);
		}
	}

	public static String getParserClass(final String mimeType) {
		return MAPPING.get(mimeType);
	}

}
