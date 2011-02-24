package ikube;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IConstants {

	/** Application name. */
	public String IKUBE = "ikube";
	/** The file separator for the system. */
	public String SEP = File.separator;

	/** We expect the configuration file to be in the META-INF as seems to be the fashion at the moment. */
	public String META_INF = SEP + "META-INF";

	/** The name of the spring file. */
	public String SPRING_XML = "spring.xml";
	/** Where the whole application is wired together. */
	public String SPRING_CONFIGURATION_FILE = META_INF + SEP + SPRING_XML;

	/** Maps mime types to file extensions.. */
	public String MIME_TYPES = META_INF + SEP + "mime" + SEP + "mime-types.xml";
	/** Maps parsers to mime types. */
	public String MIME_MAPPING = META_INF + SEP + "mime" + SEP + "mime-mapping.xml";

	public static String LOG_4_J_PROPERTIES = META_INF + "log4j.properties";

	/** The database file name. */
	public String DATABASE_FILE = "ikube.odb";
	/** The transaction files from Neodatis. */
	public String TRANSACTION_FILES = ".transaction";

	public Version VERSION = Version.LUCENE_30;
	public Analyzer ANALYZER = new StandardAnalyzer(VERSION);
	public long MAX_READ_LENGTH = 1000000;
	public int MAX_RESULT_FIELD_LENGTH = 1000;

	public String READER_FILE_SUFFIX = ".ikube";
	public int MAX_FRAGMENTS = 3;
	public String FRAGMENT_SEPERATOR = "...";

	public String ID = "id";
	public String INDEX = "index";
	public String SCORE = "score";
	public String CONTENTS = "contents";
	public String FRAGMENT = "fragment";
	public String TOTAL = "total";
	public String DURATION = "duration";
	public String TITLE = "title";
	public String NAME = "name";
	public String URL = "url";
	public String INDEXED = "indexed";
	public String HASH = "hash";
	public String START = "start";

	public String ENCODING = "UTF-8";

	/** This is the starting port for the synchronization sockets. */
	public int SYNCHRONIZATION_PORT = 9000;
	public int MAX_SYNCHRONIZATION_PORT = 10000;
	public String SYNCHRONIZATION_TOPIC = "synchronizationTopic";

	public String SYNCHRONIZATION = "synchronization";

	/** Url response codes */
	public Integer HTTP_200 = new Integer(200); // OK
	public Integer HTTP_301 = new Integer(301); // Move permanently
	public Integer HTTP_400 = new Integer(400); // Bad request
	public Integer HTTP_401 = new Integer(401); // Unauthorised
	public Integer HTTP_403 = new Integer(403); // Forbidden
	public Integer HTTP_404 = new Integer(404); // Not found
	public Integer HTTP_418 = new Integer(418); // I"m a tea pot
	public Integer HTTP_500 = new Integer(500); // Internal server error
	public Integer HTTP_503 = new Integer(503); // Service unavailable

	/** These can be extracted into the messages.properties file */
	public String RESPONSE_CODE_NOT_200 = "Response code not 200/OK";
	public String EXCEPTION_VISITING_PAGE = "Exception visiting page";
	public String EXCEPTION_VISITING_PAGE_UNKNOWN_ERROR_POSSIBLY_TIMEOUT = "Exception visiting page, error un-known, possibly a timeout";
	public String PAGE_RESPONSE_NULL = "Page returned from request null";
	public String URL_NOT_WELL_FORMED = "Url not well formed";
	public String EXCEPTION_CLICKING_LINK = "Exception clicking on the link";
	public String NO_END_TAG = "Not end tag in HTML";
	public String EXCEPTION_VALIDATING_HTML = "General exception validating the HTML";

}
