package ikube;

public interface ITools {

	/** Application name. */
	String IKUBE = "ikube";
	/** The file separator for the system. */
	String SEP = "/"; // File.separator;
	/** We expect the configuration file to be in the META-INF as seems to be the fashion at the moment. */
	String META_INF = SEP + "META-INF";
	/** The name of the spring file. */
	String SPRING_XML = "spring.xml";
	/** Where the whole application is wired together. */
	String SPRING_CONFIGURATION_FILE = META_INF + SEP + SPRING_XML;
	/** The default persistence unit name. */
	String PERSISTENCE_UNIT_NAME = "ToolsPersistenceUnit";

	/** The default logging properties. */
	String LOG_4_J_PROPERTIES = ITools.META_INF + ITools.SEP + "log4j.properties";
	/** The default platform encoding. */
	String ENCODING = "UTF8";

}
