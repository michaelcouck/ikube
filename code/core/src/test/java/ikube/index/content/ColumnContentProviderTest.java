package ikube.index.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.model.IndexableColumn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.sql.Types;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ColumnContentProviderTest extends ATest {

	private IndexableColumn indexable = mock(IndexableColumn.class);
	private IContentProvider<IndexableColumn> contentProvider = new ColumnContentProvider();

	public ColumnContentProviderTest() {
		super(ColumnContentProviderTest.class);
	}

	@Test
	public void getContent() throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		when(indexable.getColumnType()).thenReturn(Types.BOOLEAN);
		when(indexable.getContent()).thenReturn(Boolean.TRUE);
		contentProvider.getContent(indexable, outputStream);
		assertEquals(Boolean.TRUE.toString(), outputStream.toString());

		outputStream = new ByteArrayOutputStream();
		when(indexable.getColumnType()).thenReturn(Types.INTEGER);
		when(indexable.getContent()).thenReturn(Integer.MAX_VALUE);
		contentProvider.getContent(indexable, outputStream);
		assertEquals(Integer.toString(Integer.MAX_VALUE), outputStream.toString());

		outputStream = new ByteArrayOutputStream();
		long time = System.currentTimeMillis();
		when(indexable.getColumnType()).thenReturn(Types.TIMESTAMP);
		when(indexable.getContent()).thenReturn(new Timestamp(time));
		contentProvider.getContent(indexable, outputStream);
		assertEquals(Long.toString(time), outputStream.toString());

		outputStream = new ByteArrayOutputStream();
		String string = "12456";
		byte[] bytes = string.getBytes();
		when(indexable.getColumnType()).thenReturn(Types.LONGVARBINARY);
		when(indexable.getContent()).thenReturn(bytes);
		contentProvider.getContent(indexable, outputStream);
		assertEquals(new String(bytes), outputStream.toString());

		outputStream = new ByteArrayOutputStream();
		Blob blob = mock(Blob.class);
		InputStream inputStream = new ByteArrayInputStream(bytes);
		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(indexable.getColumnType()).thenReturn(Types.BLOB);
		when(indexable.getContent()).thenReturn(blob);
		contentProvider.getContent(indexable, outputStream);
		assertEquals(string, outputStream.toString());

		outputStream = new ByteArrayOutputStream();
		string = "Michael Couck ";
		bytes = getBytes(string);
		inputStream = new ByteArrayInputStream(bytes);
		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(indexable.getColumnType()).thenReturn(Types.BLOB);
		when(indexable.getContent()).thenReturn(blob);
		contentProvider.getContent(indexable, outputStream);

		assertTrue(outputStream.toString().contains(string));

		outputStream = new ByteArrayOutputStream();
		Clob clob = mock(Clob.class);
		Reader characterStream = new StringReader(new String(bytes));
		when(clob.getCharacterStream()).thenReturn(characterStream);
		when(indexable.getColumnType()).thenReturn(Types.CLOB);
		when(indexable.getContent()).thenReturn(clob);

		contentProvider.getContent(indexable, outputStream);

		assertTrue(outputStream.toString().contains(string));

		// TODO Find out why the Maven compile doesn't like this character sncoding
		// note that the encoding has been set in all the places I could find on the net
		// string = "Saint-Herménégilde";
		// outputStream = new ByteArrayOutputStream();
		// when(indexable.getColumnType()).thenReturn(Types.LONGVARCHAR);
		// when(indexable.getContent()).thenReturn(string);
		// contentProvider.getContent(indexable, outputStream);
		// assertEquals(string, outputStream.toString(IConstants.ENCODING));
		//
		// string = "Soleymān Khāţer";
		// outputStream = new ByteArrayOutputStream();
		// when(indexable.getColumnType()).thenReturn(Types.LONGVARCHAR);
		// when(indexable.getContent()).thenReturn(string);
		// contentProvider.getContent(indexable, outputStream);
		// assertEquals(string, outputStream.toString(IConstants.ENCODING));
	}

	/**
	 * Returns the max read length byte array plus 1000, i.e. more than the max bytes that the application can read. This forces the indexer
	 * to get a reader rather than a string.
	 * 
	 * @param string
	 *            the string to copy to the byte array until the max read length is exceeded
	 * @return the byte array of the string copied several times more than the max read length
	 * @throws UnsupportedEncodingException
	 */
	protected byte[] getBytes(String string) throws UnsupportedEncodingException {
		byte[] bytes = new byte[(int) (IConstants.MAX_READ_LENGTH + IConstants.MAX_READ_LENGTH + 1000)];
		for (int offset = 0; offset < bytes.length;) {
			byte[] segment = string.getBytes(IConstants.ENCODING);
			if (offset + segment.length >= bytes.length) {
				break;
			}
			System.arraycopy(segment, 0, bytes, offset, segment.length);
			offset += segment.length;
		}
		return bytes;
	}

}