package ikube.index.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.logging.Logging;
import ikube.model.IndexableColumn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.db2.jcc.DB2Driver;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ColumnContentProviderTest {

	static {
		Logging.configure();
	}

	private Logger logger = Logger.getLogger(this.getClass());
	private IndexableColumn indexable = mock(IndexableColumn.class);
	private IContentProvider<IndexableColumn> contentProvider = new ColumnContentProvider();

	@Test
	public void getContent() throws Exception {
		OutputStream outputStream = new ByteArrayOutputStream();

		when(indexable.getColumnType()).thenReturn(Types.BOOLEAN);
		when(indexable.getObject()).thenReturn(Boolean.TRUE);
		contentProvider.getContent(indexable, outputStream);
		assertEquals(Boolean.TRUE.toString(), outputStream.toString());

		when(indexable.getColumnType()).thenReturn(Types.INTEGER);
		when(indexable.getObject()).thenReturn(Integer.MAX_VALUE);
		contentProvider.getContent(indexable, outputStream);
		assertEquals(Integer.toString(Integer.MAX_VALUE), outputStream.toString());

		long time = System.currentTimeMillis();
		when(indexable.getColumnType()).thenReturn(Types.TIMESTAMP);
		when(indexable.getObject()).thenReturn(new Timestamp(time));
		contentProvider.getContent(indexable, outputStream);
		assertEquals(Long.toString(time), outputStream.toString());

		String string = "12456";
		byte[] bytes = string.getBytes();
		when(indexable.getColumnType()).thenReturn(Types.LONGVARBINARY);
		when(indexable.getObject()).thenReturn(bytes);
		contentProvider.getContent(indexable, outputStream);
		assertEquals(new String(bytes), outputStream.toString());

		Blob blob = mock(Blob.class);
		InputStream inputStream = new ByteArrayInputStream(bytes);
		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(indexable.getColumnType()).thenReturn(Types.BLOB);
		when(indexable.getObject()).thenReturn(blob);
		contentProvider.getContent(indexable, outputStream);
		assertEquals(string, outputStream.toString());

		string = "Michael Couck ";
		bytes = getBytes(string);
		inputStream = new ByteArrayInputStream(bytes);
		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(indexable.getColumnType()).thenReturn(Types.BLOB);
		when(indexable.getObject()).thenReturn(blob);
		contentProvider.getContent(indexable, outputStream);
		bytes = new byte[1024];

		assertTrue(outputStream.toString().contains(string));

		Clob clob = mock(Clob.class);
		Reader characterStream = new StringReader(new String(bytes));
		when(clob.getCharacterStream()).thenReturn(characterStream);
		when(indexable.getColumnType()).thenReturn(Types.CLOB);
		when(indexable.getObject()).thenReturn(clob);

		contentProvider.getContent(indexable, outputStream);

		assertTrue(outputStream.toString().contains(string));
	}

	@Test
	@Ignore
	public void performance() throws Exception {
		Class.forName(DB2Driver.class.getName());
		Connection connection = DriverManager.getConnection("jdbc:db2://ikube:50000/ikube", "db2admin", "db2admin");
		connection.setAutoCommit(Boolean.FALSE);
		connection.setReadOnly(Boolean.TRUE);

		String sql = "select attachmentid, attachment from attachment where attachmentid > ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setLong(1, 0);
		ResultSet resultSet = preparedStatement.executeQuery();

		IndexableColumn indexable = new IndexableColumn();
		indexable.setColumnType(Types.BLOB);

		int count = 0;
		while (resultSet.next()) {
			count++;
			if (count % 1000 == 0) {
				logger.info("Count : " + count);
				connection.commit();
			}
			Object object = resultSet.getObject("attachment");
			indexable.setObject(object);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			contentProvider.getContent(indexable, outputStream);
			indexable.setObject(null);
			outputStream.close();
			outputStream = null;
		}
	}

	/**
	 * Returns the max read length byte array plus 1000, i.e. more than the max bytes that the application can read. This forces the indexer
	 * to get a reader rather than a string.
	 * 
	 * @param string
	 *            the string to copy to the byte array until the max read length is exceeded
	 * @return the byte array of the string copied several times more than the max read length
	 */
	protected byte[] getBytes(String string) {
		byte[] bytes = new byte[(int) (IConstants.MAX_READ_LENGTH + IConstants.MAX_READ_LENGTH + 1000)];
		for (int offset = 0; offset < bytes.length;) {
			byte[] segment = string.getBytes();
			if (offset + segment.length >= bytes.length) {
				break;
			}
			System.arraycopy(segment, 0, bytes, offset, segment.length);
			offset += segment.length;
		}
		return bytes;
	}

	public static void main(String[] args) throws Exception {
		ColumnContentProviderTest columnContentProviderTest = new ColumnContentProviderTest();
		columnContentProviderTest.performance();
	}

}