package ikube;

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

	/** Maps mime types to file extensions.. */
	String MIME_TYPES = ITools.META_INF + ITools.SEP + "mime" + ITools.SEP + "mime-types.xml";
	/** Maps parsers to mime types. */
	String MIME_MAPPING = ITools.META_INF + ITools.SEP + "mime" + ITools.SEP + "mime-mapping.xml";

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
	String CONTENTS = "contents";
	String FRAGMENT = "fragment";
	String TOTAL = "total";
	String DURATION = "duration";
	String TITLE = "title";
	String NAME = "name";
	String URL = "url";
	String INDEXED = "indexed";
	String HASH = "hash";
	String START = "start";

	String ENCODING = "UTF-8";

	/** This is the starting port for the synchronization sockets. */
	int SYNCHRONIZATION_PORT = 9000;
	int MAX_SYNCHRONIZATION_PORT = 10000;
	String SYNCHRONIZATION_TOPIC = "synchronizationTopic";

	String SYNCHRONIZATION = "synchronization";

	/** Url response codes */
	Integer HTTP_200 = Integer.valueOf(200); // OK
	Integer HTTP_301 = Integer.valueOf(301); // Move permanently
	Integer HTTP_400 = Integer.valueOf(400); // Bad request
	Integer HTTP_401 = Integer.valueOf(401); // Unauthorised
	Integer HTTP_403 = Integer.valueOf(403); // Forbidden
	Integer HTTP_404 = Integer.valueOf(404); // Not found
	Integer HTTP_418 = Integer.valueOf(418); // I"m a tea pot
	Integer HTTP_500 = Integer.valueOf(500); // Internal server error
	Integer HTTP_503 = Integer.valueOf(503); // Service unavailable

	/** These can be extracted into the messages.properties file */
	String RESPONSE_CODE_NOT_200 = "Response code not 200/OK";
	String EXCEPTION_VISITING_PAGE = "Exception visiting page";
	String EXCEPTION_VISITING_PAGE_POSSIBLY_TIMEOUT = "Exception visiting page, error un-known, possibly a timeout";
	String PAGE_RESPONSE_NULL = "Page returned from request null";
	String URL_NOT_WELL_FORMED = "Url not well formed";
	String EXCEPTION_CLICKING_LINK = "Exception clicking on the link";
	String NO_END_TAG = "Not end tag in HTML";
	String EXCEPTION_VALIDATING_HTML = "General exception validating the HTML";

	String INDEX_NAME = "indexName";
	String MAX_RESULTS = "maxResults";
	String SEARCH_FIELDS = "searchFields";
	String SEARCH_STRINGS = "searchStrings";
	String SORT_FIELDS = "sortFields";
	String FIRST_RESULT = "firstResult";

	/** The tags in the response from the Geo Location API. */
	String LAT = "lat";
	String LNG = "lng";
	String LOCATION = "location";

	String DISTANCE = "distance";

}