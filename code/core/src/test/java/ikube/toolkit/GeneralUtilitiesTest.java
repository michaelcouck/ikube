package ikube.toolkit;

import static org.junit.Assert.*;

import org.junit.Test;

import ikube.ATest;

public class GeneralUtilitiesTest extends ATest {
	
	public GeneralUtilitiesTest() {
		super(GeneralUtilitiesTest.class);
	}
	
	@Test
	public void findFirstOpenPort() {
		int port = 3654;
		int firstOpenPort = GeneralUtilities.findFirstOpenPort(port);
		assertEquals("The first port should be " + port + ", there should be nothing on this port : ", port, firstOpenPort);
	}
	
}
