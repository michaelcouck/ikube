package ikube.index.parse;

import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeType;
import ikube.index.parse.mime.MimeTypes;
import ikube.index.parse.text.TextParser;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ParserProvider {

	private static Logger LOGGER = Logger.getLogger(ParserProvider.class);

	private static Map<String, IParser> PARSERS = new HashMap<String, IParser>();

	public static IParser getParser(String mimeTypeString, byte[] bytes) {
		MimeType mimeType = null;
		String parserClass = null;
		IParser parser = null;
		try {
			mimeType = MimeTypes.getMimeTypeFromName(mimeTypeString);
			if (mimeType == null) {
				mimeType = MimeTypes.getMimeType(bytes);
			}
			// We go for the default and send it to the HTML parser which will do it's best to parse the data
			if (mimeType == null) {
				mimeType = new MimeType("text/html");
			}
			// Initialize the parser
			parserClass = MimeMapper.getParserClass(mimeType.getName());
			parser = PARSERS.get(parserClass);
			if (parser == null) {
				if (parserClass == null) {
					// The fall-back parser is the text parser
					parserClass = TextParser.class.getName();
				}
				parser = (IParser) Class.forName(parserClass).newInstance();
				PARSERS.put(parserClass, parser);
			}
			return parser;
		} catch (Exception t) {
			LOGGER.error("Exception instanciating parser " + parserClass + " does a parser exist for the mime type " + mimeType
					+ " and name " + mimeTypeString + "?", t);
		}
		return null;
	}

}
