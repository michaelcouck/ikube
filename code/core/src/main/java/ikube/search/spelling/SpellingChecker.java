package ikube.search.spelling;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(SpellingChecker.class);
	private static SpellingChecker INSTANCE;

	private SpellChecker spellChecker;
	@Value("${language.word.lists.directory}")
	private String languageWordListsDirectory = "./ikube/common/languages";
	@Value("${language.word.lists.directory.index}")
	private String spellingIndexDirectoryPath = "./ikube/common/languages/index";

	public static final SpellingChecker getSpellingChecker() {
		return SpellingChecker.INSTANCE;
	}

	public SpellingChecker() {
		SpellingChecker.INSTANCE = this;
	}

	public void initialize() throws Exception {
		File spellingIndexDirectory = FileUtilities.getFile(spellingIndexDirectoryPath, Boolean.TRUE);
		Directory directory = FSDirectory.open(spellingIndexDirectory);
		spellChecker = new SpellChecker(directory);
		indexLanguageFiles();
		LOGGER.info("Opened spelling index on : " + spellingIndexDirectory.getAbsolutePath());
	}

	private void indexLanguageFiles() {
		List<File> languageDictionaryFiles = FileUtilities.findFilesRecursively(new File(languageWordListsDirectory),
				new ArrayList<File>(), "txt");
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
	 * This method will check the strings in the search string by breaking them up into tokens and checking them against the spelling index.
	 * If there are no matches for some tokens, then the best match will be added to the result. If there are no spelling errors then this
	 * method will return null, indicating that there were no corrections to be made.
	 * 
	 * @param searchString the search string with typically words in it
	 * @return the tokens in the original search string with the mis-spelled tokens corrected with the best result, or null if there were no
	 *         spelling errors
	 */
	public final String checkWords(String searchString) {
		boolean hasCorrections = Boolean.FALSE;
		StringTokenizer tokenizer = new StringTokenizer(searchString, ",;|.& ");
		StringBuilder correctWords = new StringBuilder();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			// Skip the Lucene specific conjunctions like 'and' and 'or'
			if (IConstants.LUCENE_CONJUNCTIONS_PATTERN.matcher(token.toLowerCase()).matches()) {
				correctWords.append(token);
				addSpace(tokenizer, correctWords);
				continue;
			}
			String[] strings;
			try {
				boolean exists = spellChecker.exist(token);
				if (exists) {
					correctWords.append(token);
					addSpace(tokenizer, correctWords);
					continue;
				}
				hasCorrections = true;
				strings = spellChecker.suggestSimilar(token, 1);
			} catch (IOException e) {
				LOGGER.error("Exception checking spelling for : " + token, e);
				continue;
			}
			if (strings != null && strings.length > 0) {
				correctWords.append(strings[0]);
				addSpace(tokenizer, correctWords);
			}
		}
		if (hasCorrections) {
			return correctWords.toString();
		}
		return null;
	}

	private void addSpace(final StringTokenizer tokenizer, final StringBuilder correctWords) {
		if (tokenizer.hasMoreTokens()) {
			correctWords.append(" ");
		}
	}

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