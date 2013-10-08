package ikube.toolkit;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used as a test bean to see if udp is supported on a network. The class will start a client and a server that will then
 * talk to each other over multi casted udp. If there are any other instantiations on the network then they will also be involved in the
 * 'communication' and there will be logging between each of the instances on the machines.
 * 
 * If only on one machine then the client and server only talk to each other, sweet no?
 * 
 * @author Michael Couck
 * @since 30.07.12
 * @version 01.00
 */
public class UdpBroadcaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpBroadcaster.class);

	private static final int PORT = 9876;
	private static final String MCAST_ADDR = "224.0.0.1";

	private static InetAddress GROUP;

	public static void main(String[] args) {
		try {
			GROUP = InetAddress.getByName(MCAST_ADDR);
			List<Future<?>> futures = new UdpBroadcaster().initialize();
			ThreadUtilities.waitForFutures(futures, 60000);
		} catch (Exception e) {
			LOGGER.error("Usage : [group-ip] [port]");
		}
	}

	public List<Future<?>> initialize() {
		ThreadUtilities.initialize();

		Future<?> server = server();
		ThreadUtilities.sleep(3000);
		Future<?> client = client();

		return Arrays.asList(server, client);
	}

	private Future<?> client() {
		return ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
			public void run() {
				MulticastSocket multicastSocket = null;
				try {
					multicastSocket = new MulticastSocket(PORT);
					multicastSocket.joinGroup(GROUP);
					while (true) {
						try {
							byte[] receiveData = new byte[256];
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
							multicastSocket.receive(receivePacket);
							LOGGER.info("Client received from : " + receivePacket.getAddress() + ", " + new String(receivePacket.getData()));
						} catch (Exception e) {
							LOGGER.error(null, e);
						}
					}
				} catch (Exception e) {
					LOGGER.error(null, e);
				} finally {
					multicastSocket.close();
				}
			}
		});
	}

	private Future<?> server() {
		return ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
			public void run() {
				DatagramSocket serverSocket = null;
				try {
					serverSocket = new DatagramSocket();
					try {
						while (true) {
							byte[] sendData = new byte[256];
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, GROUP, PORT);
							serverSocket.send(sendPacket);
							ThreadUtilities.sleep(10000);
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
