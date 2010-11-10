package ikube.index.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ikube.BaseTest;
import ikube.index.content.ColumnContentProvider;
import ikube.index.content.IContentProvider;
import ikube.model.IndexableColumn;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.sql.Types;

import org.junit.Test;



public class ColumnContentProviderTest extends BaseTest {

	@Test
	public void getContent() throws Exception {
		IContentProvider<IndexableColumn> contentProvider = new ColumnContentProvider();

		IndexableColumn indexable = mock(IndexableColumn.class);

		when(indexable.getColumnType()).thenReturn(Types.BOOLEAN);
		when(indexable.getObject()).thenReturn(Boolean.TRUE);
		Object result = contentProvider.getContent(indexable);
		assertEquals(Boolean.TRUE.toString(), result);

		when(indexable.getColumnType()).thenReturn(Types.INTEGER);
		when(indexable.getObject()).thenReturn(Integer.MAX_VALUE);
		result = contentProvider.getContent(indexable);
		assertEquals(Integer.toString(Integer.MAX_VALUE), result);

		long time = System.currentTimeMillis();
		when(indexable.getColumnType()).thenReturn(Types.TIMESTAMP);
		when(indexable.getObject()).thenReturn(new Timestamp(time));
		result = contentProvider.getContent(indexable);
		assertEquals(Long.toString(time), result);

		String string = "12456";
		byte[] bytes = string.getBytes();
		when(indexable.getColumnType()).thenReturn(Types.LONGVARBINARY);
		when(indexable.getObject()).thenReturn(bytes);
		result = contentProvider.getContent(indexable);
		assertEquals(new String(bytes), result);

		Blob blob = mock(Blob.class);
		InputStream inputStream = new ByteArrayInputStream(bytes);
		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(indexable.getColumnType()).thenReturn(Types.BLOB);
		when(indexable.getObject()).thenReturn(blob);
		result = contentProvider.getContent(indexable);
		assertEquals(string, result);

		string = "Michael Couck ";
		bytes = getBytes(string);
		inputStream = new ByteArrayInputStream(bytes);
		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(indexable.getColumnType()).thenReturn(Types.BLOB);
		when(indexable.getObject()).thenReturn(blob);
		result = contentProvider.getContent(indexable);
		Reader reader = (Reader) result;
		char[] chars = new char[1024];
		while (reader.read(chars) > -1) {
			String resultString = new String(chars);
			assertTrue(resultString.contains(string));
		}

		Clob clob = mock(Clob.class);
		Reader characterStream = new StringReader(new String(bytes));
		when(clob.getCharacterStream()).thenReturn(characterStream);
		when(indexable.getColumnType()).thenReturn(Types.CLOB);
		when(indexable.getObject()).thenReturn(clob);
		result = contentProvider.getContent(indexable);
		reader = (Reader) result;
		chars = new char[128];
		while (reader.read(chars) > -1) {
			String resultString = new String(chars);
			assertTrue(resultString.contains(string));
		}
	}

}