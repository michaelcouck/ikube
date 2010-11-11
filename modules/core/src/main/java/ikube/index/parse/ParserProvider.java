package ikube.index.parse;

import ikube.index.parse.html.HtmlParser;
import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeType;
import ikube.index.parse.mime.MimeTypes;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class ParserProvider {

	private static Logger LOGGER = Logger.getLogger(ParserProvider.class);

	private static Map<String, IParser> PARSERS = new HashMap<String, IParser>();

	public static IParser getParser(String mimeTypeString, byte[] bytes) {
		MimeType mimeType = MimeTypes.getMimeTypeFromName(mimeTypeString);
		if (mimeType == null) {
			mimeType = MimeTypes.getMimeType(bytes);
		}
		// We go for the default and send it to the HTML parser which will do it's best to parse the data
		if (mimeType == null) {
			try {
				mimeType = new MimeType("text/html");
			} catch (Exception t) {
				LOGGER.error("Exception creating mime type for text parser", t);
			}
		}
		// Initialise the parser
		String parserClass = MimeMapper.getParserClass(mimeType.getName());
		IParser parser = null;
		try {
			parser = PARSERS.get(parserClass);
			if (parser == null) {
				if (parserClass == null) {
					// The fall-back parser is the HTML parser
					parserClass = HtmlParser.class.getName();
				}
				parser = (IParser) Class.forName(parserClass).newInstance();
				PARSERS.put(parserClass, parser);
			}
		} catch (Exception t) {
			LOGGER.error("Exception instanciating parser " + parserClass + " does a parser exist for the mime type " + mimeType
					+ " and name " + mimeTypeString + "?", t);
		}
		return parser;
	}

}
