package ikube.index.parse.html;

import ikube.index.parse.IParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
	public final OutputStream parse(InputStream inputStream) throws Exception {
		Source source = new Source(inputStream);
		source.fullSequentialParse();
		TextExtractor textExtractor = new TextExtractor(source);
		textExtractor.setIncludeAttributes(true);
		OutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(textExtractor.toString().getBytes());
		return outputStream;
	}

}
