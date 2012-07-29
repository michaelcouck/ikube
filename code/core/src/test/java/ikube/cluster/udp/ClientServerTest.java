package ikube.cluster.udp;

import ikube.toolkit.ThreadUtilities;

import org.junit.Test;

public class ClientServerTest {
	
	@Test
	public void server() {
		new UdpServer();
		ThreadUtilities.sleep(15000);
	}
	
	@Test
	public void client() {
		new UdpClient();
		ThreadUtilities.sleep(15000);
	}
	
}
