package ikube.index.parse.html;

import ikube.IConstants;
import ikube.index.parse.IParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftTagTypes;
import net.htmlparser.jericho.PHPTagTypes;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

/**
 * @author Michael Couck
 * @since 03.09.10
 * @version 01.00
 */
public class HtmlParser implements IParser {

	public HtmlParser() {
		MicrosoftTagTypes.register();
		PHPTagTypes.register();
		MasonTagTypes.register();
	}

	@Override
	public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
		Source source = new Source(reader);
		source.fullSequentialParse();
		TextExtractor textExtractor = new TextExtractor(source);
		textExtractor.setIncludeAttributes(true);
		textExtractor.setConvertNonBreakingSpaces(Boolean.TRUE);
		outputStream.write(textExtractor.toString().getBytes(IConstants.ENCODING));
		return outputStream;
	}

}
