package ikube.search;

import ikube.IConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Michael Couck
 * @version 01.00
 * @see Search
 * @since 10.01.2012
 */
public class SearchComplex extends Search {

	public SearchComplex(final IndexSearcher searcher) {
		this(searcher, ANALYZER);
	}

	public SearchComplex(final IndexSearcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query getQuery() throws ParseException {
		if (typeFields == null || typeFields.length == 0 || searchStrings.length != typeFields.length ||
			occurrenceFields == null || occurrenceFields.length == 0 || occurrenceFields.length != typeFields.length) {
			if (searchStrings.length != searchFields.length) {
				String message = "Strings and fields must be the same length at least : " + Arrays.deepToString
					(searchStrings) + ", "
					+ Arrays.deepToString(searchFields);
				throw new RuntimeException(message);
			}
			logger.info("Using multi field query parser");
			return MultiFieldQueryParser.parse(IConstants.LUCENE_VERSION, searchStrings, searchFields, analyzer);
		}
		BooleanQuery booleanQuery = new BooleanQuery();
		for (int i = 0; i < searchStrings.length; i++) {
			final String typeField = typeFields[i];
			final String searchField = searchFields[i];
			final String searchString = searchStrings[i];
			final String occurrenceField = occurrenceFields[i];
			if (StringUtils.isEmpty(searchString)) {
				// Just ignore the empty strings
				continue;
			}
			Query query = null;
			if (TypeField.STRING.fieldType().equalsIgnoreCase(typeField)) {
				if (searchString.contains("~") || searchString.contains("*")) {
					query = new FuzzyQuery(new Term(searchField, searchString));
				} else {
					query = getQueryParser(searchField).parse(searchString);
				}
			} else {
				float min;
				float max;
				if (TypeField.NUMERIC.fieldType().equalsIgnoreCase(typeField)) {
					min = Float.parseFloat(searchString);
					max = Float.parseFloat(searchString);
				} else if (TypeField.RANGE.fieldType().equalsIgnoreCase(typeField)) {
					String[] values = StringUtils.split(searchString, '-');
					min = Float.parseFloat(values[0]);
					max = Float.parseFloat(values[1]);
				} else {
					String message = "Field must have a type to create the query : " + typeField + ", " +
						"field : " + searchField + ", string : " + searchString;
					throw new RuntimeException(message);
				}
				query = NumericRangeQuery.newFloatRange(searchField, min, max, Boolean.TRUE, Boolean.TRUE);
			}
			BooleanClause.Occur occurrence = BooleanClause.Occur.valueOf(occurrenceField.toUpperCase());
			booleanQuery.add(query, occurrence);
		}
		return booleanQuery;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(Query query) throws IOException {
		return searcher.search(query, firstResult + maxResults);
	}

}