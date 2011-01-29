package ikube.toolkit.datageneration;

import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

public abstract class ADataGenerator implements IDataGenerator {

	private static final long MAX_FILE_LENGTH = 100000;

	protected Logger logger;

	private String wordsFilePath = "words.txt";
	private String configFilePath = "spring-data-generation.xml";

	protected List<String> words;
	protected Map<String, byte[]> fileContents;

	@Before
	public void before() throws Exception {
		logger = Logger.getLogger(this.getClass());
		File dotFolder = new File(".");

		words = new ArrayList<String>();
		fileContents = new HashMap<String, byte[]>();

		File configFile = FileUtilities.findFile(dotFolder, configFilePath);
		ApplicationContextManager.getApplicationContext(configFile);

		File wordsFile = FileUtilities.findFile(dotFolder, wordsFilePath);
		populateWords(wordsFile);

		File dataFolder = configFile.getParentFile();
		List<File> files = FileUtilities.findFilesRecursively(dataFolder, new String[] { ".doc", ".html", ".pdf", ".pot", ".ppt", ".rtf",
				".txt", ".xml" }, new ArrayList<File>());
		populateFiles(files.toArray(new File[files.size()]), "svn");
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
			logger.info("Loading file : " + file.getAbsolutePath());
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

	@After
	public void after() throws Exception {
	}

	public static void main(String[] args) {
		// Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@//falcon:1521/XE", "XE", "oracle");
		// connection.createStatement().execute("");
		// System.out.println(connection);
		// connection.close();

		try {
			IDataGenerator dataGenerator = new DataGeneratorThree(3, 1000000);
			dataGenerator.before();
			dataGenerator.generate();
			dataGenerator.after();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
