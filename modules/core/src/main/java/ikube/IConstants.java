package ikube;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

public interface IConstants {

	/** We expect the configuration file to be in the META-INF as seems to be the fashion at the moment. */
	public String META_INF = "/META-INF/";
	/** Maps mime types to file extensions.. */
	public String MIME_TYPES = META_INF + "mime/mime-types.xml";
	/** Maps parsers to mime types. */
	public String MIME_MAPPING = META_INF + "mime/mime-mapping.xml";

	/** The database file name. */
	public String DATABASE_FILE = "ikube.odb";
	/** The database name in JNDI. */
	public String DATABASE = "database";
	/** The port of the database. */
	public int PORT = 8000;

	public String SPRING_CONFIGURATION_FILE = META_INF + "spring.xml";

	public Analyzer ANALYZER = new StandardAnalyzer(Version.LUCENE_29);
	public boolean COMPOUND_FILE = Boolean.TRUE;
	public int BUFFERED_DOCS = 100;
	public int MAX_FIELD_LENGTH = 10000;
	public int MERGE_FACTOR = 100;
	public long RAM_BUFFER_SIZE = 256;
	public long MAX_READ_LENGTH = 1000000;
	public int MAX_RESULT_FIELD_LENGTH = 1000;

	public String READER_FILE_SUFFIX = ".igenius";
	public int MAX_FRAGMENTS = 3;
	public String FRAGMENT_SEPERATOR = "...";
	public Version VERSION = Version.LUCENE_30;

	public String INDEX = "index";
	public String ID = "id";
	public String SCORE = "score";
	public String CONTENTS = "contents";
	public String FRAGMENT = "fragment";
	public String TOTAL = "total";
	public String DURATION = "duration";
	public String TITLE = "title";

	public String INDEX_NAME = "indexName";
	public String SERVER_NAME = "serverName";

	public String ENCODING = "UTF8";

	public String CLASS_NAME = "className";

	public int SOURCE_PORT = 8082;
	public int TARGET_PORT = 8083;
}
