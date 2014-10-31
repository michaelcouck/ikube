package ikube.toolkit;

import org.apache.log4j.Logger;

public class SpnegoTokenGeneratorException extends RuntimeException {

	private static final long serialVersionUID = -2936423730559142381L;
	private static Logger LOGGER = Logger.getLogger(SpnegoTokenGeneratorException.class);

	public SpnegoTokenGeneratorException(String message) {
		super(message);
		LOGGER.error(message);
	}

	public SpnegoTokenGeneratorException(String message, Throwable cause) {
		super(message, cause);
		LOGGER.error(message, cause);
	}
}
