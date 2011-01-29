package ikube.toolkit.datageneration;

import ikube.logging.Logging;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class DataGeneratorOne extends ADataGenerator {

	private DataSource dataSource;
	private Connection connection;

	private int batch = 1000;
	private int iterations = 100000 - (10285);

	@Before
	public void before() throws Exception {
		super.before();
		this.dataSource = ApplicationContextManager.getBean(DataSource.class);
		this.connection = this.dataSource.getConnection();
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
		int faqs = (iterations * batch);
		int attachments = (iterations * batch * fileContents.size());
		logger.info(Logging.getString("Inserted faqs : ", faqs, "attachments : ", attachments));
	}

	protected void insertFaqs() throws Exception {
		String faqInsert = "INSERT INTO DB2ADMIN.FAQ (ANSWER, CREATIONTIMESTAMP, CREATOR, MODIFIEDTIMESTAMP, MODIFIER, PUBLISHED, QUESTION) VALUES (?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = connection.prepareStatement(faqInsert, PreparedStatement.RETURN_GENERATED_KEYS);
		for (int i = 0; i < batch; i++) {
			String string = generateText((int) (Math.random() * 40), 128);
			preparedStatement.setString(1, string); // ANSWER
			preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis())); // CREATIONTIMESTAMP
			string = generateText(3, 32);
			preparedStatement.setString(3, string); // CREATOR
			preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // MODIFIEDTIMESTAMP
			string = generateText(2, 32);
			preparedStatement.setString(5, string); // MODIFIER
			preparedStatement.setInt(6, 1); // PUBLISHED
			string = generateText((int) (Math.random() * 40), 128);
			preparedStatement.setString(7, string); // QUESTION
			preparedStatement.addBatch();
		}
		preparedStatement.executeBatch();
		connection.commit();
		preparedStatement.close();
	}

	protected void insertAttachments() throws Exception {
		String faqIdSelect = "SELECT FAQID FROM DB2ADMIN.FAQ ORDER BY FAQID DESC";

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
		String attachmentInsert = "INSERT INTO DB2ADMIN.ATTACHMENT (NAME, LENGTH, FAQID, ATTACHMENT) VALUES(?,?,?,?)";
		PreparedStatement attachmentPreparedStatement = connection.prepareStatement(attachmentInsert, PreparedStatement.NO_GENERATED_KEYS);
		for (int i = 0; i < batch; i++) {
			for (String fileName : fileContents.keySet()) {
				if (!faqIdIterator.hasNext()) {
					faqIdIterator = faqIds.iterator();
				}

				long faqId = faqIdIterator.next();

				// Insert the attachment
				byte[] bytes = fileContents.get(fileName);
				// InputStream inputStream = new ByteArrayInputStream(bytes);

				attachmentPreparedStatement.setString(1, fileName); // NAME
				attachmentPreparedStatement.setInt(2, bytes.length); // LENGTH
				attachmentPreparedStatement.setLong(3, faqId); // FAQID
				attachmentPreparedStatement.setBinaryStream(4, null, 0); // ATTACHMENT
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

	@After
	public void after() throws Exception {
		super.after();
		this.connection.close();
	}

	public static void main(String[] args) {
		try {
			DataGeneratorOne dataGenerator = new DataGeneratorOne();
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