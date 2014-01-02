package ikube;

import java.util.regex.Pattern;

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
	/** The property for the configuration location. */
	String IKUBE_CONFIGURATION = IKUBE + ".configuration";
	/** The name of the log file. */
	String IKUBE_LOG = IKUBE + ".log";
	/** The file separator for the system. */
	String SEP = "/";
	String IKUBE_DIRECTORY = "." + SEP + IKUBE;
	String ANALYTICS_DIRECTORY = IKUBE_DIRECTORY + SEP + "analytics";
	
	String SPACE = " ";
	String BCK_SEP = "\\";
	String META_INF = SEP + "META-INF";
	/** The name of the spring file. */
	String SPRING_XML = "spring.xml";
	/** Where the whole application is wired together. */
	String SPRING_CONFIGURATION_FILE = SEP + IKUBE + SEP + SPRING_XML;
	/** The persistence units' names. */
	String PERSISTENCE_UNIT_H2 = "IkubePersistenceUnitH2";
	String PERSISTENCE_UNIT_DB2 = "IkubePersistenceUnitDb2";
	String PERSISTENCE_UNIT_ORACLE = "IkubePersistenceUnitOracle";
	String PERSISTENCE_UNIT_POSTGRES = "IkubePersistenceUnitPostgres";

	/** The default logging properties. */
	String LOG_4_J_PROPERTIES = META_INF + SEP + "log4j.properties";

	/** Maps mime types to file extensions.. */
	String MIME_TYPES = "mime-types.xml";
	/** Maps parsers to mime types. */
	String MIME_MAPPING = "mime-mapping.xml";

	Version LUCENE_VERSION = Version.LUCENE_46;

	long MAX_READ_LENGTH = 1000000;
	int MAX_RESULT_FIELD_LENGTH = 100;

	String READER_FILE_SUFFIX = ".ikube";
	int MAX_FRAGMENTS = 3;
	String FRAGMENT_SEPERATOR = "...";
	int RESET_DELETE_BATCH_SIZE = 1000;

	String ID = "id";
	String FILE_ID = "file-id";
	String INDEX = "index";
	String SCORE = "score";
	String CONTENT = "content";
	String CONTENTS = "contents";
	String FILE = "file";
	String FRAGMENT = "fragment";
	String MIME_TYPE = "mimeType";
	String TOTAL = "total";
	String DURATION = "duration";
	String TITLE = "title";
	String NAME = "name";
	String ALTERNATE_NAMES = "alternatenames";
	String URL = "url";
	String URL_DONE = "urlDone";
	String URL_ID = "urlId";
	String INDEXED = "indexed";
	String HASH = "hash";
	String START = "start";
	String END = "end";
	String RESULTS = "results";
	String STATISTICS = "statistics";
	String RESULTS_ROUTED = "resultsRouted";
	String ADDRESS = "address";
	String SERVER = "server";
	String SERVERS = "servers";
	String ACTION = "action";
	String ACTIONS = "actions";
	String WEB_SERVICE_URLS = "webServiceUrls";
	String INDEX_CONTEXT = "indexContext";
	String INDEX_CONTEXTS = "indexContexts";
	String INDEX_NAMES = "indexNames";
	String INDEX_FIELD_NAMES_AND_VALUES = "indexFieldNamesAndValues";
	String GEOSPATIAL = "geospatial";
	String AUTOCOMPLETE = "autocomplete";
	String DEFAULT = "default";
	String FEATURECLASS = "featureclass";
	String FEATURECODE = "featurecode";
	String COUNTRYCODE = "countrycode";
	String TIMEZONE = "timezone";
	String ASCIINAME = "asciiname";
	String CITY = "city";
	String COUNTRY = "country";
	String CORRECTIONS = "corrections";
	String ACTION_NAME = "actionName";
	String SORT_FIELD = "sortField";
	String DESCENDING = "descending";

	String ENCODING = "UTF-8";

	/** Url response codes */
	Integer HTTP_200 = 200; // OK
	Integer HTTP_301 = 301; // Move permanently
	Integer HTTP_400 = 400; // Bad request
	Integer HTTP_401 = 401; // Unauthorized
	Integer HTTP_403 = 403; // Forbidden
	Integer HTTP_404 = 404; // Not found
	Integer HTTP_418 = 418; // I"m a tea pot
	Integer HTTP_500 = 500; // Internal server error
	Integer HTTP_503 = 503; // Service unavailable

	String INDEX_NAME = "indexName";
	String MAX_RESULTS = "maxResults";

	String SEARCH_FIELDS = "searchFields";
	String TYPE_FIELDS = "typeFields";
	String SEARCH_STRINGS = "searchStrings";
	String TERM = "term";
	String TARGET_SEARCH_URL = "targetSearchUrl";
	String SORT_FIELDS = "sortFields";
	String FIRST_RESULT = "firstResult";

	/** The tags in the response from the Geo Location API. */
	String LAT = "lat";
	String LNG = "lng";
	String LOCATION = "location";

	String DISTANCE = "distance";

	String LATITUDE = "latitude";
	String LONGITUDE = "longitude";
	String POSITION_FIELD_NAME = "position";

	String EXCLUDED = "excluded";

	/** The maximum age that the server can get to before it is deleted from the cluster. */
	int MAX_AGE = 1800000;

	String STRIP_CHARACTERS = "|!,[]{};:/\\.-_";

	long MAX_ACTIONS = 10000;
	long MAX_SNAPSHOTS = 10000;
	long MAX_SERVERS = 100;

	String TIER = "tier";

	String SEARCH = "search";

	String TMP_UNZIPPED_FOLDER = "/tmp/unzipped";
	String STRING_PATTERN = ".*(\\.zip\\Z).*|.*(\\.jar\\Z).*|.*(\\.war\\Z).*|.*(\\.ear\\Z).*|.*(\\.gz\\Z).*|.*(\\.sar\\Z).*|.*(\\.tar\\Z).*|.*(\\.rar\\Z).*";
	Pattern ZIP_JAR_WAR_EAR_PATTERN = Pattern.compile(STRING_PATTERN);

	String LUCENE_CONJUNCTIONS_PATTERN_STRING = "and|or|between|not";
	Pattern LUCENE_CONJUNCTIONS_PATTERN = Pattern.compile(LUCENE_CONJUNCTIONS_PATTERN_STRING);

	int THREAD_POOL_SIZE = 100;

	String EXCEPTION = "exception";
	String EXCEPTION_STACK = "exceptionStack";
	String EXCEPTION_MESSAGE = "exceptionMessage";

	String SEMI_COLON = ";";

	String ENTITIES = "entities";
	String FIELD_NAMES = "fieldNames";

	String PROPERTIES = "properties";

	String TOTAL_SIZE = "totalSize";
	String TOTAL_DOCS = "totalDocs";

	String TOPIC = "topic";

	int MAX_RETRY_CLUSTER_REMOVE = 3;

	String ROLE_USER = "ROLE_USER";
	String ROLE_ADMIN = "ROLE_ADMIN";

	int TEN_THOUSAND = 10000;
	int HUNDRED_THOUSAND = TEN_THOUSAND * 10;
	int MILLION = HUNDRED_THOUSAND * 10;

	String DELIMITER_CHARACTERS = ";,|:";
	String LINE_NUMBER = "lineNumber";

	String LANGUAGE = "language";
	String LANGUAGE_ORIGINAL = "language-original";
	String LANGUAGE_DETECT_PROFILES_DIRECTORY = "profiles";
	String CLASSIFICATION = "classification";
	String CLASSIFICATION_CONFLICT = "classification-conflict";

	String POSITIVE = "positive";
	String NEGATIVE = "negative";
	String NEUTRAL = "neutral";
	String[] CATEGORIES = { POSITIVE, NEGATIVE };

	String ANALYZER = "classifier";
	String CLASSIFIERS = "classifiers";
	String CLASS = "class";
	String CLASS_ATTRIBUTE = "@@class@@";
	String TEXT = "text";
	String TEXT_ATTRIBUTE = "@@text@@";
	String TYPE = "type";
	String START_INDEX = "start-index";
	String END_INDEX = "end-index";
	String ENTITY = "entity";
	int MAX_GEOHASH_LEVELS = 11;

	String MUST = "must";
	String SHOULD = "should";
	String STRING = "string";
	String WORD = "word";
}