package ikube.cluster;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.log4j.Logger;

public class ServicePublisher {

	private static final long SLEEP = 5000;

	private Logger logger;

	private int multicastPort;
	private InetAddress multicastAddressGroup;

	private MulticastSocket socket;
	private DatagramPacket queuedPacket;
	private DatagramPacket receivedPacket;

	public ServicePublisher() {
		logger = Logger.getLogger(this.getClass());
		try {
			receive();
			publish();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void publish() throws Exception {
		multicastAddressGroup = InetAddress.getByName("224.0.0.1");
		multicastPort = 8001;
		socket = new MulticastSocket(multicastPort);
		socket.joinGroup(multicastAddressGroup);
		socket.setSoTimeout((int) SLEEP);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(SLEEP);
						String host = "Some data";
						byte[] bytes = host.getBytes();
						queuedPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), multicastPort);
						socket.send(queuedPacket);
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}
		});
		thread.start();
	}

	public void receive() throws Exception {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(SLEEP);
						byte[] buf = new byte[128];
						receivedPacket = new DatagramPacket(buf, buf.length);
						socket.receive(receivedPacket);
						String remoteHost = receivedPacket.getAddress().getHostAddress();
						String localHost = InetAddress.getLocalHost().getHostAddress();
						logger.info("Recieved packet from : " + remoteHost + " - " + localHost + ", on port : " + receivedPacket.getPort());
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}
		});
		thread.start();
	}

}
