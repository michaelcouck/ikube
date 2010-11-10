package ikube.search;

import ikube.IConstants;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;



/**
 * This action does the actual search on the index. The searcher that is current in the Instance is passed to this action. The search is
 * done on the index. The results are then processed for use in the front end. A list of maps is generated from the results. There are three
 * standard fields in each map. Each map then represents one record or result from the search. The three standard items in the map are the
 * index in the lucene result set, id of the record and the score that the result got. Optionally the fragment generated from the result if
 * this is specified.
 *
 * The id of the record generated using the name of the object indexed and the primary field in the database.
 *
 * For paging functionality the search method can be called specifying the start and end parameters which will give logical paging. Although
 * the search will be done for each page forward the search is so fast that this is not relevant.
 *
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
public abstract class Search {

	/** The query parsers for various query fields. */
	private static final Map<String, QueryParser> QUERY_PARSERS = new HashMap<String, QueryParser>();

	protected Logger logger;
	/** The searcher that will be used for the search. */
	protected Searcher searcher;
	/** Whether to generate fragments for the search string or not. */
	protected boolean fragment;
	/** The start position in the search results to return maps from. */
	protected int firstResult;
	/** The end position in the results to stop returning results from. */
	protected int maxResults;

	/**
	 * Constructor takes the searcher and the search string to look for. For each search one of these lightweight classes is created
	 * specifying the fragment parameter, the start and end of the results to return and the maximum results to return.
	 *
	 * @param searcher
	 *            the searcher to use for the search
	 * @param searchString
	 *            the string that we are looking for in the index
	 * @param fragment
	 *            whether to fragment the results like ...the best <b>fragment</b> of them all...
	 * @param firstResult
	 *            the start in the index results. This facilitates paging from clients
	 * @param maxResults
	 *            the end in the results to return. Also part of the paging functionality
	 */
	Search(Searcher searcher) {
		this.searcher = searcher;
		this.logger = Logger.getLogger(this.getClass());
	}

	/**
	 * Takes a result from the Lucene search query and selects the fragments that have the search word(s) in it, taking only the first few
	 * instances of the data where the term appears and returns the fragments. For example in the document the data is the following "The
	 * quick brown fox jumps over the lazy dog" and we search for 'quick', 'fox' and 'lazy'. The result will be '...The quick brown fox
	 * jumps...the lazy dog...'.<br>
	 * <br>
	 * The fragments are from the current document, so calling get next document will move the document to the next on in the Hits object.
	 *
	 * @param the
	 *            document to get the fragments from
	 * @param fieldName
	 *            the name of the field that was searched
	 * @param the
	 *            query that generated the results
	 * @return the best fragments of text containing the search keywords
	 */
	protected String getFragments(Document document, String fieldName, Query query) {
		String fragment = null;
		try {
			Scorer scorer = new QueryScorer(query);
			Highlighter highlighter = new Highlighter(scorer);
			String content = document.get(fieldName);
			// If the content is not stored in the index then there is no fragment
			if (content == null) {
				logger.debug("No field stored content : " + fieldName);
				return fragment;
			}
			StringReader stringReader = new StringReader(content);
			TokenStream tokenStream = IConstants.ANALYZER.tokenStream(fieldName, stringReader);
			fragment = highlighter.getBestFragments(tokenStream, content, IConstants.MAX_FRAGMENTS, IConstants.FRAGMENT_SEPERATOR);
		} catch (Exception t) {
			logger.error("Exception getting the best fragments for the search results", t);
		}
		return fragment;
	}

	/**
	 * Adds the fields to the results.
	 *
	 * @param document
	 *            the document to get the fields from to add to the result
	 * @param result
	 *            the result map to add the field values to
	 * @throws Exception
	 */
	protected void addFieldsToResults(Document document, Map<String, String> result) throws Exception {
		for (Fieldable field : document.getFields()) {
			String fieldName = field.name();
			String stringValue = field.stringValue();
			if (stringValue != null) {
				if (stringValue.length() > IConstants.MAX_RESULT_FIELD_LENGTH) {
					stringValue = stringValue.substring(0, IConstants.MAX_RESULT_FIELD_LENGTH);
				}
				result.put(fieldName, stringValue);
			}
		}
	}

	/**
	 * Sets the strings that will be searched for.
	 *
	 * @param searchStrings
	 *            the search strings
	 */
	public abstract void setSearchString(String... searchStrings);

	/**
	 * Sets the fields in the index that will be searched for.
	 *
	 * @param searchFields
	 *            the fields in the index to search through
	 */
	public abstract void setSearchField(String... searchFields);

	/**
	 * This executed the search with the parameters set for the search fields and the search strings.
	 *
	 * @return the results which are a list of maps. Each map has the fields in it if they are strings, not readers, and the map entries for
	 *         index, score, fragment, total and duration
	 */
	public abstract List<Map<String, String>> execute();

	/**
	 * Sets whether the fragment made of the best part of the document should be included in the search results.
	 *
	 * @param fragment
	 *            the flag for generating the best fragments in the results
	 */
	public void setFragment(boolean fragment) {
		this.fragment = fragment;
	}

	/**
	 * Sets the first result in the index.
	 *
	 * @param start
	 *            the first result to return from the results
	 */
	public void setFirstResult(int start) {
		this.firstResult = start;
	}

	/**
	 * Sets the maximum results to return.
	 *
	 * @param maxResults
	 *            the maximum results to return
	 */
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * Access to the query parsers for a particular field in the documents.
	 *
	 * @param searchField
	 *            the name of the field that needs to be searched
	 * @return the query parser for the particular field
	 */
	protected QueryParser getQueryParser(String searchField) {
		QueryParser queryParser = QUERY_PARSERS.get(searchField);
		if (queryParser == null) {
			queryParser = new QueryParser(IConstants.VERSION, searchField, IConstants.ANALYZER);
			QUERY_PARSERS.put(searchField, queryParser);
		}
		return queryParser;
	}

}