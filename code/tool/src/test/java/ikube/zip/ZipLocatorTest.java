package ikube.zip;

import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;

import org.junit.Test;

public class ZipLocatorTest extends AbstractTest {

	@Test
	public void main() {
		String[] args = { ".", ".*(\\.zip\\Z).*|.*(\\.jar\\Z).*", "Maths", "Maths" };
		ZipLocator.main(args);
		assertTrue(ZipLocator.ATOMIC_INTEGER.get() > 0);
	}

}
