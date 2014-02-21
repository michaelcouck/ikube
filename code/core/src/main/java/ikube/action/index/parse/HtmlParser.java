package ikube.action.index.parse;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import net.htmlparser.jericho.*;
import org.slf4j.*;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

/**
 * TODO Redo this parser.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 03-09-2010
 */
public class HtmlParser implements IParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlParser.class);

    public HtmlParser() {
        MicrosoftTagTypes.register();
        PHPTagTypes.register();
        MasonTagTypes.register();
    }

    @Override
    public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
        try {
            Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
            Source source = new Source(reader);
            source.fullSequentialParse();
            TextExtractor textExtractor = new TextExtractor(source);
            textExtractor.setExcludeNonHTMLElements(Boolean.TRUE);
            textExtractor.setIncludeAttributes(Boolean.TRUE);
            textExtractor.setConvertNonBreakingSpaces(Boolean.TRUE);
            String extractedText = textExtractor.toString();
            byte[] bytes = extractedText.getBytes(IConstants.ENCODING);
            LOGGER.debug("Text length : " + extractedText.length() + ", " + bytes.length);
            outputStream.write(bytes);
        } finally {
            FileUtilities.close(inputStream);
        }
        return outputStream;
    }

}
