package ikube.web.tag.spelling;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 05.03.10
 * @version 01.00
 */
public class CheckerExt {

	private static final String WORD_FILE = "/META-INF/words.txt";
	private static final String WORD_INDEX_DIRECTORY = "./spellingIndex";
	private static SpellChecker spell;

	private Logger logger = Logger.getLogger(CheckerExt.class);

	public CheckerExt() {
		try {
			openDictionary();
		} catch (Exception e) {
			logger.error("Exception opening the spelling index", e);
		}
	}

	private void openDictionary() throws Exception {
		if (spell != null) {
			return;
		}
		File spellingIndexDirectory = new File(WORD_INDEX_DIRECTORY);
		if (!spellingIndexDirectory.exists()) {
			spellingIndexDirectory.mkdirs();
			logger.info("Created spelling index at : " + spellingIndexDirectory.getAbsolutePath());
		}
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
		spell = new SpellChecker(directory);
		if (mustIndex) {
			InputStream inputStream = CheckerExt.class.getResourceAsStream(WORD_FILE);
			logger.info("Word file : " + WORD_FILE + ", input stream : " + inputStream);
			spell.indexDictionary(new PlainTextDictionary(inputStream));
		}
		logger.info("Opened spelling index on : " + spellingIndexDirectory.getAbsolutePath());
	}

	public String checkWords(String searchString) {
		StringTokenizer tokenizer = new StringTokenizer(searchString);
		StringBuilder correctWords = new StringBuilder();
		boolean hasCorrections = false;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			String[] strings;
			try {
				if (spell.exist(token)) {
					correctWords.append(token);
					if (tokenizer.hasMoreTokens()) {
						correctWords.append(" ");
					}
					continue;
				}
				hasCorrections = true;
				strings = spell.suggestSimilar(token, 1);
			} catch (IOException e) {
				logger.error("Exception checking spelling for : " + token, e);
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