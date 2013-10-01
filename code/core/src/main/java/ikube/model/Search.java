package ikube.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

/**
 * This entity represents a search that was done by the user. As searches are done they can be inserted into the database and the data can then be used for all
 * sorts of things like statistical calculations and auto complete.
 * 
 * @author Michael Couck
 * @since 01.03.12
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EntityListeners(value = { SearchIncrementListener.class })
@NamedQueries(value = { @NamedQuery(name = Search.SELECT_FROM_SEARCH_BY_SEARCH_STRINGS_LIKE, query = Search.SELECT_FROM_SEARCH_BY_SEARCH_STRINGS_LIKE),
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
	private String indexName;
	@Column
	private boolean fragment;
	@Column
	private int firstResult;
	@Column
	private int maxResults;

	@Column
	private int count;
	@Column
	private int totalResults;
	@Column
	private double highScore;
	@Column
	private boolean corrections;
	@Column
	private long hash;

	@Column
	@ElementCollection
	private String[] searchStrings;
	@Column
	@ElementCollection
	private String[] searchFields;
	@Column
	@ElementCollection
	private String[] sortFields;
	@Column
	@ElementCollection
	private String[] typeFields;

	@Column
	@ElementCollection
	private String[] correctedSearchStrings;

	@Transient
	private ArrayList<HashMap<String, String>> searchResults;

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public boolean isFragment() {
		return fragment;
	}

	public void setFragment(boolean fragment) {
		this.fragment = fragment;
	}

	public int getFirstResult() {
		return firstResult;
	}

	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
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

	public long getHash() {
		return hash;
	}

	public void setHash(long hash) {
		this.hash = hash;
	}

	public String[] getSearchStrings() {
		return searchStrings;
	}

	public void setSearchStrings(String[] searchStrings) {
		this.searchStrings = searchStrings;
	}

	public String[] getSearchFields() {
		return searchFields;
	}

	public void setSearchFields(String[] searchFields) {
		this.searchFields = searchFields;
	}

	public String[] getSortFields() {
		return sortFields;
	}

	public void setSortFields(String[] sortFields) {
		this.sortFields = sortFields;
	}

	public String[] getTypeFields() {
		return typeFields;
	}

	public void setTypeFields(String[] typeFields) {
		this.typeFields = typeFields;
	}

	public String[] getCorrectedSearchStrings() {
		return correctedSearchStrings;
	}

	public void setCorrectedSearchStrings(String[] correctedSearchStrings) {
		this.correctedSearchStrings = correctedSearchStrings;
	}

	public ArrayList<HashMap<String, String>> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(ArrayList<HashMap<String, String>> searchResults) {
		this.searchResults = searchResults;
	}

}