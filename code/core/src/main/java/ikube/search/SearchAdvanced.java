package ikube.search;

import ikube.IConstants;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;

/**
 * This is the complex search for combinations of queries, like all of these words and none of these. There are four categories/strings. The first is all the
 * words in the string. The second is an exact phrase. The third is any of the following words and the fourth is none of the words in the string. The result of
 * this query would be something like:
 * 
 * <pre>
 * 		Data:
 * 		{ "cape town university", "cape university town caucation", "cape one", "cape town", "cape town university one" }
 * 		
 * 		Query:
 * 		(cape AND town AND university) AND ("cape town") AND (one OR two OR three) NOT (caucasian)
 * </pre>
 * 
 * In a data set where the content is as in the data above, there will be one result. But in fact in the test there are two results, something in Lucene
 * perhaps? Perhaps my logic is not working correctly?
 * 
 * @see Search
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchAdvanced extends Search {

	public SearchAdvanced(final Searcher searcher) {
		this(searcher, IConstants.ANALYZER);
	}

	public SearchAdvanced(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(final Query query) throws IOException {
		return searcher.search(query, firstResult + maxResults);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query getQuery() throws ParseException {
		BooleanQuery booleanQuery = new BooleanQuery();

		if (searchStrings[0] != null) {
			Query query = getQueryParser(searchFields[0]).parse(searchStrings[0]);
			booleanQuery.add(query, BooleanClause.Occur.MUST);
		}

		if (searchStrings[1] != null) {
			Set<Term> phraseTerms = new HashSet<Term>();
			PhraseQuery phraseQuery = new PhraseQuery();
			String[] phraseStrings = StringUtils.split(searchStrings[1], " ");
			for (final String phraseString : phraseStrings) {
				phraseTerms.add(new Term(searchFields[1], phraseString));
			}
			phraseQuery.extractTerms(phraseTerms);
			booleanQuery.add(phraseQuery, BooleanClause.Occur.MUST);
		}

		if (searchStrings[2] != null) {
			String[] atLeastOneOfTheseStrings = StringUtils.split(searchStrings[2], " ");
			for (final String atLeastOneOfTheseString : atLeastOneOfTheseStrings) {
				Query query = getQueryParser(searchFields[2]).parse(atLeastOneOfTheseString);
				booleanQuery.add(query, BooleanClause.Occur.SHOULD);
			}
		}

		if (searchStrings[3] != null) {
			String[] noneOfTheseStrings = StringUtils.split(searchStrings[2], " ");
			for (final String atLeastOneOfTheseString : noneOfTheseStrings) {
				Query query = getQueryParser(searchFields[3]).parse(atLeastOneOfTheseString);
				booleanQuery.add(query, BooleanClause.Occur.MUST_NOT);
			}
		}

		return booleanQuery;
	}

}