package ikube.search;

import ikube.IConstants;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;

/**
 * TODO - synchronize this class or create one for each search.
 * 
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

	protected Logger logger;
	/** The searcher that will be used for the search. */
	protected transient Searcher searcher;
	/** The query parsers for various query fields. */
	private final transient Map<String, QueryParser> queryParsers;

	/** The search string that we are looking for. */
	protected transient String[] searchStrings;
	/** The fields in index to add to the search. */
	protected transient String[] searchFields;
	/** The fields to sort the results by. */
	protected transient String[] sortFields;

	/** Whether to generate fragments for the search string or not. */
	protected transient boolean fragment;
	/** The start position in the search results to return maps from. */
	protected transient int firstResult;
	/** The end position in the results to stop returning results from. */
	protected transient int maxResults;

	Search(final Searcher searcher) {
		this.logger = Logger.getLogger(this.getClass());
		this.searcher = searcher;
		this.queryParsers = new HashMap<String, QueryParser>();
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
	protected String getFragments(final Document document, final String fieldName, final Query query) {
		String fragment = null;
		try {
			Scorer scorer = new QueryScorer(query);
			Highlighter highlighter = new Highlighter(scorer);
			String content = document.get(fieldName);
			// If the content is not stored in the index then there is no fragment
			if (content == null) {
				// logger.debug("No field stored content : " + fieldName);
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
	protected void addFieldsToResults(final Document document, final Map<String, String> result) throws Exception {
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
	public void setSearchString(final String... searchStrings) {
		this.searchStrings = searchStrings;
	}

	/**
	 * Sets the fields in the index that will be searched for.
	 * 
	 * @param searchFields
	 *            the fields in the index to search through
	 */
	public void setSearchField(final String... searchFields) {
		this.searchFields = searchFields;
	}

	public void setSortField(final String... sortFields) {
		this.sortFields = sortFields;
	}

	/**
	 * This executed the search with the parameters set for the search fields and the search strings.
	 * 
	 * @return the results which are a list of maps. Each map has the fields in it if they are strings, not readers, and the map entries for
	 *         index, score, fragment, total and duration
	 */
	public List<Map<String, String>> execute() {
		if (searcher == null) {
			logger.warn("No searcher on any index, is an index created?");
		}
		long duration = 0;
		long totalHits = 0;
		List<Map<String, String>> results = null;
		try {
			Query query = getQuery();
			long start = System.currentTimeMillis();
			TopDocs topDocs = search(query);
			duration = System.currentTimeMillis() - start;
			totalHits = topDocs.totalHits;
			results = getResults(topDocs, query);
		} catch (Exception e) {
			logger.error("Exception searching for string " + searchStrings[0] + " in searcher " + searcher, e);
			if (results == null) {
				results = new ArrayList<Map<String, String>>();
			}
		}
		// Add the search results size as a last result
		addStatistics(results, totalHits, duration);
		return results;
	}

	/**
	 * Access to the query. This can be any number of query types, defined by the sub classes.
	 * 
	 * @return the Lucene query based on the parameters passed to the sub classes, like a span query etc.
	 * @throws ParseException
	 */
	protected abstract Query getQuery() throws ParseException;

	/**
	 * Does the actual search on the Lucene index.
	 * 
	 * @param query
	 *            the query to execute against the index
	 * @return the top documents from the search
	 * @throws IOException
	 */
	protected abstract TopDocs search(Query query) throws IOException;

	/**
	 * Sets whether the fragment made of the best part of the document should be included in the search results.
	 * 
	 * @param fragment
	 *            the flag for generating the best fragments in the results
	 */
	public void setFragment(final boolean fragment) {
		this.fragment = fragment;
	}

	/**
	 * Sets the first result in the index.
	 * 
	 * @param start
	 *            the first result to return from the results
	 */
	public void setFirstResult(final int start) {
		this.firstResult = start;
	}

	/**
	 * Sets the maximum results to return.
	 * 
	 * @param maxResults
	 *            the maximum results to return
	 */
	public void setMaxResults(final int maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * Access to the query parsers for a particular field in the documents.
	 * 
	 * @param searchField
	 *            the name of the field that needs to be searched
	 * @return the query parser for the particular field
	 */
	protected QueryParser getQueryParser(final String searchField) {
		QueryParser queryParser = queryParsers.get(searchField);
		if (queryParser == null) {
			queryParser = new QueryParser(IConstants.VERSION, searchField, IConstants.ANALYZER);
			queryParsers.put(searchField, queryParser);
		}
		return queryParser;
	}

	/**
	 * Builds the list of results(a list of maps) from the top documents returned from Lucene.
	 * 
	 * @param topDocs
	 *            the top documents from the Lucene search
	 * @param query
	 *            the query that was used for the query
	 * @return the list of results from the search
	 */
	protected List<Map<String, String>> getResults(final TopDocs topDocs, final Query query) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		long totalHits = topDocs.totalHits;
		long scoreHits = topDocs.scoreDocs.length;
		for (int i = 0; i < totalHits && i < scoreHits; i++) {
			if (i < firstResult) {
				continue;
			}
			try {
				Map<String, String> result = new HashMap<String, String>();
				Document document = searcher.doc(topDocs.scoreDocs[i].doc);
				float score = topDocs.scoreDocs[i].score;
				String index = Integer.toString(topDocs.scoreDocs[i].doc);
				result.put(IConstants.INDEX, index);
				result.put(IConstants.SCORE, Float.toString(score));
				addFieldsToResults(document, result);
				if (fragment) {
					StringBuilder builder = new StringBuilder();
					for (String searchField : searchFields) {
						String fragment = getFragments(document, searchField, query);
						if (fragment != null) {
							builder.append(fragment);
							builder.append(' ');
						}
					}
					result.put(IConstants.FRAGMENT, builder.toString());
				}
				results.add(result);
			} catch (Exception e) {
				logger.error("Exception building the results from the Lucene search : ", e);
			}
		}
		return results;
	}

	/**
	 * Adds the time it took for the search etc.
	 * 
	 * @param results
	 *            the total number of results
	 * @param totalHits
	 *            the total hits
	 * @param duration
	 *            how long the search took in milliseconds
	 */
	protected void addStatistics(final List<Map<String, String>> results, final long totalHits, final long duration) {
		// Add the search results size as a last result
		Map<String, String> statistics = new HashMap<String, String>();
		statistics.put(IConstants.TOTAL, Long.toString(totalHits));
		statistics.put(IConstants.DURATION, Long.toString(duration));
		results.add(statistics);
	}

}