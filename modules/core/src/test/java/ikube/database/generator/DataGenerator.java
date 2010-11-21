package ikube.database.generator;

import ikube.ATest;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Ignore
public class DataGenerator extends ATest {

	private String faqSqlFilePath = "faq.sql";
	private String attachmentSqlFilePath = "attachment.sql";

	private String wordsFilePath = "/data/words.txt";
	private String configurationFilePath = "/data/spring.xml";

	private DataSource dataSource;
	private Connection connection;
	private List<String> words;

	private Map<String, byte[]> fileContents;

	@Before
	public void before() throws Exception {
		ApplicationContextManager.getApplicationContext(configurationFilePath);
		this.dataSource = ApplicationContextManager.getBean(DataSource.class);
		this.connection = this.dataSource.getConnection();
		this.words = new ArrayList<String>();
		InputStream inputStream = this.getClass().getResourceAsStream(wordsFilePath);
		String words = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
		StringTokenizer tokenizer = new StringTokenizer(words);
		while (tokenizer.hasMoreTokens()) {
			this.words.add(tokenizer.nextToken());
		}
		this.fileContents = new HashMap<String, byte[]>();
		getFileContents();
	}

	@Test
	public void generate() throws Exception {
		final String faqInsert = getContents(faqSqlFilePath).toString();
		final String attachmentInsert = getContents(attachmentSqlFilePath).toString();
		try {
			PerformanceTester.execute(new PerformanceTester.APerform() {
				@Override
				public void execute() throws Exception {
					// Insert the faq
					PreparedStatement preparedStatement = null;
					PreparedStatement attachmentPreparedStatement = null;
					ResultSet resultSet = null;
					try {
						preparedStatement = connection.prepareStatement(faqInsert, PreparedStatement.RETURN_GENERATED_KEYS);
						preparedStatement.setString(1, generateText((int) (Math.random() * 100))); // ANSWER
						preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis())); // CREATIONTIMESTAMP
						preparedStatement.setString(3, generateText(3)); // CREATOR
						preparedStatement.setString(4, generateText(1)); // GANG
						preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // MODIFIEDTIMESTAMP
						preparedStatement.setString(6, generateText(2)); // MODIFIER
						preparedStatement.setInt(7, 1); // PUBLISHED
						preparedStatement.setString(8, generateText((int) (Math.random() * 50))); // QUESTION
						preparedStatement.executeUpdate();
						resultSet = preparedStatement.getGeneratedKeys();
						while (resultSet.next()) {
							long faqId = resultSet.getLong(1);
							for (String fileName : fileContents.keySet()) {
								// Insert the attachment
								byte[] bytes = fileContents.get(fileName);
								InputStream inputStream = new ByteArrayInputStream(bytes);
								attachmentPreparedStatement = connection.prepareStatement(attachmentInsert,
										PreparedStatement.NO_GENERATED_KEYS);
								attachmentPreparedStatement.setBinaryStream(1, inputStream, bytes.length); // ATTACHMENT
								attachmentPreparedStatement.setInt(2, bytes.length); // LENGTH
								attachmentPreparedStatement.setString(3, fileName); // NAME
								attachmentPreparedStatement.setLong(4, faqId); // FAQID
								attachmentPreparedStatement.executeUpdate();
								attachmentPreparedStatement.close();
							}
						}
						preparedStatement.close();
						resultSet.close();
					} catch (Exception e) {
						logger.error("", e);
					} finally {
						try {
							resultSet.close();
							attachmentPreparedStatement.close();
							preparedStatement.close();
						} catch (Exception e) {
							logger.error("", e);
						}
					}
				}
			}, "Database insert : ", 10000000);
		} finally {
			connection.close();
		}
	}

	protected ByteArrayOutputStream getContents(String fileName) {
		String[] stringPatterns = new String[] { fileName };
		List<File> files = FileUtilities.findFilesRecursively(new File("."), stringPatterns, new ArrayList<File>());
		File file = files.get(0);
		return FileUtilities.getContents(file);
	}

	protected void getFileContents() {
		fileContents.put("txt.txt", null);
		fileContents.put("html.html", null);
		fileContents.put("xml.xml", null);
		fileContents.put("pdf.pdf", null);
		fileContents.put("doc.doc", null);
		fileContents.put("rtf.rtf", null);
		fileContents.put("pot.pot", null);
		fileContents.put("xls.xls", null);
		for (String fileName : fileContents.keySet()) {
			byte[] bytes = getContents(fileName).toByteArray();
			fileContents.put(fileName, bytes);
		}
	}

	protected String generateText(int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			int index = (int) (Math.random() * (words.size() - 1));
			String word = this.words.get(index);
			builder.append(word);
			builder.append(" ");
		}
		return builder.toString();
	}

	@After
	public void after() throws Exception {
		this.connection.close();
	}

}