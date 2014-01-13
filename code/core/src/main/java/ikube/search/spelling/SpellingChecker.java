package ikube.search.spelling;


import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.search.Search;
import ikube.search.SearchComplex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This class will index the text files with the words form various languages in them, and check tokens or words against the index of words. To add languages
 * the logic of this class has to change a little, the word file needs to become words files, and iterate over the array of files rather than the first one.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05.03.10
 */
public class SpellingChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpellingChecker.class);

	private static SpellingChecker INSTANCE;
	@Autowired
	@Qualifier(value = IConstants.AUTOCOMPLETE)
	private IndexContext<?> indexContext;

	/**
	 * Static access to the system spelling checker.
	 *
	 * @return the spelling checker
	 */
	public static SpellingChecker getSpellingChecker() {
		if (SpellingChecker.INSTANCE == null) {
			SpellingChecker.INSTANCE = new SpellingChecker();
			try {
				SpellingChecker.INSTANCE.initialize();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return SpellingChecker.INSTANCE;
	}

	/**
	 * The constructor sets the system wide statis spelling checker.
	 */
	public SpellingChecker() {
		SpellingChecker.INSTANCE = this;
	}

	/**
	 * Initializes the spelling checker, by indexing all the language files on each start.
	 *
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		LOGGER.info("Opened spelling index : ");
	}

	/**
	 * This method will check one word against all the words in the language index and return the best suggestion for correction.
	 *
	 * @param word the word to check against all the words in the language files
	 * @return the first corrected spelling suggestion, probably based on a Levinshtein distance
	 */
	public String checkWord(final String word) {
		try {
			if (indexContext.getMultiSearcher() != null) {
				Search search = new SearchComplex(indexContext.getMultiSearcher()) {
					protected void addStatistics(final String[] searchStrings, final ArrayList<HashMap<String, String>> results,
												 final long totalHits, final float highScore, final long duration, final Exception exception) {
						// Do nothing, we don't want a recursive loop in the search
					}
				};
				search.setOccurrenceFields(IConstants.SHOULD);
				search.setTypeFields(IConstants.STRING);
				search.setFirstResult(0);
				search.setFragment(Boolean.FALSE);
				search.setMaxResults(10);
				search.setSearchStrings(word);
				search.setSearchFields(IConstants.WORD);

				ArrayList<HashMap<String, String>> results = search.execute();
				if (results.size() > 0) {
					String firstMatch = results.get(0).get(IConstants.WORD);
					if (!word.equals(firstMatch)) {
						return firstMatch;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	 * Closes the spelling checker, releasing file system resources.
	 */
	public void destroy() {
		// Nothing to do really
	}
}