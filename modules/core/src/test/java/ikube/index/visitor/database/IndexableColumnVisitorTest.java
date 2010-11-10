package ikube.index.visitor.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.model.IndexableColumn;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.Types;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.junit.Before;
import org.junit.Test;

public class IndexableColumnVisitorTest extends BaseTest {

	private IndexableColumnVisitor<IndexableColumn> indexableColumnVisitor;
	private String fieldName = "fieldName";
	private Document document;
	private IndexableColumn indexable;

	@Before
	public void before() {
		this.indexableColumnVisitor = new IndexableColumnVisitor<IndexableColumn>();
		this.document = new Document();
		this.indexable = mock(IndexableColumn.class);
		this.indexableColumnVisitor.setDocument(document);

		when(indexable.getName()).thenReturn(fieldName);
		when(indexable.isAnalyzed()).thenReturn(Boolean.TRUE);
		when(indexable.isStored()).thenReturn(Boolean.TRUE);
		when(indexable.isVectored()).thenReturn(Boolean.TRUE);
	}

	@Test
	public void visit() throws Exception {
		// We visit a string then a reader
		String string = "<html><body>And some text</body></html>";
		when(indexable.getObject()).thenReturn(string);
		when(indexable.getColumnType()).thenReturn(Types.VARCHAR);
		this.indexableColumnVisitor.visit(indexable);
		// Add the field again and it should be merged
		this.indexableColumnVisitor.visit(indexable);
		String fieldValue = document.get(fieldName);
		assertNotNull(fieldValue);
		assertEquals("And some text And some text", fieldValue);

		// We add a reader field next and it should be merged with the string
		// value in the field already
		Clob clob = mock(Clob.class);
		byte[] bytes = getBytes("Michael Couck ");
		Reader characterStream = new StringReader(new String(bytes));
		when(clob.getCharacterStream()).thenReturn(characterStream);
		when(indexable.getColumnType()).thenReturn(Types.CLOB);
		when(indexable.getObject()).thenReturn(clob);

		this.indexableColumnVisitor.visit(indexable);
		Field field = document.getField(fieldName);
		assertNotNull(field);
		assertNotNull(field.readerValue());
	}

}
