package ikube.search.spelling;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * This class will index the text files with the words form various languages in them, and check tokens or words against the index of words.
 * To add languages the logic of this class has to change a little, the word file needs to become words files, and iterate over the array of
 * files rather than the first one.
 * 
 * @author Michael Couck
 * @since 05.03.10
 * @version 01.00
 */
public class SpellingChecker {

	private static SpellingChecker INSTANCE;

	private static final Logger LOGGER = LoggerFactory.getLogger(SpellingChecker.class);

	/** The 'real' spelling checker from Lucene. */
	private SpellChecker spellChecker;

	/** The path to the word files in different languages. */
	@Value("${language.word.lists.directory}")
	private String languageWordListsDirectory = "./ikube/common/languages";

	/** The path to the index that is created from the word files. */
	@Value("${language.word.lists.directory.index}")
	private String spellingIndexDirectoryPath = "./ikube/common/languages/index";

	/**
	 * Static access to the system spelling checker.
	 * 
	 * @return the spelling checker
	 */
	public static SpellingChecker getSpellingChecker() {
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
		File spellingIndexDirectory = FileUtilities.getFile(spellingIndexDirectoryPath, Boolean.TRUE);
		LOGGER.info("Spelling directory : " + spellingIndexDirectory + ", " + spellingIndexDirectoryPath);
		Directory directory = FSDirectory.open(spellingIndexDirectory);
		spellChecker = new SpellChecker(directory);
		indexLanguageFiles();
		LOGGER.info("Opened spelling index on : " + spellingIndexDirectory);
	}

	/**
	 * Indexes all the language files it finds in the language directory.
	 */
	private void indexLanguageFiles() {
		File wordListDirectory = new File(languageWordListsDirectory);
		LOGGER.info("Word list directory : " + languageWordListsDirectory + ", " + wordListDirectory);
		List<File> languageDictionaryFiles = FileUtilities.findFilesRecursively(wordListDirectory, new ArrayList<File>(), "txt");
		LOGGER.info("Language files : " + languageDictionaryFiles);
		for (File languageDictionaryFile : languageDictionaryFiles) {
			InputStream inputStream = null;
			try {
				LOGGER.info("Language file : " + languageDictionaryFile);
				inputStream = new FileInputStream(languageDictionaryFile);
				LOGGER.info("Input stream : " + inputStream);
				IndexWriterConfig indexWriterConfig = new IndexWriterConfig(IConstants.VERSION, IConstants.ANALYZER);
				spellChecker.indexDictionary(new PlainTextDictionary(inputStream), indexWriterConfig, Boolean.TRUE);
			} catch (Exception e) {
				LOGGER.error("Exception indexing language file : " + languageDictionaryFile, e);
			} finally {
				FileUtilities.close(inputStream);
			}
		}
	}

	/**
	 * This method will check one word against all the words in the language index and return the best suggestion for correction.
	 * 
	 * @param word the word to check against all the words in the language files
	 * @return the first corrected spelling suggestion, probably based on a Levinshtein distance
	 */
	public String checkWord(String word) {
		try {
			if (!spellChecker.exist(word)) {
				String[] searchStringCorrection = spellChecker.suggestSimilar(word, 1);
				if (searchStringCorrection != null && searchStringCorrection.length > 0) {
					return searchStringCorrection[0];
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	 * Closes the spelling checker, releasing file system resources.
	 */
	public void destroy() {
		if (this.spellChecker != null) {
			try {
				this.spellChecker.close();
			} catch (Exception e) {
				LOGGER.error("Exception closing the spelling checker : ", e);
			}
		}
	}
}