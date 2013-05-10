package ikube.search;

import ikube.IConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;

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
public class SearchAdvanced extends SearchSingle {

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
	public Query getQuery() throws ParseException {
		BooleanQuery booleanQuery = new BooleanQuery();

		int index = 0;
		if (checkRange(index) && !StringUtils.isEmpty(searchStrings[index])) {
			Query query = getQueryParser(searchFields[index]).parse(searchStrings[index]);
			booleanQuery.add(query, BooleanClause.Occur.MUST);
		}

		index++;
		if (checkRange(index) && !StringUtils.isEmpty(searchStrings[index])) {
			PhraseQuery phraseQuery = new PhraseQuery();
			String[] phraseStrings = StringUtils.split(searchStrings[index], " ");
			for (final String phraseString : phraseStrings) {
				Term phraseTerm = new Term(searchFields[index], phraseString);
				phraseQuery.add(phraseTerm);
			}
			booleanQuery.add(phraseQuery, BooleanClause.Occur.SHOULD);
		}

		index++;
		// TODO This must be reviewed
		if (checkRange(index) && !StringUtils.isEmpty(searchStrings[index])) {
			String[] atLeastOneOfTheseStrings = StringUtils.split(searchStrings[index], " ");
			for (final String atLeastOneOfTheseString : atLeastOneOfTheseStrings) {
				Query query = getQueryParser(searchFields[index]).parse(atLeastOneOfTheseString);
				booleanQuery.add(query, BooleanClause.Occur.SHOULD);
			}
		}

		index++;
		if (checkRange(index) && !StringUtils.isEmpty(searchStrings[index])) {
			String[] noneOfTheseStrings = StringUtils.split(searchStrings[index], " ");
			for (final String notThisString : noneOfTheseStrings) {
				Term term = new Term(searchFields[index], notThisString);
				TermQuery termQuery = new TermQuery(term);
				booleanQuery.add(termQuery, BooleanClause.Occur.MUST_NOT);
			}
		}

		return booleanQuery;
	}

	private boolean checkRange(final int index) {
		return searchFields != null && searchStrings != null && searchFields.length > index && searchStrings.length > index;
	}

}