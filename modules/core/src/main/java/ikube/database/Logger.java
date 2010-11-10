package ikube.database;

import org.neodatis.tool.ILogger;

public class Logger implements ILogger {

	private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

	@Override
	public void debug(Object object) {
		// logger.debug(object);
	}

	@Override
	public void info(Object object) {
		// logger.info(object);
	}

	@Override
	public void error(Object object) {
		// logger.error(object);
	}

	@Override
	public void error(Object object, Throwable t) {
		logger.error(object, t);
	}

}
