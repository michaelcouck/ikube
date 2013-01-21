package ikube.search;

import ikube.IConstants;
import ikube.search.spelling.SpellingChecker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.util.ReaderUtil;

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
@SuppressWarnings("deprecation")
public abstract class Search {

	enum TypeField {

		STRING("string"), //
		NUMERIC("numeric"), //
		RANGE("range");

		String fieldType;

		TypeField(final String fieldType) {
			this.fieldType = fieldType;
		}

		public String fieldType() {
			return this.fieldType;
		}
	}

	@SuppressWarnings("unused")
	private static transient Map<String, QueryParser> QUERY_PARSERS = new HashMap<String, QueryParser>();

	protected Logger logger;
	/** The searcher that will be used for the search. */
	protected transient Searcher searcher;
	/** The query parsers for various query fields. */

	/** The search string that we are looking for. */
	protected transient String[] searchStrings;
	/** The fields in index to add to the search. */
	protected transient String[] searchFields;
	/** The fields to sort the results by. */
	protected transient String[] sortFields;
	/** The types of fields for the search queries, like numeric etc. */
	protected transient String[] typeFields;

	/** Whether to generate fragments for the search string or not. */
	protected transient boolean fragment;
	/** The start position in the search results to return maps from. */
	protected transient int firstResult;
	/** The end position in the results to stop returning results from. */
	protected transient int maxResults;

	protected transient Analyzer analyzer;

	Search(final Searcher searcher) {
		this(searcher, IConstants.ANALYZER);
	}

	Search(final Searcher searcher, final Analyzer analyzer) {
		this.logger = Logger.getLogger(this.getClass());
		this.searcher = searcher;
		this.analyzer = analyzer;
	}

	/**
	 * Takes a result from the Lucene search query and selects the fragments that have the search word(s) in it, taking only the first few
	 * instances of the data where the term appears and returns the fragments. For example in the document the data is the following
	 * "The quick brown fox jumps over the lazy dog" and we search for 'quick', 'fox' and 'lazy'. The result will be '...The quick brown fox
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
			TokenStream tokenStream = analyzer.tokenStream(fieldName, stringReader);
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
	protected void addFieldsToResults(final Document document, final HashMap<String, String> result) throws Exception {
		for (Fieldable field : document.getFields()) {
			String fieldName = field.name();
			// Don't add the latitude and longitude tier field, very ugly data, and not useful
			if (fieldName != null
					&& ((fieldName.equals(IConstants.LAT) || fieldName.equals(IConstants.LNG)) || (fieldName.contains(IConstants.TIER)))) {
				continue;
			}
			String stringValue = field.stringValue();
			if (!StringUtils.isEmpty(stringValue)) {
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
		if (searchStrings != null) {
			// Strip all the characters that Lucene doesn't like
			for (int i = 0; i < searchStrings.length; i++) {
				searchStrings[i] = StringUtils.strip(searchStrings[i], IConstants.STRIP_CHARACTERS);
			}
		}
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

	/**
	 * Sets the fields that will be used to sort the results by Lucene.
	 * 
	 * @param sortFields
	 *            the fields to sort with in the index
	 */
	public void setSortField(final String... sortFields) {
		this.sortFields = sortFields;
	}

	/**
	 * Sets the types of fields that will be used in the search like numeric etc.
	 * 
	 * @param typeFields
	 *            the types of fields that map to the search strings and the field names
	 */
	public void setTypeFields(final String... typeFields) {
		this.typeFields = typeFields;
	}

	/**
	 * This executed the search with the parameters set for the search fields and the search strings.
	 * 
	 * @return the results which are a list of maps. Each map has the fields in it if they are strings, not readers, and the map entries for
	 *         index, score, fragment, total and duration
	 */
	public ArrayList<HashMap<String, String>> execute() {
		if (searcher == null) {
			logger.warn("No searcher on any index, is an index created?");
		}
		long totalHits = 0;
		long start = System.currentTimeMillis();
		ArrayList<HashMap<String, String>> results = null;
		Exception exception = null;
		try {
			Query query = getQuery();
			TopDocs topDocs = search(query);
			totalHits = topDocs.totalHits;
			// TODO If there are no results here then do a search for the
			// corrected spelling to see if there are any results from that
			results = getResults(topDocs, query);
		} catch (IllegalArgumentException e) {
			// Do nothing this is something weird in TopScoreDocCollector
			exception = e;
		} catch (Exception e) {
			exception = e;
			String searchString = searchStrings != null && searchStrings.length > 0 ? searchStrings[0] : "null";
			logger.error("Exception searching for string " + searchString + " in searcher " + searcher, e);
		}
		if (results == null) {
			results = new ArrayList<HashMap<String, String>>();
		}
		long duration = System.currentTimeMillis() - start;
		// Add the search results size as a last result
		addStatistics(results, totalHits, duration, exception);
		return results;
	}

	/**
	 * Access to the query. This can be any number of query types, defined by the sub classes.
	 * 
	 * @return the Lucene query based on the parameters passed to the sub classes, like a span query etc.
	 * @throws ParseException
	 */
	public abstract Query getQuery() throws ParseException;

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
		return new QueryParser(IConstants.VERSION, searchField, analyzer);
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
	protected ArrayList<HashMap<String, String>> getResults(final TopDocs topDocs, final Query query) {
		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		long totalHits = topDocs.totalHits;
		long scoreHits = topDocs.scoreDocs.length;
		for (int i = 0; i < totalHits && i < scoreHits; i++) {
			if (i < firstResult) {
				continue;
			}
			try {
				HashMap<String, String> result = new HashMap<String, String>();
				Document document = searcher.doc(topDocs.scoreDocs[i].doc);
				float score = topDocs.scoreDocs[i].score;
				String index = Integer.toString(topDocs.scoreDocs[i].doc);
				result.put(IConstants.INDEX, index);
				result.put(IConstants.SCORE, Float.toString(score));
				addFieldsToResults(document, result);
				if (fragment) {
					StringBuilder builder = new StringBuilder();
					int fragments = 0;
					for (final String searchField : searchFields) {
						String fragment = getFragments(document, searchField, query);
						if (fragment == null || "".equals(fragment.trim())) {
							continue;
						}
						builder.append(fragment);
						builder.append(' ');
						fragments++;
						if (fragments >= IConstants.MAX_FRAGMENTS) {
							break;
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
	 * Adds the time it took for the search and adds the spelling corrected strings.
	 * 
	 * @param results
	 *            the total number of results
	 * @param totalHits
	 *            the total hits
	 * @param duration
	 *            how long the search took in milliseconds
	 */
	protected void addStatistics(final ArrayList<HashMap<String, String>> results, final long totalHits, final long duration,
			final Exception exception) {
		// Add the search results size as a last result
		HashMap<String, String> statistics = new HashMap<String, String>();
		statistics.put(IConstants.TOTAL, Long.toString(totalHits));
		statistics.put(IConstants.DURATION, Long.toString(duration));

		String[] correctedSearchStrings = getCorrections();

		String searchString = StringUtils.strip(Arrays.deepToString(searchStrings), IConstants.STRIP_CHARACTERS);
		String correctedSearchString = StringUtils.strip(Arrays.deepToString(correctedSearchStrings), IConstants.STRIP_CHARACTERS);
		statistics.put(IConstants.SEARCH_STRINGS, searchString);
		statistics.put(IConstants.CORRECTIONS, correctedSearchString);

		if (exception != null) {
			statistics.put(IConstants.EXCEPTION, exception.getMessage());
			OutputStream outputStream = new ByteArrayOutputStream();
			exception.printStackTrace(new PrintWriter(outputStream));
			statistics.put(IConstants.EXCEPTION_STACK, outputStream.toString());
			statistics.put(IConstants.EXCEPTION_MESSAGE, exception.getMessage());
		}

		results.add(statistics);
	}

	/**
	 * This method will return an array of strings that have been corrected using the spelling and words defined in the languages directory.
	 * If there are no corrections, i.e. the strings are all correct then this array will be empty.
	 * 
	 * @return the array of strings that have been corrected using the language and word lists in the language directory
	 */
	protected String[] getCorrections() {
		boolean corrections = Boolean.FALSE;
		Set<String> correctedSearchStrings = new TreeSet<String>();
		SpellingChecker spellingChecker = SpellingChecker.getSpellingChecker();
		for (int i = 0; i < searchStrings.length; i++) {
			String searchString = StringUtils.strip(searchStrings[i], IConstants.STRIP_CHARACTERS);
			if (searchString == null) {
				continue;
			}
			String correctedSearchString = spellingChecker.checkWords(searchString.toLowerCase());
			if (correctedSearchString != null) {
				corrections = Boolean.TRUE;
				correctedSearchStrings.add(correctedSearchString);
			} else {
				correctedSearchStrings.add(searchString);
			}
		}
		if (corrections) {
			return correctedSearchStrings.toArray(new String[correctedSearchStrings.size()]);
		}
		return new String[0];
	}

	/**
	 * This method will get all the fields in the index from the readers in the searcher and return them as a string array.
	 * 
	 * @param searcher
	 *            the searcher to get all the fields for
	 * @return all the fields in the searcher that are searchable
	 */
	protected String[] getFields(final Searcher searcher) {
		Searchable[] searchables = ((MultiSearcher) searcher).getSearchables();
		Set<String> searchFieldNames = new TreeSet<String>();
		for (Searchable searchable : searchables) {
			IndexReader indexReader = ((IndexSearcher) searchable).getIndexReader();
			FieldInfos fieldInfos = null;
			try {
				fieldInfos = ReaderUtil.getMergedFieldInfos(indexReader);
			} catch (NullPointerException e) {
				logger.warn("Null pointer : ");
				logger.debug(null, e);
			}
			if (fieldInfos != null) {
				Iterator<FieldInfo> iterator = fieldInfos.iterator();
				while (iterator.hasNext()) {
					FieldInfo fieldInfo = iterator.next();
					searchFieldNames.add(fieldInfo.name);
				}
			}
		}
		return searchFieldNames.toArray(new String[searchFieldNames.size()]);
	}

}