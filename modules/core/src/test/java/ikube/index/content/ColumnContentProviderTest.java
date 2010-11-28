package ikube.index.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.index.parse.IParser;
import ikube.model.IndexableColumn;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
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

	@Test
	public void getContent() throws Exception {
		when(indexable.getColumnType()).thenReturn(Types.BOOLEAN);
		when(indexable.getObject()).thenReturn(Boolean.TRUE);
		Object result = contentProvider.getContent(indexable);
		assertEquals(Boolean.TRUE.toString(), result.toString());

		when(indexable.getColumnType()).thenReturn(Types.INTEGER);
		when(indexable.getObject()).thenReturn(Integer.MAX_VALUE);
		result = contentProvider.getContent(indexable);
		assertEquals(Integer.toString(Integer.MAX_VALUE), result.toString());

		long time = System.currentTimeMillis();
		when(indexable.getColumnType()).thenReturn(Types.TIMESTAMP);
		when(indexable.getObject()).thenReturn(new Timestamp(time));
		result = contentProvider.getContent(indexable);
		assertEquals(Long.toString(time), result.toString());

		String string = "12456";
		byte[] bytes = string.getBytes();
		when(indexable.getColumnType()).thenReturn(Types.LONGVARBINARY);
		when(indexable.getObject()).thenReturn(bytes);
		result = contentProvider.getContent(indexable);
		result = FileUtilities.getContents((InputStream) result, Integer.MAX_VALUE).toString();
		assertEquals(new String(bytes), result);

		Blob blob = mock(Blob.class);
		InputStream inputStream = new ByteArrayInputStream(bytes);
		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(indexable.getColumnType()).thenReturn(Types.BLOB);
		when(indexable.getObject()).thenReturn(blob);
		result = contentProvider.getContent(indexable);
		result = FileUtilities.getContents((InputStream) result, Integer.MAX_VALUE).toString();
		assertEquals(string, result.toString());

		string = "Michael Couck ";
		bytes = getBytes(string);
		inputStream = new ByteArrayInputStream(bytes);
		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(indexable.getColumnType()).thenReturn(Types.BLOB);
		when(indexable.getObject()).thenReturn(blob);
		result = contentProvider.getContent(indexable);
		bytes = new byte[1024];

		while (((InputStream) result).read(bytes) > -1) {
			String resultString = new String(bytes);
			assertTrue(resultString.contains(string));
		}

		Clob clob = mock(Clob.class);
		Reader characterStream = new StringReader(new String(bytes));
		when(clob.getCharacterStream()).thenReturn(characterStream);
		when(indexable.getColumnType()).thenReturn(Types.CLOB);
		when(indexable.getObject()).thenReturn(clob);
		result = contentProvider.getContent(indexable);

		while (((InputStream) result).read(bytes) > -1) {
			String resultString = new String(bytes);
			assertTrue(resultString.contains(string));
		}
	}

	@Test
	public void performance() throws Exception {
		Blob blob = mock(Blob.class);
		String string = "Michael Couck ";
		byte[] bytes = getBytes(string);
		InputStream inputStream = new ByteArrayInputStream(bytes);
		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(indexable.getColumnType()).thenReturn(Types.BLOB);
		when(indexable.getObject()).thenReturn(blob);

		for (int i = 0; i < 1000; i++) {
			performance(inputStream);
			Thread.sleep(10000);
		}

	}

	protected void performance(final InputStream inputStream) throws Exception {
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				inputStream.reset();
				InputStream contentInputStream = (InputStream) contentProvider.getContent(indexable);
				assertNotNull(contentInputStream);
				contentInputStream.close();
			}
		}, "Excel parser performance : ", 10000);
	}

	public static void main(String[] args) throws Exception {
		ColumnContentProviderTest columnContentProviderTest = new ColumnContentProviderTest();
		columnContentProviderTest.performance();
	}

}