package ikube.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

/**
 * This entity represents a search that was done by the user. As searches are done they can be inserted into the database and the data can
 * then be used for all sorts of things like statistical calculations and auto complete.
 * 
 * @author Michael Couck
 * @since 01.03.12
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EntityListeners(value = { SearchIncrementListener.class })
@NamedQueries(value = {
		@NamedQuery(name = Search.SELECT_FROM_SEARCH_BY_SEARCH_STRINGS_LIKE, query = Search.SELECT_FROM_SEARCH_BY_SEARCH_STRINGS_LIKE),
		@NamedQuery(name = Search.SELECT_FROM_SEARCH_BY_INDEX_NAME_AND_SEARCH_STRINGS, query = Search.SELECT_FROM_SEARCH_BY_INDEX_NAME_AND_SEARCH_STRINGS),
		@NamedQuery(name = Search.SELECT_FROM_SEARCH_BY_INDEX_NAME, query = Search.SELECT_FROM_SEARCH_BY_INDEX_NAME),
		@NamedQuery(name = Search.SELECT_FROM_SEARCH_COUNT_SEARCHES, query = Search.SELECT_FROM_SEARCH_COUNT_SEARCHES) })
public class Search extends Persistable {

	public static final String SELECT_FROM_SEARCH_BY_SEARCH_STRINGS_LIKE = //
	"select s from Search as s where s.searchStrings like :searchStrings order by s.count desc";
	public static final String SELECT_FROM_SEARCH_BY_INDEX_NAME_AND_SEARCH_STRINGS = //
	"select s from Search as s where s.indexName = :indexName and s.searchStrings = :searchStrings";
	public static final String SELECT_FROM_SEARCH_BY_INDEX_NAME = //
	"select s from Search as s where s.indexName = :indexName";
	public static final String SELECT_FROM_SEARCH_COUNT_SEARCHES = //
	"select sum(s.count) from Search as s where s.indexName = :indexName";

	@Column
	private int count;
	@Column
	private int totalResults;
	@Column
	private String indexName;
	@Column(length = 256)
	private String searchStrings;
	@Column(length = 256)
	private String correctedSearchStrings;
	@Column
	private double highScore;
	@Column
	private boolean corrections;
	@Transient
	private ArrayList<HashMap<String, String>> searchResults;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int results) {
		this.totalResults = results;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getSearchStrings() {
		return searchStrings;
	}

	public void setSearchStrings(String searchStrings) {
		this.searchStrings = searchStrings;
	}

	public String getCorrectedSearchStrings() {
		return correctedSearchStrings;
	}

	public void setCorrectedSearchStrings(String correctedSearchStrings) {
		this.correctedSearchStrings = correctedSearchStrings;
	}

	public double getHighScore() {
		return highScore;
	}

	public void setHighScore(double highScore) {
		this.highScore = highScore;
	}

	public boolean isCorrections() {
		return corrections;
	}

	public void setCorrections(boolean corrections) {
		this.corrections = corrections;
	}

	public ArrayList<HashMap<String, String>> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(ArrayList<HashMap<String, String>> searchResults) {
		this.searchResults = searchResults;
	}

}