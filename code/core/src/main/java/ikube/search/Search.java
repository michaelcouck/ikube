package ikube.search;

import ikube.IConstants;

import ikube.action.index.IndexManager;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.search.spelling.SpellingChecker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;

/**
 * This action does the actual search on the index. The searcher that is current in the Instance is passed to this action. The search is done on the index. The
 * results are then processed for use in the front end. A list of maps is generated from the results. There are three standard fields in each map. Each map then
 * represents one record or category from the search. The three standard items in the map are the index in the lucene category set, id of the record and the
 * score that the category got. Optionally the fragment generated from the category if this is specified.
 * 
 * The id of the record generated using the name of the object indexed and the primary field in the database.
 * 
 * For paging functionality the search method can be called specifying the start and end parameters which will give logical paging. Although the search will be
 * done for each page forward the search is so fast that this is not relevant.
 * 
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
public abstract class Search {

	public enum TypeField {

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

	/** The default analyzer. */
	protected static final Analyzer ANALYZER = new StemmingAnalyzer();

	protected Logger logger;
	/** The searcher that will be used for the search. */
	protected transient IndexSearcher searcher;

	/** The search string that we are looking for. */
	protected transient String[] searchStrings;
	/** The fields in index to add to the search. */
	protected transient String[] searchFields;
	/** The fields to sort the results by. */
	protected transient String[] sortFields;
	/** The direction of sort for the sort fields. */
	protected transient String[] sortDirections;
	/** The types of fields for the search queries, like numeric etc. */
	protected transient String[] typeFields;
	/** The types of fields for the search queries, like numeric etc. */
	protected transient String[] occurrenceFields;

	/** Whether to generate fragments for the search string or not. */
	protected transient boolean fragment;
	/** The start position in the search results to return maps from. */
	protected transient int firstResult;
	/** The end position in the results to stop returning results from. */
	protected transient int maxResults;

	protected transient Analyzer analyzer;

	Search(final IndexSearcher searcher) {
		this(searcher, ANALYZER);
	}

	Search(final IndexSearcher searcher, final Analyzer analyzer) {
		this.logger = Logger.getLogger(this.getClass());
		this.searcher = searcher;
		this.analyzer = analyzer;
	}

	/**
	 * Takes a category from the Lucene search query and selects the fragments that have the search word(s) in it, taking only the first few instances of the
	 * data where the term appears and returns the fragments. For example in the document the data is the following
	 * "The quick brown fox jumps over the lazy dog" and we search for 'quick', 'fox' and 'lazy'. The category will be '...The quick brown fox jumps...the lazy
	 * dog...'.<br>
	 * <br>
	 * The fragments are from the current document, so calling get next document will move the document to the next on in the Hits object.
	 * 
	 * @param document to get the fragments from
	 * @param fieldName the name of the field that was searched
	 * @param query that generated the results
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
	 * Sets the strings that will be searched for.
	 * 
	 * @param searchStrings the search strings
	 */
	public void setSearchStrings(final String... searchStrings) {
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
	 * @param searchFields the fields in the index to search through
	 */
	public void setSearchFields(final String... searchFields) {
		this.searchFields = searchFields;
	}

	/**
	 * Sets the fields that will be used to sort the results by Lucene.
	 * 
	 * @param sortFields the fields to sort with in the index
	 */
	public void setSortField(final String... sortFields) {
		this.sortFields = sortFields;
	}

	/**
	 * Sets the direction of sort for the sort fields.
	 * 
	 * @param sortDirections the direction of sorting for the sort fields
	 */
	public void setSortDirections(String... sortDirections) {
		this.sortDirections = sortDirections;
	}

	/**
	 * Sets the types of fields that will be used in the search like numeric etc.
	 * 
	 * @param typeFields the types of fields that map to the search strings and the field names
	 */
	public void setTypeFields(final String... typeFields) {
		this.typeFields = typeFields;
	}

	/**
	 * Sets whether the fields should contain these terms, or should not, or lenient, i.e. can contains these terms.
	 * 
	 * @param occurrenceFields the occurrence for the field(s)
	 */
	public void setOccurrenceFields(String... occurrenceFields) {
		this.occurrenceFields = occurrenceFields;
	}

	/**
	 * This executed the search with the parameters set for the search fields and the search strings.
	 * 
	 * @return the results which are a list of maps. Each map has the fields in it if they are strings, not readers, and the map entries for index, score,
	 *         fragment, total and duration
	 */
	public ArrayList<HashMap<String, String>> execute() {
		long totalHits = 0;
		float highScore = 0;
		long start = System.currentTimeMillis();
		ArrayList<HashMap<String, String>> results = null;
		Exception exception = null;
		try {
			Query query = getQuery();
			TopDocs topDocs = search(query);
			totalHits = topDocs.totalHits;
			highScore = topDocs.getMaxScore();
			results = getResults(topDocs, query);
		} catch (IllegalArgumentException e) {
			// Do nothing this is something weird in TopScoreDocCollector
			exception = e;
		} catch (Exception e) {
			exception = e;
		} finally {
			if (exception != null) {
				String searchString = searchStrings != null && searchStrings.length > 0 ? searchStrings[0] : "null";
				logger.error("Exception searching for string " + searchString + " in searcher " + searcher, exception);
			}
			long duration = System.currentTimeMillis() - start;
			// Add the search results size as a last category
			addStatistics(searchStrings, results, totalHits, highScore, duration, exception);
		}
		return results;
	}

	/**
	 * Access to the query. This can be any number of query types, defined by the sub classes.
	 * 
	 * @return the Lucene query based on the parameters passed to the sub classes, like a span query etc.
	 */
	public abstract Query getQuery() throws Exception;

	/**
	 * Does the actual search on the Lucene index.
	 * 
	 * @param query the query to execute against the index
	 * @return the top documents from the search
	 * @throws IOException
	 */
	protected abstract TopDocs search(Query query) throws IOException;

	/**
	 * Sets whether the fragment made of the best part of the document should be included in the search results.
	 * 
	 * @param fragment the flag for generating the best fragments in the results
	 */
	public void setFragment(final boolean fragment) {
		this.fragment = fragment;
	}

	/**
	 * Sets the first category in the index.
	 * 
	 * @param start the first category to return from the results
	 */
	public void setFirstResult(final int start) {
		this.firstResult = start;
	}

	/**
	 * Sets the maximum results to return.
	 * 
	 * @param maxResults the maximum results to return
	 */
	public void setMaxResults(final int maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * Access to the query parsers for a particular field in the documents.
	 * 
	 * @param searchField the name of the field that needs to be searched
	 * @return the query parser for the particular field
	 */
	protected QueryParser getQueryParser(final String searchField) {
		return new QueryParser(IConstants.LUCENE_VERSION, searchField, analyzer);
	}

	/**
	 * Builds the list of results(a list of maps) from the top documents returned from Lucene.
	 * 
	 * @param topDocs the top documents from the Lucene search
	 * @param query the query that was used for the query
	 * @return the list of results from the search
	 * @throws IOException
	 */
	protected ArrayList<HashMap<String, String>> getResults(final TopDocs topDocs, final Query query) throws IOException {
		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		long totalHits = topDocs.totalHits;
		long scoreHits = topDocs.scoreDocs.length;
		for (int i = 0; i < totalHits && i < scoreHits; i++) {
			if (i < firstResult) {
				continue;
			}
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
		}
		return results;
	}

	/**
	 * Adds the fields to the results.
	 * 
	 * @param document the document to get the fields from to add to the category
	 * @param result map to add the field values to
	 */
	protected void addFieldsToResults(final Document document, final HashMap<String, String> result) {
		for (final IndexableField field : document.getFields()) {
			String fieldName = field.name();
			// Don't add the latitude and longitude tier field, very ugly data, and not useful
			if (fieldName != null && ((fieldName.equals(IConstants.LAT) || fieldName.equals(IConstants.LNG)) || (fieldName.contains(IConstants.TIER)))) {
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
	 * Adds the time it took for the search and adds the spelling corrected strings.
	 * 
	 * @param results the total number of results
	 * @param totalHits the total hits
	 * @param duration how long the search took in milliseconds
	 */
	protected void addStatistics(final String[] searchStrings, final ArrayList<HashMap<String, String>> results, final long totalHits, final float highScore,
			final long duration, final Exception exception) {
		if (results == null) {
			return;
		}
		// Add the search results size as a last category
		HashMap<String, String> statistics = new HashMap<String, String>();
		String[] correctedSearchStrings = getCorrections(searchStrings);
		String searchString = StringUtils.strip(Arrays.deepToString(searchStrings), IConstants.STRIP_CHARACTERS);
		String correctedSearchString = StringUtils.strip(Arrays.deepToString(correctedSearchStrings), IConstants.STRIP_CHARACTERS);

		statistics.put(IConstants.TOTAL, Long.toString(totalHits));
		statistics.put(IConstants.DURATION, Long.toString(duration));
		statistics.put(IConstants.SCORE, Float.toString(highScore));
		statistics.put(IConstants.SEARCH_STRINGS, searchString);
		statistics.put(IConstants.CORRECTIONS, correctedSearchString);

		if (exception != null) {
			OutputStream outputStream = new ByteArrayOutputStream();
			exception.printStackTrace(new PrintWriter(outputStream));
			statistics.put(IConstants.EXCEPTION, exception.getMessage());
			statistics.put(IConstants.EXCEPTION_STACK, outputStream.toString());
			statistics.put(IConstants.EXCEPTION_MESSAGE, exception.getMessage());
		}
		results.add(statistics);
	}

	/**
	 * This method will return an array of strings that have been corrected using the spelling and words defined in the languages directory. If there are no
	 * corrections, i.e. the strings are all correct then this array will be empty.
	 * 
	 * @param searchStrings the array of search strings and terms that are to be corrected for spelling
	 * @return the array of strings that have been corrected using the language and word lists in the language directory
	 */
	protected String[] getCorrections(final String... searchStrings) {
		boolean corrections = Boolean.FALSE;
		String[] correctedSearchStrings = new String[searchStrings.length];
		System.arraycopy(searchStrings, 0, correctedSearchStrings, 0, correctedSearchStrings.length);
		SpellingChecker spellingChecker = SpellingChecker.getSpellingChecker();
		for (int i = 0; i < correctedSearchStrings.length; i++) {
			if (StringUtils.isEmpty(correctedSearchStrings[i])) {
				continue;
			}
			String searchString = StringUtils.strip(correctedSearchStrings[i], IConstants.STRIP_CHARACTERS);
			// Break the search string up into tokens to check individually
			String[] searchStringTokens = StringUtils.split(searchString, " ");
			for (final String searchStringToken : searchStringTokens) {
				String correctedSearchStringToken = spellingChecker.checkWord(searchStringToken.toLowerCase());
				if (correctedSearchStringToken != null) {
					// Replace the incorrect token in the original string
					correctedSearchStrings[i] = StringUtils.replace(correctedSearchStrings[i], searchStringToken, correctedSearchStringToken);
					corrections = Boolean.TRUE;
				}
			}
		}
		if (corrections) {
			return correctedSearchStrings;
		}
		return new String[0];
	}

	/**
	 * This method will get all the fields in the index from the readers in the searcher and return them as a string array.
	 * 
	 * @param searcher the searcher to get all the fields for
	 * @return all the fields in the searcher that are searchable
	 */
	protected String[] getFields(final IndexSearcher searcher) {
		Collection<String> searchFieldNames = IndexManager.getFieldNames(searcher);
		return searchFieldNames.toArray(new String[searchFieldNames.size()]);
	}

	/**
	 * Returns a sort object that together with a filter will sort the results according to the specified fields.
	 * 
	 * @param query the query to wrap in the filter for the sort
	 * @return the sort that can be used together with the filter to sort the results based on the specified fields
	 */
	protected Sort getSort(final Query query) {
		if (sortFields == null || sortFields.length == 0 || sortDirections == null) {
			return null;
		}
		Sort sort = new Sort();
		SortField[] fields = new SortField[sortFields.length];
		for (int i = 0; i < sortFields.length; i++) {
			String sortFieldName = sortFields[i];
			SortField sortField = null;
			if (sortDirections == null || sortDirections.length <= i) {
				sortField = new SortField(sortFieldName, SortField.Type.STRING);
			} else {
				sortField = new SortField(sortFieldName, SortField.Type.STRING, Boolean.parseBoolean(sortDirections[i]));
			}
			fields[i] = sortField;
		}
		sort.setSort(fields);
		return sort;
	}

}