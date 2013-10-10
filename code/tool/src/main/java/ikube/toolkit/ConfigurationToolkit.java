package ikube.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class just allows the context to be initialized from the command line or another class as a check that the configuration is correct.
 * 
 * @author Michael Couck
 * @since 10.10.13
 * @version 01.00
 */
public class ConfigurationToolkit {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationToolkit.class);

	public static void main(String[] args) {
		try {
			args = new String[] { "C:/Eclipse/workspace/ikube/code/libs/src/main/resources/indexes/bpost/spring-bpost-streets.xml", "street" };
			ApplicationContextManager.getApplicationContextFilesystem(args[0]);
			Object bean = ApplicationContextManager.getBean(args[1]);
			assert bean != null;
		} catch (Exception e) {
			LOGGER.info("Usage : [/path/to/spring.xml] [class.of.bean.in.ApplicationContext]");
			LOGGER.error(null, e);
		}
	}
}
