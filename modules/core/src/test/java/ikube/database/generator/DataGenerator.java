package ikube.database.generator;

import ikube.ATest;
import ikube.logging.Logging;
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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

	private String wordsFilePath = "/data/words.txt";
	private String configurationFilePath = "/data/spring.xml";

	private DataSource dataSource;
	private Connection connection;
	private List<String> words;

	private Map<String, byte[]> fileContents;

	private int inserts = 100;
	private int iterations = 10000;

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
		try {
			connection.setAutoCommit(Boolean.FALSE);
			String type = "Data generation : ";
			PerformanceTester.execute(new PerformanceTester.APerform() {
				@Override
				public void execute() throws Exception {
					insertFaqs();
					insertAttachments();
				}
			}, type, iterations);
		} finally {
			connection.close();
		}
		logger.info(Logging.getString("Inserted : ", (iterations * inserts), (iterations * inserts * fileContents.size())));
		// iterations * inserts + (iterations * inserts * docs)
		// 80000
	}

	protected void insertFaqs() throws Exception {
		String faqInsert = "INSERT INTO DB2ADMIN.FAQ (DB2ADMIN.FAQ.ANSWER, DB2ADMIN.FAQ.CREATIONTIMESTAMP, DB2ADMIN.FAQ.CREATOR, DB2ADMIN.FAQ.MODIFIEDTIMESTAMP, DB2ADMIN.FAQ.MODIFIER, DB2ADMIN.FAQ.PUBLISHED, QUESTION) VALUES (?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = connection.prepareStatement(faqInsert, PreparedStatement.RETURN_GENERATED_KEYS);
		for (int i = 0; i < inserts; i++) {
			String string = generateText((int) (Math.random() * 40), 1024);
			preparedStatement.setString(1, string); // ANSWER
			preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis())); // CREATIONTIMESTAMP
			string = generateText(3, 32);
			preparedStatement.setString(3, string); // CREATOR
			preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // MODIFIEDTIMESTAMP
			string = generateText(2, 32);
			preparedStatement.setString(5, string); // MODIFIER
			preparedStatement.setInt(6, 1); // PUBLISHED
			string = generateText((int) (Math.random() * 40), 1024);
			preparedStatement.setString(7, string); // QUESTION
			preparedStatement.addBatch();
		}
		preparedStatement.executeBatch();
		connection.commit();
		preparedStatement.close();
	}

	protected void insertAttachments() throws Exception {
		String faqIdSelect = "SELECT DB2ADMIN.FAQ.FAQID FROM DB2ADMIN.FAQ ORDER BY DB2ADMIN.FAQ.FAQID DESC";

		List<Long> faqIds = new ArrayList<Long>();
		Statement statement = connection.createStatement();
		ResultSet faqIdResultSet = statement.executeQuery(faqIdSelect);
		while (faqIdResultSet.next()) {
			long faqId = faqIdResultSet.getLong(1);
			faqIds.add(faqId);
		}
		faqIdResultSet.close();
		statement.close();

		Iterator<Long> faqIdIterator = faqIds.iterator();
		String attachmentInsert = "INSERT INTO DB2ADMIN.ATTACHMENT (DB2ADMIN.ATTACHMENT.ATTACHMENT, DB2ADMIN.ATTACHMENT.LENGTH, "
				+ "DB2ADMIN.ATTACHMENT.NAME, DB2ADMIN.ATTACHMENT.FAQID) VALUES(?,?,?,?)";
		PreparedStatement attachmentPreparedStatement = connection.prepareStatement(attachmentInsert, PreparedStatement.NO_GENERATED_KEYS);
		for (int i = 0; i < inserts; i++) {
			for (String fileName : fileContents.keySet()) {
				if (!faqIdIterator.hasNext()) {
					faqIdIterator = faqIds.iterator();
				}

				long faqId = faqIdIterator.next();

				// Insert the attachment
				byte[] bytes = fileContents.get(fileName);
				InputStream inputStream = new ByteArrayInputStream(bytes);
				attachmentPreparedStatement.setBinaryStream(1, inputStream, bytes.length); // ATTACHMENT
				attachmentPreparedStatement.setInt(2, bytes.length); // LENGTH
				attachmentPreparedStatement.setString(3, fileName); // NAME
				attachmentPreparedStatement.setLong(4, faqId); // FAQID
				attachmentPreparedStatement.addBatch();
			}
		}
		attachmentPreparedStatement.executeBatch();
		connection.commit();
		attachmentPreparedStatement.close();
	}

	protected ByteArrayOutputStream getContents(String fileName) {
		File file = FileUtilities.findFile(new File("."), fileName);
		return FileUtilities.getContents(file);
	}

	protected void getFileContents() {
		fileContents.put("txt.txt", null);
		fileContents.put("html.html", null);
		fileContents.put("xml.xml", null);
		fileContents.put("pdf.pdf", null);
		fileContents.put("doc.doc", null);
		fileContents.put("rtf.rtf", null);
		fileContents.put("ppt.ppt", null);
		fileContents.put("xls.xls", null);
		for (String fileName : fileContents.keySet()) {
			byte[] bytes = getContents(fileName).toByteArray();
			// logger.debug("File contents : " + new String(bytes));
			fileContents.put(fileName, bytes);
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
		this.connection.close();
	}

	public static void main(String[] args) {
		try {
			DataGenerator dataGenerator = new DataGenerator();
			dataGenerator.before();

			// DataLoader dataLoader = new DataLoader();
			// dataLoader.createTables("./modules/core/src/test/resources/data/tables.sql");

			dataGenerator.generate();
			dataGenerator.after();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}