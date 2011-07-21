package ikube.web.tag;

import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

/**
 * TODO Comment me!
 * 
 * @author Michael Couck
 * @since 10.02.10
 * @version 01.00
 */
public abstract class ATag extends BodyTagSupport {

	/**
	 * Wrapper for the next url and the page number for the next block of results.
	 * 
	 * @author Michael Couck
	 */
	public static class Url {
		public String url;
		public Integer page;

		public Url(String url, Integer page) {
			this.url = url;
			this.page = page;
		}
	}

	public static final String RESULTS = "results";
	public static final String DURATION = "duration";
	public static final String CHECKED_SEARCH_STRING = "checkedSearchString";

	/** The items below are the attribute names in the session for the required variables. */
	protected static final String TOTAL = "total";
	protected static final String FIRST_RESULT = "firstResult";
	protected static final String MAX_RESULTS = "maxResults";
	/** The maximum paged results, i.e. up to 100 results if the max results are 10. */
	protected static final int MAX_PAGED_RESULTS = 10;
	/** The default encoding is UTF-8 of course. */
	protected static final String ENCODING = "UTF-8";

	protected static final String CONTENT = "content";
	public static final String SEARCH_STRINGS = "searchStrings";
	
	protected transient Logger logger;
	
	public ATag() {
		super();
		this.logger = Logger.getLogger(this.getClass());
	}

}
