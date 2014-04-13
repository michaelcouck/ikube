package ikube;

import org.apache.lucene.util.Version;

import java.util.regex.Pattern;

/**
 * Constants for Ikube.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public interface IConstants {

    /**
     * Application name and other bits and pieces use this constant, like the generic grid map.
     */
    String IKUBE = "ikube";
    /**
     * This constant is for the server map on the grid.
     */
    String SERVER = "server";
    /**
     * The property for the configuration location.
     */
    String IKUBE_CONFIGURATION = IKUBE + ".configuration";
    /**
     * The name of the log file.
     */
    String IKUBE_LOG = IKUBE + ".log";
    /**
     * The file separator for the system.
     */
    String SEP = "/";
    String IKUBE_DIRECTORY = "." + SEP + IKUBE;
    String ANALYTICS_DIRECTORY = IKUBE_DIRECTORY + SEP + "analytics";

    String SPACE = " ";
    String BCK_SEP = "\\";
    /**
     * The name of the spring file.
     */
    String SPRING_XML = "spring.xml";
    /**
     * Where the whole application is wired together.
     */
    String SPRING_CONFIGURATION_FILE = SEP + IKUBE + SEP + SPRING_XML;
    /**
     * The persistence units' names.
     */
    String PERSISTENCE_UNIT_H2 = "IkubePersistenceUnitH2";
    String PERSISTENCE_UNIT_DB2 = "IkubePersistenceUnitDb2";
    String PERSISTENCE_UNIT_ORACLE = "IkubePersistenceUnitOracle";
    String PERSISTENCE_UNIT_POSTGRES = "IkubePersistenceUnitPostgres";

    /**
     * Maps mime types to file extensions..
     */
    String MIME_TYPES = "mime-types.xml";
    /**
     * Maps parsers to mime types.
     */
    String MIME_MAPPING = "mime-mapping.xml";

    Version LUCENE_VERSION = Version.LUCENE_46;

    long MAX_READ_LENGTH = 1000000;
    int MAX_RESULT_FIELD_LENGTH = 100;

    String READER_FILE_SUFFIX = ".ikube";
    int MAX_FRAGMENTS = 3;
    String FRAGMENT_SEPARATOR = "...";
    int RESET_DELETE_BATCH_SIZE = 1000;

    String ID = "id";
    String FILE_ID = "file-id";
    String INDEX = "index";
    String SCORE = "score";
    String CONTENT = "content";
    String CONTENTS = "contents";
    String FRAGMENT = "fragment";
    String MIME_TYPE = "mimeType";
    String TOTAL = "total";
    String DURATION = "duration";
    String TITLE = "title";
    String NAME = "name";
    String INDEXED = "indexed";
    String URL = "url";
    String HASH = "hash";
    String END = "end";
    String ADDRESS = "address";
    String INDEX_CONTEXT = "indexContext";
    String GEOSPATIAL = "geospatial";
    String AUTOCOMPLETE = "autocomplete";
    String COUNTRY = "country";
    String CORRECTIONS = "corrections";
    String SORT_FIELD = "sortField";
    String DESCENDING = "descending";

    String ENCODING = "UTF-8";
    String APPLICATION_JSON = "application/json";
    String CONTENT_TYPE = "Content-Type";

    String INDEX_NAME = "indexName";
    String MAX_RESULTS = "maxResults";

    String SEARCH_FIELDS = "searchFields";
    String TYPE_FIELDS = "typeFields";
    String SEARCH_STRINGS = "searchStrings";
    String SORT_FIELDS = "sortFields";
    String FIRST_RESULT = "firstResult";

    /**
     * The tags in the response from the Geo Location API.
     */
    String LAT = "lat";
    String LNG = "lng";
    String LOCATION = "location";

    String DISTANCE = "distance";

    String LATITUDE = "latitude";
    String LONGITUDE = "longitude";
    String POSITION_FIELD_NAME = "position";

    /**
     * The maximum age that the server can get to before it is deleted from the cluster.
     */
    int MAX_AGE = 180000;

    String STRIP_CHARACTERS = "|!,[]{};:/\\.-_";

    long MAX_ACTIONS = 10000;
    long MAX_SNAPSHOTS = 10000;
    long MAX_SERVERS = 100;

    String TIER = "tier";

    String SEARCH = "search";

    String TMP_UNZIPPED_FOLDER = "/tmp/ikube-unzipped";
    String STRING_PATTERN = ".*(\\.zip\\Z).*|.*(\\.jar\\Z).*|.*(\\.war\\Z).*|.*(\\.ear\\Z).*|.*(\\.gz\\Z).*|.*(\\.sar\\Z).*|.*(\\.tar\\Z).*|.*(\\.rar\\Z).*";
    Pattern ZIP_JAR_WAR_EAR_PATTERN = Pattern.compile(STRING_PATTERN);

    int THREAD_POOL_SIZE = 100;

    String EXCEPTION = "exception";
    String EXCEPTION_STACK = "exception-stack";
    String EXCEPTION_MESSAGE = "exception-message";

    String SEMI_COLON = ";";

    String TOPIC = "topic";
    String EXECUTOR_SERVICE = "executor-service";

    int ONE_THOUSAND = 1000;
    int TEN_THOUSAND = ONE_THOUSAND * 10;
    int HUNDRED_THOUSAND = TEN_THOUSAND * 10;
    int MILLION = HUNDRED_THOUSAND * 10;

    String DELIMITER_CHARACTERS = ";,|:";

    String LANGUAGE = "language";
    String LANGUAGE_ORIGINAL = "language-original";
    String LANGUAGE_PROFILES_DIRECTORY = "language-profiles";
    String CLASSIFICATION = "classification";
    String CLASSIFICATION_CONFLICT = "classification-conflict";

    String POSITIVE = "positive";
    String NEGATIVE = "negative";
    String NEUTRAL = "neutral";

    String ANALYZER = "classifier";
    String CLASS = "class";
    String START_INDEX = "start-index";
    String END_INDEX = "end-index";
    int MAX_GEOHASH_LEVELS = 7;

    String MUST = "must";
    String SHOULD = "should";
    String STRING = "string";
    String WORD = "word";
    String RANGE = "range";
    String HAZELCAST_WATCHER = "hazelcast-watcher";
    String APPLICATION_CONTEXT_REFRESHER = "application-context-refresher";

    long SIXTY_SECONDS = 1000 * 60;
    String TIMESTAMP = "timestamp";
}