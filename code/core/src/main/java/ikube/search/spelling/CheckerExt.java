package ikube.search.spelling;

import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This class will index the text files with the words form various languages in them, and check tokens or words against
 * the index of words. To add languages the logic of this class has to change a little, the word file needs to become
 * words files, and iterate over the array of files rather than the first one.
 * 
 * @author Michael Couck
 * @since 05.03.10
 * @version 01.00
 */
public class CheckerExt {

	private static final Logger		LOGGER					= Logger.getLogger(CheckerExt.class);

	private static final String		WORD_FILE_DIRECTORY		= "/META-INF/spelling/";
	private static final String[]	WORD_FILES				= { "english.txt", "french.txt", "german.txt" };
	private static final String		WORD_INDEX_DIRECTORY	= "./spellingIndex";
	private static SpellChecker		SPELL_CHECKER;
	private static CheckerExt		CHECKER_EXT				= new CheckerExt();

	public static CheckerExt getCheckerExt() {
		return CHECKER_EXT;
	}

	private CheckerExt() {
		try {
			openDictionary();
		} catch (Exception e) {
			LOGGER.error("Exception opening the spelling index", e);
		}
	}

	private void openDictionary() throws Exception {
		if (SPELL_CHECKER != null) {
			return;
		}
		File spellingIndexDirectory = FileUtilities.getFile(WORD_INDEX_DIRECTORY, Boolean.TRUE);
		Directory directory = FSDirectory.open(spellingIndexDirectory);
		boolean mustIndex = true;
		if (IndexReader.indexExists(directory)) {
			String[] files = directory.listAll();
			mustIndex = files.length < 3;
			if (mustIndex) {
				for (String file : files) {
					directory.deleteFile(file);
				}
			}
		}
		SPELL_CHECKER = new SpellChecker(directory);
		if (mustIndex) {
			// Iterate over the spelling index directory and collect up all the files there too
			List<File> wordFiles = FileUtilities.findFilesRecursively(spellingIndexDirectory, new ArrayList<File>(), ".txt");
			for (File wordFile : wordFiles) {
				InputStream inputStream = null;
				try {
					inputStream = new FileInputStream(wordFile);
					SPELL_CHECKER.indexDictionary(new PlainTextDictionary(inputStream));
				} catch (Exception e) {
					LOGGER.error("Exception indexing the word file : " + wordFile, e);
				} finally {
					FileUtilities.close(inputStream);
				}
			}
			for (String wordFile : WORD_FILES) {
				InputStream inputStream = null;
				try {
					inputStream = CheckerExt.class.getResourceAsStream(WORD_FILE_DIRECTORY + wordFile);
					LOGGER.info("Word file : " + wordFile + ", input stream : " + inputStream);
					if (inputStream != null) {
						SPELL_CHECKER.indexDictionary(new PlainTextDictionary(inputStream));
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				} finally {
					FileUtilities.close(inputStream);
				}
			}
		}
		LOGGER.info("Opened spelling index on : " + spellingIndexDirectory.getAbsolutePath());
	}

	/**
	 * This method will check the strings in the search string by breaking them up into tokens and checking them against
	 * the spelling index. If there are no matches for some tokens, then the best match will be added to the result. If
	 * there are no spelling errors then this method will return null, indicating that there were no corrections to be
	 * made.
	 * 
	 * @param searchString
	 *            the search string with typically words in it
	 * @return the tokens in the original search string with the mis-spelled tokens corrected with the best result, or
	 *         null if there were no spelling errors
	 */
	public String checkWords(String searchString) {
		StringTokenizer tokenizer = new StringTokenizer(searchString);
		StringBuilder correctWords = new StringBuilder();
		boolean hasCorrections = false;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			String[] strings;
			try {
				if (SPELL_CHECKER.exist(token)) {
					correctWords.append(token);
					if (tokenizer.hasMoreTokens()) {
						correctWords.append(" ");
					}
					continue;
				}
				hasCorrections = true;
				strings = SPELL_CHECKER.suggestSimilar(token, 1);
			} catch (IOException e) {
				LOGGER.error("Exception checking spelling for : " + token, e);
				continue;
			}
			if (strings != null && strings.length > 0) {
				correctWords.append(strings[0]);
				if (tokenizer.hasMoreTokens()) {
					correctWords.append(" ");
				}
			}
		}
		if (hasCorrections) {
			return correctWords.toString();
		}
		return null;
	}
}