package ikube.index.parse.html;

import ikube.index.parse.IParser;
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
	public final String parse(String string) throws Exception {
		Source source = new Source(string);
		source.fullSequentialParse();
		TextExtractor textExtractor = new TextExtractor(source);
		textExtractor.setIncludeAttributes(true);
		return textExtractor.toString();
	}

}
