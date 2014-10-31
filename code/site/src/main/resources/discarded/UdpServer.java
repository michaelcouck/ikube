package ikube.cluster.udp;

import ikube.toolkit.ThreadUtilities;

import java.io.BufferedReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpServer extends Base implements Runnable {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	protected boolean active = true;
	protected DatagramSocket multicastSocket = null;
	// protected MulticastSocket multicastSocket;
	protected BufferedReader bufferedReader = null;

	public static void main(String[] args) {
		UdpServer udpServer = new UdpServer();
		ThreadUtilities.sleep(60000);
		udpServer.active = false;
	}

	public UdpServer() {
		new Thread(this).start();
	}

	public void run() {
		try {
			multicastSocket = new DatagramSocket(port);
			// multicastSocket = new MulticastSocket(port);
			while (active) {
				byte[] buf = "Michael Couck".getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("230.0.0.1"), port);
				multicastSocket.send(packet);
				ThreadUtilities.sleep(1000);
				logger.info("Sending packet : " + packet);
			}
		} catch (Exception e) {
			logger.error(null, e);
		} finally {
			multicastSocket.close();
		}
	}

}
