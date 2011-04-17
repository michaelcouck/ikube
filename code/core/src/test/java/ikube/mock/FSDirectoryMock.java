package ikube.mock;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import mockit.Mock;
import mockit.MockClass;

import org.apache.lucene.store.FSDirectory;

@MockClass(realClass = FSDirectory.class)
public class FSDirectoryMock {

	@Mock
	public static FSDirectory open(File path) throws IOException {
		return mock(FSDirectory.class);
	}
}
