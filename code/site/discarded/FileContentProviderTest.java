package ikube.index.content;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.model.IndexableFileSystem;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@Ignore
@Deprecated
public class FileContentProviderTest extends ATest {

	private FileContentProvider contentProvider;

	public FileContentProviderTest() {
		super(FileContentProviderTest.class);
	}

	@Before
	public void before() {
		this.contentProvider = new FileContentProvider();
	}

	@Test
	public void getContent() {
		OutputStream outputStream = new ByteArrayOutputStream();
		IndexableFileSystem indexableFileSystem = mock(IndexableFileSystem.class);
		// File file = FileUtilities.findFileRecursively(new File("."), "xml.xml");
		// when(indexableFileSystem.getCurrentFile()).thenReturn(file);
		when(indexableFileSystem.getMaxReadLength()).thenReturn(Long.MAX_VALUE);
		this.contentProvider.getContent(indexableFileSystem, outputStream);
		String content = outputStream.toString();
		assertNotNull(content);
		assertTrue(content.contains(IConstants.IKUBE));
	}

}
