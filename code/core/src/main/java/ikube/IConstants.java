package ikube;

import java.text.SimpleDateFormat;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

/**
 * Constants for Ikube.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IConstants {

	/** Application name. */
	String IKUBE = "ikube";
	/** The name of the log file. */
	String IKUBE_LOG = IKUBE + ".log";
	/** The file separator for the system. */
	String SEP = "/";
	/** We expect the configuration file to be in the META-INF as seems to be the fashion at the moment. */
	String META_INF = SEP + "META-INF";
	/** The name of the spring file. */
	String SPRING_XML = "spring.xml";
	/** Where the whole application is wired together. */
	String SPRING_CONFIGURATION_FILE = META_INF + SEP + SPRING_XML;
	/** The persistence units' names. */
	String PERSISTENCE_UNIT_H2 = "IkubePersistenceUnitH2";
	String PERSISTENCE_UNIT_DB2 = "IkubePersistenceUnitDb2";
	String PERSISTENCE_UNIT_ORACLE = "IkubePersistenceUnitOracle";
	String PERSISTENCE_UNIT_OBJECT_DB = "IkubePersistenceUnitObjectDb";

	/** The default logging properties. */
	String LOG_4_J_PROPERTIES = META_INF + SEP + "log4j.properties";

	/** Maps mime types to file extensions.. */
	String MIME_TYPES = META_INF + SEP + "mime" + SEP + "mime-types.xml";
	/** Maps parsers to mime types. */
	String MIME_MAPPING = META_INF + SEP + "mime" + SEP + "mime-mapping.xml";

	/** The database file name. */
	String DATABASE_FILE = "ikube.odb";
	/** The transaction files from Neodatis. */
	String TRANSACTION_FILES = ".transaction";

	Version VERSION = Version.LUCENE_30;
	Analyzer ANALYZER = new StandardAnalyzer(VERSION);
	long MAX_READ_LENGTH = 1000000;
	int MAX_RESULT_FIELD_LENGTH = 1000;

	String READER_FILE_SUFFIX = ".ikube";
	int MAX_FRAGMENTS = 3;
	String FRAGMENT_SEPERATOR = "...";

	String ID = "id";
	String INDEX = "index";
	String SCORE = "score";
	String CONTENT = "content";
	String CONTENTS = "contents";
	String FRAGMENT = "fragment";
	String TOTAL = "total";
	String DURATION = "duration";
	String TITLE = "title";
	String NAME = "name";
	String URL = "url";
	String URL_DONE = "urlDone";
	String URL_HASH = "urlHash";
	String INDEXED = "indexed";
	String HASH = "hash";
	String START = "start";
	String END = "end";
	String RESULTS = "results";
	String ADDRESS = "address";
	String SERVER = "server";
	String SERVERS = "servers";
	String ACTIONS = "actions";
	String WEB_SERVICE_URLS = "webServiceUrls";
	String INDEX_CONTEXTS = "indexContexts";
	String INDEX_NAMES = "indexNames";
	String GEOSPATIAL = "geospatial";
	String FEATURECLASS = "featureclass";
	String FEATURECODE = "featurecode";
	String COUNTRYCODE = "countrycode";
	String TIMEZONE = "timezone";
	String ASCIINAME = "asciiname";

	String ENCODING = "UTF-8";

	/** Url response codes */
	Integer HTTP_200 = Integer.valueOf(200); // OK
	Integer HTTP_301 = Integer.valueOf(301); // Move permanently
	Integer HTTP_400 = Integer.valueOf(400); // Bad request
	Integer HTTP_401 = Integer.valueOf(401); // Unauthorized
	Integer HTTP_403 = Integer.valueOf(403); // Forbidden
	Integer HTTP_404 = Integer.valueOf(404); // Not found
	Integer HTTP_418 = Integer.valueOf(418); // I"m a tea pot
	Integer HTTP_500 = Integer.valueOf(500); // Internal server error
	Integer HTTP_503 = Integer.valueOf(503); // Service unavailable

	/** These can be extracted into the messages.properties file */
	String NO_END_TAG = "Not end tag in HTML";
	String URL_NOT_WELL_FORMED = "Url not well formed";
	String EXCEPTION_VISITING_PAGE = "Exception visiting page";
	String PAGE_RESPONSE_NULL = "Page returned from request null";
	String RESPONSE_CODE_NOT_200 = "Response code not 200/OK";
	String EXCEPTION_CLICKING_LINK = "Exception clicking on the link";
	String EXCEPTION_VALIDATING_HTML = "General exception validating the HTML";
	String EXCEPTION_VISITING_PAGE_POSSIBLY_TIMEOUT = "Exception visiting page, error un-known, possibly a timeout";

	String INDEX_NAME = "indexName";
	String MAX_RESULTS = "maxResults";
	String SEARCH_FIELDS = "searchFields";
	String SEARCH_STRINGS = "searchStrings";
	String SORT_FIELDS = "sortFields";
	String FIRST_RESULT = "firstResult";
	String INDEXABLES = "indexables";

	/** The tags in the response from the Geo Location API. */
	String LAT = "lat";
	String LNG = "lng";
	String LOCATION = "location";

	String DISTANCE = "distance";

	String LATITUDE = "latitude";
	String LONGITUDE = "longitude";

	String EXCEPTION_TOPIC = "exceptionTopic";
	String SHUTDOWN_TOPIC = "shutdownTopic";

	/** Lock objects for cluster wide locking while we update the cache. */
	String SERVER_LOCK = "serverLock";
	/** The timeout to wait for the lock. */
	long LOCK_TIMEOUT = 10000;
	/** We only keep a few actions in the server. */
	double MAX_ACTION_SIZE = 50;
	/** The ratio to delete the actions when the maximum is reached. */
	double ACTION_PRUNE_RATIO = 0.3;
	/** The maximum age that the server can get to before it is deleted from the cluster. */
	int MAX_AGE = 600000;

	String INDEX_SIZE = "indexSize";
	String INDEX_DOCUMENTS = "indexDocuments";

	String SEARCHING_EXECUTIONS = "searchingExecutions";
	String INDEXING_EXECUTIONS = "indexingExecutions";

	/** The length of the log that should be passed around. */
	long TAIL_LOG = 10000;
	
	SimpleDateFormat HHMMSS_DDMMYYYY = new SimpleDateFormat("hh:mm:ss ddMMyyyy");

}