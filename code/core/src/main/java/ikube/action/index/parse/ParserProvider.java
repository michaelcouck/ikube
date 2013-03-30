package ikube.action.index.parse;

import ikube.action.index.parse.mime.MimeMapper;
import ikube.action.index.parse.mime.MimeType;
import ikube.action.index.parse.mime.MimeTypes;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Provides parsers based on the content(magic offsets) and content type like 'text/html' for example.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public final class ParserProvider {

	private static final Logger LOGGER = Logger.getLogger(ParserProvider.class);

	/**
	 * Singularity.
	 */
	private ParserProvider() {
		//M Documented
	};

	/** The map of content type to parsers. */
	private static final Map<String, IParser> PARSERS_MIME_TYPE = new HashMap<String, IParser>();
	private static final Map<String, IParser> PARSERS_CLASS_NAME_PARSER = new HashMap<String, IParser>();

	/**
	 * This method will try to find the best parser for the content type based on the data and the content type. The content type can be
	 * something like 'index.html' and the html parser will be returned. If the mimeType is null or not known then the bytes passed will be
	 * used to get the parser. For example in the case or a Word doc, if the extension missing then we expect a byte of '31be0000' at offset
	 * 0.
	 * 
	 * @param mimeTypeString
	 *            the mime type or the document extension
	 * @param bytes
	 *            the first few bytes of the document, typically 1024
	 * @return the parser that most closely matches the mime type and the data or the text parser if there is no matching parser, like for
	 *         exe files for example
	 */
	public static IParser getParser(final String mimeTypeString, final byte[] bytes) {
		MimeType mimeType = null;
		String parserClassName = null;
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
			parser = PARSERS_MIME_TYPE.get(mimeType.getName());
			if (parser == null) {
				parserClassName = MimeMapper.getParserClass(mimeType.getName());
				parser = PARSERS_CLASS_NAME_PARSER.get(parserClassName);
				if (parser == null) {
					if (parserClassName == null) {
						// The fall-back parser is the text parser
						parserClassName = TextParser.class.getName();
					}
					LOGGER.info("Locating the text parser for : " + mimeType.getName() + ", " + mimeTypeString + ", " + parserClassName);
					parser = (IParser) Class.forName(parserClassName).newInstance();
					PARSERS_CLASS_NAME_PARSER.put(parserClassName, parser);
				}
				PARSERS_MIME_TYPE.put(mimeType.getName(), parser);
			}
		} catch (Exception t) {
			LOGGER.error("Exception instanciating parser " + parserClassName + " does a parser exist for the mime type " + mimeType
					+ " and name " + mimeTypeString + "?", t);
		}
		return parser;
	}

}
