package ikube.zip;

import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ZipLocatorTest extends AbstractTest {

	@Test
	public void main() {
		String[] args = { ".", ".*(serenity.jar).*", "index", "Serenity" };
		ZipLocator.main(args);
		assertTrue(ZipLocator.ATOMIC_INTEGER.get() > 0);
	}

}
