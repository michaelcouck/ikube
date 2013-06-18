package ikube;

/**
 * Constants for Ikube.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface Constants {

	/** Application name. */
	String IKUBE = "ikube";
	/** The name of the log file. */
	String IKUBE_LOG = IKUBE + ".log";
	/** The file separator for the system. */
	String SEP = "/";
	String META_INF = SEP + "META-INF";
	/** The default logging properties. */
	String LOG_4_J_PROPERTIES = META_INF + SEP + "log4j.properties";
	/** System encoding. */
	String ENCODING = "UTF-8";
	
	/** The property for the configuration location. */
	String IKUBE_CONFIGURATION = IKUBE + ".configuration";
}