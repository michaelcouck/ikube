package ikube.integration.strategy;

import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftTagTypes;
import net.htmlparser.jericho.PHPTagTypes;

import org.apache.log4j.Logger;

public abstract class AStrategy implements IStrategy {

	protected Logger logger;
	protected int port;
	protected String context;

	AStrategy(String context, int port) {
		logger = Logger.getLogger(this.getClass());
		this.context = context;
		this.port = port;
		MicrosoftTagTypes.register();
		PHPTagTypes.register();
		MasonTagTypes.register();
	}

}
