package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

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
@NamedQueries(value = {
		@NamedQuery(name = Search.SELECT_FROM_SEARCH_BY_SEARCH_STRINGS_LIKE, query = Search.SELECT_FROM_SEARCH_BY_SEARCH_STRINGS_LIKE),
		@NamedQuery(name = Search.SELECT_FROM_SEARCH_BY_INDEX_NAME_AND_SEARCH_STRINGS, query = Search.SELECT_FROM_SEARCH_BY_INDEX_NAME_AND_SEARCH_STRINGS) })
public class Search extends Persistable {

	public static final String SELECT_FROM_SEARCH_BY_SEARCH_STRINGS_LIKE = //
	"select s from Search as s where s.searchStrings like :searchStrings order by s.count desc";
	public static final String SELECT_FROM_SEARCH_BY_INDEX_NAME_AND_SEARCH_STRINGS = //
	"select s from Search as s where s.indexName = :indexName and s.searchStrings = :searchStrings";

	@Column()
	private int count;
	@Column()
	private int results;
	@Column()
	private String indexName;
	@Column()
	private String searchStrings;
	@Column()
	private double highScore;
	@Column()
	private boolean corrections;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getResults() {
		return results;
	}

	public void setResults(int results) {
		this.results = results;
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

}
