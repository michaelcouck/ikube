package ikube.toolkit.data;

import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public abstract class ADataGenerator implements IDataGenerator {

	private static final long MAX_FILE_LENGTH = 100000;

	protected Logger logger = Logger.getLogger(this.getClass());
	private String wordsFilePath = "words.txt";
	protected List<String> words;
	protected Map<String, byte[]> fileContents;

	public void before() throws Exception {
		File dotFolder = new File(".");
		words = new ArrayList<String>();
		fileContents = new HashMap<String, byte[]>();
		File wordsFile = FileUtilities.findFile(dotFolder, wordsFilePath);
		populateWords(wordsFile);
		String[] fileTypes = new String[] { ".doc", ".html", ".pdf", ".pot", ".ppt", ".rtf", ".txt", ".xml" };
		List<File> files = FileUtilities.findFilesRecursively(new File("."), new ArrayList<File>(), fileTypes);
		populateFiles(files.toArray(new File[files.size()]), "spring", "svn");
	}

	protected void populateWords(File wordsFile) throws Exception {
		InputStream inputStream = new FileInputStream(wordsFile);
		String words = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
		StringTokenizer tokenizer = new StringTokenizer(words);
		while (tokenizer.hasMoreTokens()) {
			this.words.add(tokenizer.nextToken());
		}
	}

	protected void populateFiles(File[] files, String... excludedPatterns) {
		outer: for (File file : files) {
			if (file == null || !file.exists() || !file.canRead() || !file.isFile()) {
				continue;
			}
			if (file.length() > MAX_FILE_LENGTH) {
				logger.warn("File too big : " + file.length() + ":" + MAX_FILE_LENGTH);
				continue;
			}
			for (String excludedPattern : excludedPatterns) {
				if (file.getName().contains(excludedPattern)) {
					continue outer;
				}
			}
			// logger.info("Loading file : " + file.getAbsolutePath());
			byte[] contents = FileUtilities.getContents(file).toByteArray();
			fileContents.put(file.getName(), contents);
			continue;
		}
	}

	protected String generateText(int count, int maxLength) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			int index = (int) (Math.random() * (words.size() - 1));
			String word = this.words.get(index);
			builder.append(word);
			builder.append(" ");
		}
		if (builder.length() > maxLength) {
			return builder.substring(0, maxLength);
		}
		return builder.toString();
	}

	protected Object instanciateObject(Class<?> klass, int length) {
		if (Boolean.class.equals(klass) || boolean.class.equals(klass)) {
			return Boolean.TRUE;
		} else if (Integer.class.equals(klass) || int.class.equals(klass)) {
			return new Integer((int) System.nanoTime());
		} else if (Long.class.equals(klass) || long.class.equals(klass)) {
			return new Long(System.nanoTime());
		} else if (Timestamp.class.equals(klass)) {
			return new Timestamp(System.currentTimeMillis());
		} else if (Date.class.equals(klass)) {
			return new Date(System.currentTimeMillis());
		} else if (String.class.equals(klass)) {
			return generateText(length * 5, length);
		} else if (Blob.class.equals(klass)) {
			return new ByteArrayInputStream(generateText(length * 5, length).getBytes());
		}
		return null;
	}

	public void after() throws Exception {
	}

}
