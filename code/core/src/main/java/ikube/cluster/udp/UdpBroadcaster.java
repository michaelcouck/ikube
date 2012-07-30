package ikube.cluster.udp;

import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpBroadcaster {

	static {
		Logging.configure();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpBroadcaster.class);

	public static void main(String[] args) {
		List<Future<?>> futures = new UdpBroadcaster().initialize();
		ThreadUtilities.waitForFutures(futures, 60000);
	}

	@SuppressWarnings("unchecked")
	public List<Future<?>> initialize() {
		ThreadUtilities.initialize();
		List<Future<?>> futures = Arrays.asList(client(), server());
		return futures;
	}

	private Future<?> client() {
		return ThreadUtilities.submit(new Runnable() {
			public void run() {
				DatagramSocket clientSocket = null;
				try {
					clientSocket = new DatagramSocket();
					while (true) {
						try {
							ThreadUtilities.sleep(10000);
							InetAddress ipAddress = InetAddress.getByName("localhost");

							byte[] sendData = UriUtilities.getIp().getBytes();
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, 9876);
							LOGGER.info("Sending : " + new String(sendData));
							clientSocket.send(sendPacket);

							byte[] receiveData = new byte[1024];
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
							clientSocket.receive(receivePacket);
							LOGGER.info("Received from : " + receivePacket.getAddress() + ", " + new String(receivePacket.getData()));
						} catch (Exception e) {
							LOGGER.error(null, e);
						}
					}
				} catch (Exception e) {
					LOGGER.error(null, e);
				} finally {
					clientSocket.close();
				}
			}
		});
	}

	private Future<?> server() {
		return ThreadUtilities.submit(new Runnable() {
			public void run() {
				DatagramSocket serverSocket = null;
				try {
					serverSocket = new DatagramSocket(9876);
					try {
						while (true) {
							ThreadUtilities.sleep(10000);
							byte[] receiveData = new byte[1024];
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
							serverSocket.receive(receivePacket);
							LOGGER.info("Server received from : " + receivePacket.getAddress() + ", " + new String(receivePacket.getData()));

							InetAddress IPAddress = receivePacket.getAddress();
							int port = receivePacket.getPort();
							byte[] sendData = new String(receivePacket.getData()).toUpperCase().getBytes();
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
							serverSocket.send(sendPacket);
						}
					} catch (Exception e) {
						LOGGER.error(null, e);
					}
				} catch (Exception e) {
					LOGGER.error(null, e);
				}
			}
		});
	}

}
