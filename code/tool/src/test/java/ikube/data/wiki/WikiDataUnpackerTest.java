package ikube.data.wiki;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WikiDataUnpackerTest {

	@MockClass(realClass = WikiDataUnpacker.class)
	static class WikiDataUnpackerMock {
		@Mock
		protected static void read7ZAndUnpackFiles(final String inputFilePath, final String... outputDisksPaths) throws Exception {
			// Do nothing
		}
	}

	@Before
	public void before() {
		Mockit.setUpMocks(WikiDataUnpackerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(WikiDataUnpackerMock.class);
	}

	@Test
	public void main() throws Exception {
		String[] args = {};
		WikiDataUnpacker.main(args);
		args = new String[] { WikiDataUnpacker.UNPACK_SINGLES, "/NoFile", "/NoDisks" };
		WikiDataUnpacker.main(args);
		// Nothing to do here, just no exception expected
	}

}
