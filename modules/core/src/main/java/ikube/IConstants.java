package ikube;

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

	/** We expect the configuration file to be in the META-INF as seems to be the fashion at the moment. */
	public String META_INF = "/META-INF";

	/** Where the whole application is wired together. */
	public String SPRING_CONFIGURATION_FILE = META_INF + "/spring.xml";

	/** Maps mime types to file extensions.. */
	public String MIME_TYPES = META_INF + "/mime/mime-types.xml";
	/** Maps parsers to mime types. */
	public String MIME_MAPPING = META_INF + "/mime/mime-mapping.xml";

	/** The database file name. */
	public String DATABASE_FILE = "ikube.odb";

	public Analyzer ANALYZER = new StandardAnalyzer(Version.LUCENE_29);
	public long MAX_READ_LENGTH = 1000000;
	public int MAX_RESULT_FIELD_LENGTH = 1000;

	public String READER_FILE_SUFFIX = ".ikube";
	public int MAX_FRAGMENTS = 3;
	public String FRAGMENT_SEPERATOR = "...";
	public Version VERSION = Version.LUCENE_30;

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

	public String ENCODING = "UTF8";

}
